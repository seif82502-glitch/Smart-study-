package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.AiConversation
import com.example.domain.LessonProgress
import com.example.domain.LessonSpec
import com.example.domain.OfficialAssessmentItem
import com.example.domain.OfficialEgyptianCurriculumCatalog
import com.example.domain.PracticeExamRecord
import com.example.domain.SavedSummary
import com.example.domain.SubjectSpec
import com.example.ui.theme.DesertTeal
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.EmeraldVerifiedBg
import com.example.ui.theme.PapyrusGold
import com.example.ui.viewmodel.SmartStudyUiPreferences

@Composable
fun DashboardScreen(
    prefs: SmartStudyUiPreferences,
    progressList: List<LessonProgress>,
    savedSummaries: List<SavedSummary>,
    conversations: List<AiConversation>,
    practiceExams: List<PracticeExamRecord>,
    onSelectGradeAndTrack: (String, String) -> Unit,
    onOpenSubject: (SubjectSpec) -> Unit,
    onOpenLesson: (SubjectSpec, LessonSpec) -> Unit,
    onOpenAssessments: () -> Unit,
    onOpenAiTutor: () -> Unit,
    onDeleteSummary: (String) -> Unit
) {
    val isArabic = prefs.isArabic
    val currentGrade = OfficialEgyptianCurriculumCatalog.findGrade(prefs.selectedGradeId)
    val currentTrack = currentGrade.tracks.firstOrNull { it.id == prefs.selectedTrackId } ?: currentGrade.tracks.first()
    val gradeSubjects = OfficialEgyptianCurriculumCatalog.subjectsFor(currentGrade.id, currentTrack.id)
    val gradeLessons = gradeSubjects.flatMap { sub ->
        sub.units.flatMap { u -> u.chapters.flatMap { c -> c.lessons.map { l -> sub to l } } }
    }
    val completedCount = progressList.count { it.completed && it.gradeId == currentGrade.id }
    val totalLessons = gradeLessons.size.coerceAtLeast(1)
    val progressRatio = (completedCount.toFloat() / totalLessons.toFloat()).coerceIn(0f, 1f)
    val weakTopics = progressList.filter { it.weakTopic && it.gradeId == currentGrade.id }
    val upcomingAssessments = OfficialEgyptianCurriculumCatalog.officialAssessments
        .filter { it.gradeId == currentGrade.id }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Student Welcome Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_egypt_edu),
                        contentDescription = if (isArabic) "خلفية لوحة الطالب" else "Student dashboard banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentScale = ContentScale.Crop,
                        alpha = 0.28f
                    )
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "${if (isArabic) "العام الدراسي" else "Academic Year"} ${prefs.academicYear}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = EmeraldVerifiedBg,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = if (isArabic) "منهج رسمي" else "MOE Verified",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldVerified,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = if (isArabic) currentGrade.nameAr else currentGrade.nameEn,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) currentTrack.nameAr else currentTrack.nameEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isArabic) "تقدم المذاكرة ($completedCount / ${gradeLessons.size} دروس)" else "Study Progress ($completedCount / ${gradeLessons.size} lessons)",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "${(progressRatio * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                }
            }
        }

        // 2. Quick Grade & Track Switcher
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isArabic) "اختر الصف الدراسي والمسار" else "Select Grade & Academic Track",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OfficialEgyptianCurriculumCatalog.grades.forEach { grade ->
                        FilterChip(
                            selected = grade.id == currentGrade.id,
                            onClick = { onSelectGradeAndTrack(grade.id, grade.tracks.first().id) },
                            label = {
                                Text(
                                    text = if (isArabic) grade.nameAr else grade.nameEn,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.testTag("grade_chip_${grade.id}")
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentGrade.tracks.forEach { track ->
                        FilterChip(
                            selected = track.id == currentTrack.id,
                            onClick = { onSelectGradeAndTrack(currentGrade.id, track.id) },
                            label = { Text(if (isArabic) track.nameAr else track.nameEn) },
                            modifier = Modifier.testTag("track_chip_${track.id}")
                        )
                    }
                }
            }
        }

        // 3. Study Statistics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    tint = EmeraldVerified,
                    value = "$completedCount",
                    label = if (isArabic) "دروس مكتملة" else "Completed"
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Bookmark,
                    tint = PapyrusGold,
                    value = "${savedSummaries.size}",
                    label = if (isArabic) "ملخصات محفوظة" else "Saved Notes"
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    tint = DesertTeal,
                    value = if (practiceExams.isEmpty()) "—" else "${practiceExams.map { it.score }.average().toInt()}%",
                    label = if (isArabic) "متوسط التدريب" else "Avg Quiz"
                )
            }
        }

        // 4. Subjects for Selected Grade
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "المواد الدراسية" else "Your Subjects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onOpenAiTutor) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isArabic) "اسأل المعلم الذكي" else "Ask AI Tutor")
                }
            }
        }

        items(gradeSubjects, key = { it.id }) { subject ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSubject(subject) }
                    .testTag("dashboard_subject_${subject.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) subject.nameAr else subject.nameEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        val langBadge = buildString {
                            if (subject.hasOfficialArabic) append(if (isArabic) "نسخة عربية رسمية" else "Official AR")
                            if (subject.hasOfficialArabic && subject.hasOfficialEnglish) append(" • ")
                            if (subject.hasOfficialEnglish) append(if (isArabic) "نسخة إنجليزية رسمية" else "Official EN")
                        }
                        Text(
                            text = langBadge,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${subject.units.sumOf { u -> u.chapters.sumOf { c -> c.lessons.size } }} ${if (isArabic) "دروس" else "Lessons"}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 5. Recommended Lessons & Weak Topics
        if (weakTopics.isNotEmpty() || gradeLessons.isNotEmpty()) {
            item {
                Text(
                    text = if (isArabic) "نقاط تحتاج للمراجعة ودروس مقترحة" else "Weak Topics & Recommended Lessons",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (weakTopics.isNotEmpty()) {
                items(weakTopics.take(3), key = { "weak_${it.lessonId}" }) { weak ->
                    val match = gradeLessons.firstOrNull { it.second.id == weak.lessonId }
                    if (match != null) {
                        val (sub, les) = match
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenLesson(sub, les) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = PapyrusGold)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isArabic) les.titleAr else les.titleEn,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isArabic) "موصى بمراجعته • ${sub.nameAr}" else "Needs revision • ${sub.nameEn}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                val firstPair = gradeLessons.firstOrNull()
                if (firstPair != null) {
                    item {
                        val (sub, les) = firstPair
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenLesson(sub, les) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isArabic) "ابدأ بالدرس الموصى به: ${les.titleAr}" else "Recommended Next: ${les.titleEn}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (isArabic) sub.nameAr else sub.nameEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Upcoming Official Assessments
        if (upcomingAssessments.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "تقييمات الوزارة القادمة" else "Upcoming MOE Assessments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onOpenAssessments) {
                        Text(if (isArabic) "عرض الكل" else "View All")
                    }
                }
            }
            items(upcomingAssessments.take(2), key = { it.id }) { item ->
                UpcomingAssessmentMiniCard(item = item, isArabic = isArabic, onClick = onOpenAssessments)
            }
        }

        // 7. Saved AI Summaries & Recent AI Conversations
        if (savedSummaries.isNotEmpty()) {
            item {
                Text(
                    text = if (isArabic) "الملخصات المحفوظة" else "Saved Summaries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(savedSummaries.take(4), key = { it.id }) { sum ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sum.lessonTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${sum.subjectName} • ${sum.summaryType}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onDeleteSummary(sum.id) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = if (isArabic) "حذف الملخص" else "Delete summary"
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = sum.content,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (conversations.isNotEmpty()) {
            item {
                Text(
                    text = if (isArabic) "أحدث محادثات المعلم الذكي" else "Recent AI Tutor Conversations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(conversations.take(3), key = { it.id }) { conv ->
                AssistChip(
                    onClick = onOpenAiTutor,
                    label = { Text("${conv.title} — ${conv.lastMessage.take(60)}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun UpcomingAssessmentMiniCard(
    item: OfficialAssessmentItem,
    isArabic: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isArabic) item.titleAr else item.titleEn,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${if (isArabic) item.periodLabelAr else item.periodLabelEn} • ${item.publishedDateLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
