package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.ContentOrigin
import com.example.domain.CurriculumVersionRecord
import com.example.domain.LessonProgress
import com.example.domain.LessonSpec
import com.example.domain.OfficialEgyptianCurriculumCatalog
import com.example.domain.SearchHit
import com.example.domain.SubjectSpec
import com.example.ui.theme.AmberAiBadge
import com.example.ui.theme.AmberAiBadgeBg
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.EmeraldVerifiedBg
import com.example.ui.viewmodel.SmartStudyUiPreferences
import java.io.File

@Composable
fun CurriculumScreen(
    prefs: SmartStudyUiPreferences,
    selectedSubject: SubjectSpec?,
    selectedLesson: LessonSpec?,
    searchQuery: String,
    searchResults: List<SearchHit>,
    progressList: List<LessonProgress>,
    publishedCloudVersions: List<CurriculumVersionRecord>,
    localPdfFiles: List<File>,
    onSearchChange: (String) -> Unit,
    onSelectGradeAndTrack: (String, String) -> Unit,
    onSelectSemester: (Int) -> Unit,
    onSelectSubject: (SubjectSpec?) -> Unit,
    onSelectLesson: (LessonSpec?) -> Unit,
    onToggleCompleted: (LessonSpec, SubjectSpec, Boolean) -> Unit,
    onFlagWeakTopic: (LessonSpec, SubjectSpec, Boolean) -> Unit,
    onAskAiForLesson: (SubjectSpec, LessonSpec) -> Unit,
    onOpenMoePortal: (String) -> Unit,
    onOpenLocalPdf: (File) -> Unit
) {
    val isArabic = prefs.isArabic
    val context = LocalContext.current

    if (selectedLesson != null && selectedSubject != null) {
        BackHandler { onSelectLesson(null) }
        LessonDetailPane(
            isArabic = isArabic,
            subject = selectedSubject,
            lesson = selectedLesson,
            progress = progressList.firstOrNull { it.lessonId == selectedLesson.id },
            onBack = { onSelectLesson(null) },
            onToggleCompleted = { done -> onToggleCompleted(selectedLesson, selectedSubject, done) },
            onFlagWeak = { weak -> onFlagWeakTopic(selectedLesson, selectedSubject, weak) },
            onAskAi = { onAskAiForLesson(selectedSubject, selectedLesson) },
            onOpenMoePortal = { onOpenMoePortal(selectedLesson.officialSourceUrl) }
        )
        return
    }

    val currentGrade = OfficialEgyptianCurriculumCatalog.findGrade(prefs.selectedGradeId)
    val subjects = OfficialEgyptianCurriculumCatalog.subjectsFor(
        gradeId = currentGrade.id,
        trackId = prefs.selectedTrackId,
        semester = prefs.selectedSemester
    ).ifEmpty {
        OfficialEgyptianCurriculumCatalog.subjectsFor(currentGrade.id, null)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bilingual Curriculum Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("curriculum_search_input"),
                placeholder = {
                    Text(
                        if (isArabic) "ابحث في الدروس، القوانين، التعريفات، والتقييمات..."
                        else "Search lessons, formulas, definitions, assessments..."
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = if (isArabic) "مسح البحث" else "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (searchQuery.isNotBlank()) {
            item {
                Text(
                    text = if (isArabic) "نتائج البحث (${searchResults.size})" else "Search Results (${searchResults.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (searchResults.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isArabic) "لم يتم العثور على نتائج مطابقة في المنهج." else "No matching curriculum results found.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(searchResults, key = { "${it.kind}_${it.id}" }) { hit ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val sub = OfficialEgyptianCurriculumCatalog.subjects.firstOrNull { it.id == hit.subjectId }
                                if (hit.lesson != null && sub != null) {
                                    onSelectSubject(sub)
                                    onSelectLesson(hit.lesson)
                                } else if (hit.assessment != null) {
                                    onOpenMoePortal(hit.assessment.sourceUrl)
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isArabic) hit.titleAr else hit.titleEn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isArabic) hit.subtitleAr else hit.subtitleEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            return@LazyColumn
        }

        // Grade & Semester Filters
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OfficialEgyptianCurriculumCatalog.grades.forEach { g ->
                    FilterChip(
                        selected = g.id == currentGrade.id,
                        onClick = { onSelectGradeAndTrack(g.id, g.tracks.first().id) },
                        label = { Text(if (isArabic) g.nameAr else g.nameEn) }
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = prefs.selectedSemester == 1,
                    onClick = { onSelectSemester(1) },
                    label = { Text(if (isArabic) "الفصل الدراسي الأول" else "Semester 1") },
                    modifier = Modifier.testTag("semester_1_chip")
                )
                FilterChip(
                    selected = prefs.selectedSemester == 2,
                    onClick = { onSelectSemester(2) },
                    label = { Text(if (isArabic) "الفصل الدراسي الثاني" else "Semester 2") },
                    modifier = Modifier.testTag("semester_2_chip")
                )
            }
        }

        // Official Locally Saved PDFs (if any downloaded or imported)
        if (localPdfFiles.isNotEmpty()) {
            item {
                Text(
                    text = if (isArabic) "الكتب والملفات المحفوظة محليًا (PDF)" else "Locally Stored Official PDFs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(localPdfFiles, key = { it.name }) { file ->
                OutlinedButton(
                    onClick = { onOpenLocalPdf(file) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("${file.name} (${file.length() / 1024} KB)")
                }
            }
        }

        // Subject -> Unit -> Chapter -> Lesson Tree
        items(subjects, key = { it.id }) { subject ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) subject.nameAr else subject.nameEn,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${if (isArabic) "بوابة الوزارة" else "MOE Portal"} • ${subject.publicationYear}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onOpenMoePortal(subject.officialMoePortalUrl) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = if (isArabic) "فتح موقع الوزارة" else "Open MOE website"
                            )
                        }
                    }

                    // Language Official Availability Badge
                    if (!isArabic && !subject.hasOfficialEnglish) {
                        OriginBadge(origin = ContentOrigin.AI_TRANSLATED, isArabic = false)
                    } else {
                        OriginBadge(origin = ContentOrigin.OFFICIAL_MOE, isArabic = isArabic)
                    }

                    subject.units.forEach { unit ->
                        Text(
                            text = if (isArabic) unit.titleAr else unit.titleEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        unit.chapters.forEach { chapter ->
                            Text(
                                text = if (isArabic) chapter.titleAr else chapter.titleEn,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            chapter.lessons.forEach { lesson ->
                                val done = progressList.any { it.lessonId == lesson.id && it.completed }
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectSubject(subject)
                                            onSelectLesson(lesson)
                                        }
                                        .testTag("lesson_item_${lesson.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (done) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = if (done) EmeraldVerified else MaterialTheme.colorScheme.primary
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isArabic) lesson.titleAr else lesson.titleEn,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "${lesson.objectivesAr.size} ${if (isArabic) "أهداف تعلم" else "Objectives"} • ${lesson.publicationDate}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Show any Admin-approved Cloud Curriculum Updates for this subject
                    val cloudUpdates = publishedCloudVersions.filter { it.subjectId == subject.id && it.status == "PUBLISHED" }
                    cloudUpdates.forEach { ver ->
                        Surface(
                            color = EmeraldVerifiedBg,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${if (isArabic) "تحديث معتمد من الوزارة" else "Approved MOE Update"} v${ver.versionNumber}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = EmeraldVerified,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = ver.extractedUnitsSummary.take(280),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonDetailPane(
    isArabic: Boolean,
    subject: SubjectSpec,
    lesson: LessonSpec,
    progress: LessonProgress?,
    onBack: () -> Unit,
    onToggleCompleted: (Boolean) -> Unit,
    onFlagWeak: (Boolean) -> Unit,
    onAskAi: () -> Unit,
    onOpenMoePortal: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isDone = progress?.completed == true
    val isWeak = progress?.weakTopic == true
    val tabs = if (isArabic) {
        listOf("الشرح", "الأهداف", "القوانين", "التدريبات", "المراجعة")
    } else {
        listOf("Lesson", "Objectives", "Formulas", "Exercises", "Revision")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("lesson_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (isArabic) "رجوع" else "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isArabic) lesson.titleAr else lesson.titleEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isArabic) subject.nameAr else subject.nameEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onToggleCompleted(isDone) }, modifier = Modifier.testTag("toggle_lesson_done")) {
                Icon(
                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = if (isArabic) "إتمام الدرس" else "Mark completed",
                    tint = if (isDone) EmeraldVerified else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onFlagWeak(!isWeak) }, modifier = Modifier.testTag("toggle_lesson_weak")) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = if (isArabic) "يحتاج مراجعة" else "Flag weak topic",
                    tint = if (isWeak) AmberAiBadge else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OriginBadge(
            origin = if (isArabic) ContentOrigin.OFFICIAL_MOE else lesson.englishOrigin,
            isArabic = isArabic,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(6.dp))

        PrimaryTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(label, maxLines = 1) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isArabic) lesson.contentAr else lesson.contentEn,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${if (isArabic) "المصدر الرسمي:" else "Official Source:"} ${lesson.officialSourceUrl} (${lesson.publicationDate})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                1 -> items(if (isArabic) lesson.objectivesAr else lesson.objectivesEn) { obj ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "• $obj", modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                2 -> items(if (isArabic) lesson.formulasAndDefinitionsAr else lesson.formulasAndDefinitionsEn) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(text = item, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                }
                3 -> items(if (isArabic) lesson.exercisesAr else lesson.exercisesEn) { ex ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(text = ex, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                else -> item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Text(
                            text = if (isArabic) lesson.revisionSummaryAr else lesson.revisionSummaryEn,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAskAi,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lesson_ask_ai_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isArabic) "تلخيص وشرح بالذكاء الاصطناعي" else "AI Summary & Tutor")
                    }
                    OutlinedButton(
                        onClick = onOpenMoePortal,
                        modifier = Modifier.testTag("lesson_open_moe_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isArabic) "كتاب الوزارة" else "MOE Book")
                    }
                }
            }
        }
    }
}

@Composable
fun OriginBadge(
    origin: ContentOrigin,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val isOfficial = origin == ContentOrigin.OFFICIAL_MOE
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (isOfficial) EmeraldVerifiedBg else AmberAiBadgeBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isOfficial) Icons.Default.Verified else Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = if (isOfficial) EmeraldVerified else AmberAiBadge,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (isArabic) origin.labelAr else origin.labelEn,
                style = MaterialTheme.typography.labelSmall,
                color = if (isOfficial) EmeraldVerified else AmberAiBadge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
