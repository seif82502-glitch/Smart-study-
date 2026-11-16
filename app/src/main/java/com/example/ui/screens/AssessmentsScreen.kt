package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.MoePortalCheckResult
import com.example.domain.AssessmentType
import com.example.domain.LanguageCode
import com.example.domain.OfficialAssessmentItem
import com.example.domain.OfficialAssessmentRecord
import com.example.domain.OfficialEgyptianCurriculumCatalog
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.EmeraldVerifiedBg
import com.example.ui.viewmodel.SmartStudyUiPreferences

@Composable
fun AssessmentsScreen(
    prefs: SmartStudyUiPreferences,
    cloudAssessments: List<OfficialAssessmentRecord>,
    moeCheckResult: MoePortalCheckResult?,
    moeBusy: Boolean,
    onCheckMoeAssessmentsPortal: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onDownloadPdf: (String, String) -> Unit,
    onStartAiPracticeForAssessment: (OfficialAssessmentItem) -> Unit
) {
    val isArabic = prefs.isArabic
    var selectedGradeFilter by remember(prefs.selectedGradeId) { mutableStateOf<String?>(prefs.selectedGradeId) }
    var selectedTypeFilter by remember { mutableStateOf<AssessmentType?>(null) }

    val mergedAssessments = remember(cloudAssessments) {
        val cloudConverted = cloudAssessments.map { rec ->
            OfficialAssessmentItem(
                id = rec.id,
                titleAr = rec.titleAr,
                titleEn = rec.titleEn,
                gradeId = rec.gradeId,
                subjectId = rec.subjectId,
                subjectNameAr = OfficialEgyptianCurriculumCatalog.subjects.firstOrNull { it.id == rec.subjectId }?.nameAr ?: rec.subjectId,
                subjectNameEn = OfficialEgyptianCurriculumCatalog.subjects.firstOrNull { it.id == rec.subjectId }?.nameEn ?: rec.subjectId,
                academicYear = rec.academicYear,
                semester = rec.semester.toInt(),
                periodLabelAr = rec.periodLabel,
                periodLabelEn = rec.periodLabel,
                assessmentType = runCatching { AssessmentType.valueOf(rec.assessmentType) }.getOrDefault(AssessmentType.WEEKLY),
                language = if (rec.language == "EN") LanguageCode.EN else LanguageCode.AR,
                sourceUrl = rec.sourceUrl,
                pdfDirectUrl = if (rec.sourceUrl.endsWith(".pdf", ignoreCase = true)) rec.sourceUrl else null,
                publishedDateLabel = rec.publishedDateLabel,
                isOfficialVerified = rec.isOfficialVerified
            )
        }
        (cloudConverted + OfficialEgyptianCurriculumCatalog.officialAssessments)
            .distinctBy { it.id }
    }

    val filtered = mergedAssessments.filter { item ->
        (selectedGradeFilter == null || item.gradeId == selectedGradeFilter) &&
            (selectedTypeFilter == null || item.assessmentType == selectedTypeFilter)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldVerified)
                        Text(
                            text = "تقييمات وزارة التربية والتعليم — Official Ministry Assessments",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (isArabic) {
                            "التقييمات الأسبوعية والشهرية والنماذج الاسترشادية ومواصفات الورقة الامتحانية من البوابة الرسمية moe.gov.eg دون تعديل."
                        } else {
                            "Official weekly/monthly assessments, model exams, and exam specifications verified from moe.gov.eg with original source documents preserved."
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onCheckMoeAssessmentsPortal,
                            enabled = !moeBusy,
                            modifier = Modifier.testTag("sync_moe_assessments_button")
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (moeBusy) (if (isArabic) "جاري الفحص..." else "Checking...")
                                else (if (isArabic) "فحص تحديثات الوزارة" else "Check MOE Portal")
                            )
                        }
                        OutlinedButton(
                            onClick = { onOpenUrl(OfficialEgyptianCurriculumCatalog.MOE_ASSESSMENTS_PORTAL) },
                            modifier = Modifier.testTag("open_moe_assessments_portal")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isArabic) "الموقع الرسمي" else "MOE Portal")
                        }
                    }

                    if (moeCheckResult != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${moeCheckResult.statusLabel} (robots.txt: ${if (moeCheckResult.robotsAllowed) "Allowed" else "Blocked"})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(moeCheckResult.message, style = MaterialTheme.typography.bodySmall)
                                moeCheckResult.discoveredPdfLinks.take(5).forEach { pdfLink ->
                                    OutlinedButton(
                                        onClick = { onDownloadPdf(pdfLink, "moe_${pdfLink.hashCode()}") },
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(pdfLink.substringAfterLast('/'), maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Grade Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedGradeFilter == null,
                    onClick = { selectedGradeFilter = null },
                    label = { Text(if (isArabic) "كل الصفوف" else "All Grades") }
                )
                OfficialEgyptianCurriculumCatalog.grades.forEach { g ->
                    FilterChip(
                        selected = selectedGradeFilter == g.id,
                        onClick = { selectedGradeFilter = g.id },
                        label = { Text(if (isArabic) g.nameAr else g.nameEn) }
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text(if (isArabic) "كل الأنواع" else "All Types") }
                )
                AssessmentType.entries.forEach { t ->
                    FilterChip(
                        selected = selectedTypeFilter == t,
                        onClick = { selectedTypeFilter = t },
                        label = { Text(if (isArabic) t.labelAr else t.labelEn) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { item ->
            val grade = OfficialEgyptianCurriculumCatalog.findGrade(item.gradeId)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("assessment_card_${item.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = EmeraldVerifiedBg,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(14.dp))
                                Text(
                                    text = if (isArabic) "مصدر رسمي موثق (MOE)" else "Verified MOE Source",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldVerified,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = item.publishedDateLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = if (isArabic) item.titleAr else item.titleEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${if (isArabic) grade.nameAr else grade.nameEn} • ${if (isArabic) item.subjectNameAr else item.subjectNameEn} • ${if (isArabic) item.periodLabelAr else item.periodLabelEn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val samples = if (isArabic) item.sampleQuestionsAr else item.sampleQuestionsEn
                    if (samples.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isArabic) "نماذج من التقييم الأصلي:" else "Excerpt from Official Document:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                samples.forEach { q ->
                                    Text("• $q", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (item.pdfDirectUrl != null) {
                                    onDownloadPdf(item.pdfDirectUrl, item.id)
                                } else {
                                    onOpenUrl(item.sourceUrl)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isArabic) "عرض / تنزيل PDF" else "View / Download")
                        }
                        Button(
                            onClick = { onStartAiPracticeForAssessment(item) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isArabic) "تدريب تفاعلي (AI)" else "Interactive Practice")
                        }
                    }
                }
            }
        }
    }
}
