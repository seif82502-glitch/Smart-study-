package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.domain.OfficialEgyptianCurriculumCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

data class MoePortalCheckResult(
    val portalUrl: String,
    val robotsAllowed: Boolean,
    val httpCode: Int,
    val discoveredPdfLinks: List<String>,
    val statusLabel: String,
    val message: String
)

data class DownloadedPdfInfo(
    val localFile: File,
    val sha256: String,
    val sizeBytes: Long,
    val pageCount: Int
)

/**
 * Integrates respectfully with https://moe.gov.eg/ and https://studentbooks.moe.gov.eg/
 * checking robots.txt, validating official *.moe.gov.eg hostnames, storing downloaded
 * PDFs locally under filesDir/moe_official, and rendering PDF pages with Android PdfRenderer.
 */
class MoePortalSyncService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val storageDir: File
        get() = File(context.filesDir, "moe_official").apply { mkdirs() }

    fun isOfficialMoeUrl(urlStr: String): Boolean {
        return try {
            val host = java.net.URI(urlStr).host?.lowercase() ?: return false
            host == "moe.gov.eg" || host.endsWith(".moe.gov.eg")
        } catch (e: Exception) {
            false
        }
    }

    suspend fun checkMoePortal(portalUrl: String = OfficialEgyptianCurriculumCatalog.MOE_BOOKS_PORTAL): MoePortalCheckResult =
        withContext(Dispatchers.IO) {
            if (!isOfficialMoeUrl(portalUrl)) {
                return@withContext MoePortalCheckResult(
                    portalUrl = portalUrl,
                    robotsAllowed = false,
                    httpCode = 0,
                    discoveredPdfLinks = emptyList(),
                    statusLabel = "INVALID_DOMAIN",
                    message = "Only verified *.moe.gov.eg domains are permitted."
                )
            }

            // 1. Check robots.txt politely
            val robotsAllowed = checkRobotsTxt(portalUrl)
            if (!robotsAllowed) {
                return@withContext MoePortalCheckResult(
                    portalUrl = portalUrl,
                    robotsAllowed = false,
                    httpCode = 403,
                    discoveredPdfLinks = emptyList(),
                    statusLabel = "ROBOTS_DISALLOWED",
                    message = "Automated scraping disallowed by robots.txt. Use Admin PDF upload."
                )
            }

            // 2. Fetch publicly accessible portal HTML
            try {
                val request = Request.Builder()
                    .url(portalUrl)
                    .header("User-Agent", USER_AGENT)
                    .build()
                client.newCall(request).execute().use { response ->
                    val code = response.code
                    val body = response.body?.string().orEmpty()
                    val pdfRegex = Regex("""https?://[a-zA-Z0-9.\-_/]+moe\.gov\.eg[^\s"'<>]+\.pdf""", RegexOption.IGNORE_CASE)
                    val discovered = pdfRegex.findAll(body).map { it.value }.distinct().take(25).toList()

                    val statusLabel = when {
                        code in 200..299 && discovered.isNotEmpty() -> "DISCOVERED_PDFS"
                        code in 200..299 -> "PORTAL_REACHABLE_DYNAMIC_UI"
                        else -> "HTTP_$code"
                    }
                    val message = when {
                        code in 200..299 && discovered.isNotEmpty() ->
                            "Portal online (HTTP $code). Found ${discovered.size} direct MOE PDF links."
                        code in 200..299 ->
                            "MOE portal online (HTTP $code). Interactive dropdown portal detected; use direct PDF URL or Admin upload for specific files."
                        else ->
                            "MOE portal returned HTTP $code. Use Admin manual PDF upload fallback."
                    }

                    MoePortalCheckResult(
                        portalUrl = portalUrl,
                        robotsAllowed = true,
                        httpCode = code,
                        discoveredPdfLinks = discovered,
                        statusLabel = statusLabel,
                        message = message
                    )
                }
            } catch (e: Exception) {
                Log.w("MoeSync", "MOE portal check failed for $portalUrl: ${e.message}")
                MoePortalCheckResult(
                    portalUrl = portalUrl,
                    robotsAllowed = true,
                    httpCode = 0,
                    discoveredPdfLinks = emptyList(),
                    statusLabel = "NETWORK_UNAVAILABLE",
                    message = "Unable to reach $portalUrl (${e.localizedMessage ?: "network error"})."
                )
            }
        }

    suspend fun downloadOfficialPdf(pdfUrl: String, fileKey: String): Result<DownloadedPdfInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(isOfficialMoeUrl(pdfUrl)) { "URL must belong to an official *.moe.gov.eg domain." }
                val request = Request.Builder()
                    .url(pdfUrl)
                    .header("User-Agent", USER_AGENT)
                    .build()
                client.newCall(request).execute().use { response ->
                    require(response.isSuccessful) { "MOE server returned HTTP ${response.code}" }
                    val bytes = response.body?.bytes() ?: error("Empty PDF response")
                    require(bytes.size >= 4 && String(bytes.take(4).toByteArray()) == "%PDF") {
                        "Downloaded file is not a valid PDF document."
                    }
                    saveBytesLocally(bytes, fileKey)
                }
            }
        }

    suspend fun importLocalPdfFromUri(uri: Uri, fileKey: String): Result<DownloadedPdfInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Unable to open selected PDF file.")
                require(bytes.size >= 4 && String(bytes.take(4).toByteArray()) == "%PDF") {
                    "Selected file is not a valid PDF (%PDF header missing)."
                }
                saveBytesLocally(bytes, fileKey)
            }
        }

    fun listLocalPdfs(): List<File> =
        storageDir.listFiles { file -> file.extension.equals("pdf", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

    fun getLocalPdfFile(fileKey: String): File? {
        val safeKey = fileKey.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val f = File(storageDir, "$safeKey.pdf")
        return if (f.exists() && f.length() > 0L) f else null
    }

    suspend fun renderPdfPage(file: File, pageIndex: Int): Result<Pair<Bitmap, Int>> =
        withContext(Dispatchers.IO) {
            runCatching {
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        val count = renderer.pageCount
                        require(count > 0) { "PDF has 0 pages." }
                        val clampedIndex = pageIndex.coerceIn(0, count - 1)
                        renderer.openPage(clampedIndex).use { page ->
                            val targetWidth = 1080
                            val targetHeight = (targetWidth * page.height.toFloat() / page.width.toFloat())
                                .toInt()
                                .coerceAtLeast(100)
                            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bitmap to count
                        }
                    }
                }
            }
        }

    private fun saveBytesLocally(bytes: ByteArray, fileKey: String): DownloadedPdfInfo {
        val sha256 = sha256Hex(bytes)
        val safeKey = fileKey.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val outFile = File(storageDir, "$safeKey.pdf")
        outFile.writeBytes(bytes)
        val pageCount = ParcelFileDescriptor.open(outFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { it.pageCount }
        }
        return DownloadedPdfInfo(
            localFile = outFile,
            sha256 = sha256,
            sizeBytes = bytes.size.toLong(),
            pageCount = pageCount
        )
    }

    private fun checkRobotsTxt(targetUrl: String): Boolean {
        return try {
            val uri = java.net.URI(targetUrl)
            val robotsUrl = "${uri.scheme}://${uri.host}/robots.txt"
            val req = Request.Builder().url(robotsUrl).header("User-Agent", USER_AGENT).build()
            client.newCall(req).execute().use { res ->
                if (!res.isSuccessful) return true
                val lines = res.body?.string()?.lines().orEmpty()
                var appliesToAll = false
                for (raw in lines) {
                    val line = raw.substringBefore('#').trim()
                    if (line.startsWith("User-agent:", ignoreCase = true)) {
                        val agent = line.substringAfter(':').trim()
                        appliesToAll = agent == "*"
                    } else if (appliesToAll && line.startsWith("Disallow:", ignoreCase = true)) {
                        val disallowed = line.substringAfter(':').trim()
                        if (disallowed == "/" || (disallowed.isNotEmpty() && uri.path.startsWith(disallowed))) {
                            return false
                        }
                    }
                }
                true
            }
        } catch (e: Exception) {
            true
        }
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val USER_AGENT = "SmartStudyEgyptEduApp/1.0 (+https://moe.gov.eg)"
    }
}
