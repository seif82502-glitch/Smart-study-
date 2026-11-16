package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.MoePortalCheckResult
import com.example.domain.AdminAuditLogRecord
import com.example.domain.AssessmentType
import com.example.domain.CurriculumDiffRecord
import com.example.domain.CurriculumVersionRecord
import com.example.domain.MoeSyncLogRecord
import com.example.domain.OfficialEgyptianCurriculumCatalog
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.EmeraldVerifiedBg
import com.example.ui.viewmodel.SmartStudyUiPreferences
import java.io.File

@Composable
fun AdminPanelScreen(
    prefs: SmartStudyUiPreferences,
    isAdminUser: Boolean,
    allVersions: List<CurriculumVersionRecord>,
    diffs: List<CurriculumDiffRecord>,
    syncLogs: List<MoeSyncLogRecord>,
    auditLogs: List<AdminAuditLogRecord>,
    moeCheckResult: MoePortalCheckResult?,
    busy: Boolean,
    onCheckMoePortals: (String) -> Unit,
    onImportCurriculumPdf: (Uri, String, String, Int, String, String) -> Unit,
    onApproveVersion: (String, String?) -> Unit,
    onRejectVersion: (String, String?) -> Unit,
    onPublishAssessment: (String, String, String, String, Int, String, AssessmentType, String, String) -> Unit
) {
    val isArabic = prefs.isArabic
    val context = LocalContext.current

    if (!isAdminUser) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (isArabic) "لوحة الإدارة مقصورة على مسؤولي المنصة المعتمدين." else "Admin Console is restricted to authorized administrators.",
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    var targetGradeId by remember { mutableStateOf(prefs.selectedGradeId) }
    val subjectsForGrade = OfficialEgyptianCurriculumCatalog.subjectsFor(targetGradeId, null)
    var targetSubjectId by remember(targetGradeId) {
        mutableStateOf(subjectsForGrade.firstOrNull()?.id ?: "prep1_sci_en")
    }
    var targetSemester by remember { mutableIntStateOf(1) }
    var targetLang by remember { mutableStateOf("AR") }
    var sourceUrl by remember { mutableStateOf(OfficialEgyptianCurriculumCatalog.MOE_BOOKS_PORTAL) }

    var assessmentTitleAr by remember { mutableStateOf("") }
    var assessmentPeriod by remember { mutableStateOf("الأسبوع الثامن") }
    var assessmentType by remember { mutableStateOf(AssessmentType.WEEKLY) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportCurriculumPdf(uri, targetGradeId, targetSubjectId, targetSemester, targetLang, sourceUrl)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. MOE Monitoring & Portal Sync
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isArabic) "محرك مراقبة مناهج الوزارة (moe.gov.eg)" else "Smart MOE Curriculum Monitor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onCheckMoePortals(OfficialEgyptianCurriculumCatalog.MOE_BOOKS_PORTAL) },
                            enabled = !busy,
                            modifier = Modifier.testTag("admin_check_books_portal")
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isArabic) "فحص بوابة الكتب" else "Check Books Portal")
                        }
                        OutlinedButton(
                            onClick = { onCheckMoePortals(OfficialEgyptianCurriculumCatalog.MOE_ASSESSMENTS_PORTAL) },
                            enabled = !busy
                        ) {
                            Text(if (isArabic) "فحص التقييمات" else "Check Assessments")
                        }
                    }
                    if (moeCheckResult != null) {
                        Text(
                            text = "${moeCheckResult.statusLabel}: ${moeCheckResult.message}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // 2. Official Curriculum PDF Upload & AI Extraction -> PENDING_REVIEW
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isArabic) "رفع كتاب رسمي (PDF) واستخراج المنهج للمراجعة" else "Import Official PDF & Stage Version",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OfficialEgyptianCurriculumCatalog.grades.forEach { g ->
                            FilterChip(
                                selected = targetGradeId == g.id,
                                onClick = { targetGradeId = g.id },
                                label = { Text(if (isArabic) g.nameAr else g.nameEn) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjectsForGrade.forEach { sub ->
                            FilterChip(
                                selected = targetSubjectId == sub.id,
                                onClick = { targetSubjectId = sub.id },
                                label = { Text(if (isArabic) sub.nameAr else sub.nameEn) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = targetLang == "AR", onClick = { targetLang = "AR" }, label = { Text("AR") })
                        FilterChip(selected = targetLang == "EN", onClick = { targetLang = "EN" }, label = { Text("EN") })
                        FilterChip(selected = targetSemester == 1, onClick = { targetSemester = 1 }, label = { Text("Sem 1") })
                        FilterChip(selected = targetSemester == 2, onClick = { targetSemester = 2 }, label = { Text("Sem 2") })
                    }
                    OutlinedTextField(
                        value = sourceUrl,
                        onValueChange = { sourceUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (isArabic) "رابط المصدر الرسمي (moe.gov.eg)" else "Official Source URL") },
                        singleLine = true
                    )
                    Button(
                        onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                        enabled = !busy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_import_pdf_button")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isArabic) "اختيار ملف PDF وبدء الاستخراج" else "Pick Official PDF & Stage")
                    }
                }
            }
        }

        // 3. Staged Versions & Diff Approval Queue
        item {
            Text(
                text = if (isArabic) "نسخ المناهج وتقارير الفروقات (${allVersions.size})" else "Curriculum Versions & Diff Reports (${allVersions.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(allVersions, key = { it.id }) { ver ->
            val matchingDiff = diffs.firstOrNull { it.versionId == ver.id }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${ver.subjectId} • v${ver.versionNumber} (${ver.language})", fontWeight = FontWeight.Bold)
                        Surface(
                            color = if (ver.status == "PUBLISHED") EmeraldVerifiedBg else MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = ver.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (ver.status == "PUBLISHED") EmeraldVerified else MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (matchingDiff != null) {
                        Text(matchingDiff.summary, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(ver.extractedUnitsSummary.take(240), style = MaterialTheme.typography.bodySmall)
                    if (ver.status == "PENDING_REVIEW") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onApproveVersion(ver.id, matchingDiff?.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isArabic) "اعتماد ونشر" else "Approve & Publish")
                            }
                            OutlinedButton(
                                onClick = { onRejectVersion(ver.id, matchingDiff?.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isArabic) "رفض" else "Reject")
                            }
                        }
                    }
                }
            }
        }

        // 4. Publish Official MOE Assessment
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isArabic) "إضافة تقييم رسمي لوزارة التربية والتعليم" else "Publish Verified MOE Assessment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = assessmentTitleAr,
                        onValueChange = { assessmentTitleAr = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (isArabic) "عنوان التقييم" else "Assessment Title") }
                    )
                    OutlinedTextField(
                        value = assessmentPeriod,
                        onValueChange = { assessmentPeriod = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (isArabic) "الأسبوع أو الشهر" else "Week / Month Label") }
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(AssessmentType.WEEKLY, AssessmentType.MONTHLY, AssessmentType.MODEL_EXAM, AssessmentType.REVISION).forEach { t ->
                            FilterChip(
                                selected = assessmentType == t,
                                onClick = { assessmentType = t },
                                label = { Text(if (isArabic) t.labelAr else t.labelEn) }
                            )
                        }
                    }
                    Button(
                        onClick = {
                            if (assessmentTitleAr.isNotBlank()) {
                                onPublishAssessment(
                                    assessmentTitleAr,
                                    assessmentTitleAr,
                                    targetGradeId,
                                    targetSubjectId,
                                    targetSemester,
                                    assessmentPeriod,
                                    assessmentType,
                                    targetLang,
                                    OfficialEgyptianCurriculumCatalog.MOE_ASSESSMENTS_PORTAL
                                )
                                assessmentTitleAr = ""
                            }
                        },
                        enabled = assessmentTitleAr.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isArabic) "نشر التقييم الرسمي" else "Publish Official Assessment")
                    }
                }
            }
        }

        // 5. MOE Sync Logs & Audit Trail
        if (syncLogs.isNotEmpty() || auditLogs.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Text(
                        text = if (isArabic) "سجل المزامنة والمراجعة الإدارية" else "MOE Sync & Admin Audit Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            items(auditLogs.take(8), key = { it.id }) { log ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(log.action, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text(log.details, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            items(syncLogs.take(5), key = { it.id }) { sync ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${sync.status} — ${sync.checkedUrl}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(sync.details, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun NativePdfViewerDialog(
    file: File,
    bitmap: Bitmap?,
    pageIndex: Int,
    totalPages: Int,
    isArabic: Boolean,
    onNavigatePage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = if (isArabic) "إغلاق" else "Close")
                    }
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = if (isArabic) "صفحة الكتاب المدرسي" else "PDF page",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { onNavigatePage(-1) }, enabled = pageIndex > 0) {
                        Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null)
                        Text(if (isArabic) "السابق" else "Prev")
                    }
                    Text("${pageIndex + 1} / ${totalPages.coerceAtLeast(1)}", fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = { onNavigatePage(1) }, enabled = pageIndex + 1 < totalPages) {
                        Text(if (isArabic) "التالي" else "Next")
                        Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
                    }
                }
            }
        }
    }
}
