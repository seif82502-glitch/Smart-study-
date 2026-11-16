package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.AiMode
import com.example.domain.GeneratedPracticeExam
import com.example.domain.LessonSpec
import com.example.domain.OfficialEgyptianCurriculumCatalog
import com.example.domain.SubjectSpec
import com.example.domain.SummaryType
import com.example.ui.theme.AmberAiBadge
import com.example.ui.theme.AmberAiBadgeBg
import com.example.ui.viewmodel.AiTextState
import com.example.ui.viewmodel.SmartStudyUiPreferences
import com.example.ui.viewmodel.TutorChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTutorScreen(
    prefs: SmartStudyUiPreferences,
    aiMode: AiMode,
    selectedSubject: SubjectSpec?,
    selectedLesson: LessonSpec?,
    aiTextState: AiTextState,
    tutorMessages: List<TutorChatMessage>,
    tutorStreaming: Boolean,
    problemImage: Bitmap?,
    generatedExam: GeneratedPracticeExam?,
    examLoading: Boolean,
    examError: String?,
    onSelectMode: (AiMode) -> Unit,
    onSelectSubject: (SubjectSpec?) -> Unit,
    onSelectLesson: (LessonSpec?) -> Unit,
    onGenerateSummary: (SummaryType) -> Unit,
    onSaveSummary: (SummaryType) -> Unit,
    onPickProblemImage: (android.net.Uri) -> Unit,
    onClearProblemImage: () -> Unit,
    onSolveProblem: (String) -> Unit,
    onSendTutorMessage: (String) -> Unit,
    onGeneratePracticeExam: (String, Int, List<String>) -> Unit,
    onSaveExamScore: (Int) -> Unit
) {
    val isArabic = prefs.isArabic
    val subjects = OfficialEgyptianCurriculumCatalog.subjectsFor(prefs.selectedGradeId, prefs.selectedTrackId)
        .ifEmpty { OfficialEgyptianCurriculumCatalog.subjectsFor(prefs.selectedGradeId, null) }
    val activeSubject = selectedSubject ?: subjects.firstOrNull()
    val lessons = activeSubject?.units?.flatMap { u -> u.chapters.flatMap { c -> c.lessons } }.orEmpty()
    val activeLesson = selectedLesson ?: lessons.firstOrNull()

    val modeTabs = if (isArabic) {
        listOf("ملخصات", "حل المسائل", "المعلم الذكي", "مولّد الامتحان")
    } else {
        listOf("Summarizer", "Solver", "AI Tutor", "Exam Lab")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = aiMode.ordinal) {
            AiMode.entries.forEachIndexed { idx, mode ->
                Tab(
                    selected = aiMode == mode,
                    onClick = { onSelectMode(mode) },
                    text = { Text(modeTabs[idx], maxLines = 1) },
                    modifier = Modifier.testTag("ai_mode_tab_${mode.name}")
                )
            }
        }

        // Context Selector Bar (Subject & Lesson for RAG grounding)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subjects.forEach { sub ->
                FilterChip(
                    selected = sub.id == activeSubject?.id,
                    onClick = { onSelectSubject(sub) },
                    label = { Text(if (isArabic) sub.nameAr else sub.nameEn, maxLines = 1) }
                )
            }
        }

        if (lessons.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                lessons.forEach { les ->
                    FilterChip(
                        selected = les.id == activeLesson?.id,
                        onClick = { onSelectLesson(les) },
                        label = { Text(if (isArabic) les.titleAr else les.titleEn, maxLines = 1) }
                    )
                }
            }
        }

        when (aiMode) {
            AiMode.SUMMARIZER -> SummarizerPane(
                isArabic = isArabic,
                aiTextState = aiTextState,
                onGenerateSummary = onGenerateSummary,
                onSaveSummary = onSaveSummary
            )
            AiMode.PROBLEM_SOLVER -> ProblemSolverPane(
                isArabic = isArabic,
                aiTextState = aiTextState,
                problemImage = problemImage,
                onPickImage = onPickProblemImage,
                onClearImage = onClearProblemImage,
                onSolveProblem = onSolveProblem
            )
            AiMode.STUDY_TUTOR -> TutorChatPane(
                isArabic = isArabic,
                messages = tutorMessages,
                isStreaming = tutorStreaming,
                onSendMessage = onSendTutorMessage
            )
            AiMode.EXAM_GENERATOR -> ExamGeneratorPane(
                isArabic = isArabic,
                exam = generatedExam,
                isLoading = examLoading,
                error = examError,
                onGenerateExam = onGeneratePracticeExam,
                onSaveScore = onSaveExamScore
            )
        }
    }
}

@Composable
private fun SummarizerPane(
    isArabic: Boolean,
    aiTextState: AiTextState,
    onGenerateSummary: (SummaryType) -> Unit,
    onSaveSummary: (SummaryType) -> Unit
) {
    var selectedType by remember { mutableStateOf(SummaryType.SHORT) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(if (isArabic) type.labelAr else type.labelEn) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onGenerateSummary(selectedType) },
                enabled = aiTextState !is AiTextState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_summary_button")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isArabic) "توليد الملخص من المنهج الرسمي" else "Generate Grounded Lesson Summary")
            }
        }

        item {
            AiResponseCard(
                isArabic = isArabic,
                state = aiTextState,
                extraAction = {
                    if (aiTextState is AiTextState.Success) {
                        OutlinedButton(
                            onClick = { onSaveSummary(selectedType) },
                            modifier = Modifier.testTag("save_summary_button")
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isArabic) "حفظ في لوحة الطالب" else "Save to Dashboard")
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ProblemSolverPane(
    isArabic: Boolean,
    aiTextState: AiTextState,
    problemImage: Bitmap?,
    onPickImage: (android.net.Uri) -> Unit,
    onClearImage: () -> Unit,
    onSolveProblem: (String) -> Unit
) {
    var problemInput by remember { mutableStateOf("") }
    // Zero-permission Android Photo Picker (Play Policy compliant)
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onPickImage(uri)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = problemInput,
                onValueChange = { problemInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("problem_input_field"),
                minLines = 3,
                label = {
                    Text(
                        if (isArabic) "اكتب مسألة الرياضيات أو العلوم أو القواعد..."
                        else "Type math, science, or grammar problem..."
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(Modifier.height(8.dp))

            if (problemImage != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = problemImage.asImageBitmap(),
                            contentDescription = if (isArabic) "صورة التمرين المرفقة" else "Attached problem image",
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (isArabic) "تم إرفاق صورة التمرين للتحليل" else "Exercise image ready for Gemini Vision",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        IconButton(onClick = onClearImage) {
                            Icon(Icons.Default.Close, contentDescription = if (isArabic) "إزالة الصورة" else "Remove image")
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pick_problem_image_button")
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isArabic) "رفع صورة التمرين" else "Upload Photo")
                }
                Button(
                    onClick = { onSolveProblem(problemInput) },
                    enabled = aiTextState !is AiTextState.Loading,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("solve_problem_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isArabic) "حل بالخطوات" else "Solve Step-by-Step")
                }
            }
        }

        item {
            AiResponseCard(isArabic = isArabic, state = aiTextState)
        }
    }
}

@Composable
private fun TutorChatPane(
    isArabic: Boolean,
    messages: List<TutorChatMessage>,
    isStreaming: Boolean,
    onSendMessage: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isArabic) {
                                "مرحباً بك في المعلم الذكي! اسأل أي سؤال عن الدرس المختار وسأشرحه لك بأسلوب مبسط مع ذكر المصدر من المنهج المصري."
                            } else {
                                "Welcome to your Egyptian Curriculum AI Tutor! Ask any question about the selected lesson for a grounded explanation and follow-up quiz."
                            },
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            items(messages) { msg ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (msg.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    tonalElevation = if (msg.isUser) 0.dp else 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (msg.isUser) (if (isArabic) "أنت" else "You") else (if (isArabic) "المعلم الذكي" else "AI Study Tutor"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(msg.text, style = MaterialTheme.typography.bodyMedium)
                        if (!msg.isUser && msg.citations.isNotEmpty()) {
                            Text(
                                text = "${if (isArabic) "المراجع:" else "Citations:"} ${msg.citations.joinToString(" | ")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tutor_chat_input"),
                placeholder = {
                    Text(if (isArabic) "اكتب سؤالك هنا..." else "Ask a curriculum question...")
                },
                maxLines = 3,
                shape = RoundedCornerShape(16.dp)
            )
            Button(
                onClick = {
                    val text = input.trim()
                    if (text.isNotEmpty()) {
                        input = ""
                        onSendMessage(text)
                    }
                },
                enabled = !isStreaming && input.isNotBlank(),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("tutor_send_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = if (isArabic) "إرسال" else "Send")
            }
        }
    }
}

@Composable
private fun ExamGeneratorPane(
    isArabic: Boolean,
    exam: GeneratedPracticeExam?,
    isLoading: Boolean,
    error: String?,
    onGenerateExam: (String, Int, List<String>) -> Unit,
    onSaveScore: (Int) -> Unit
) {
    var difficulty by remember { mutableStateOf("Medium") }
    var count by remember { mutableIntStateOf(5) }
    val selectedAnswers = remember(exam) { mutableStateMapOf<Int, String>() }
    var revealAnswers by remember(exam) { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                color = AmberAiBadgeBg,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isArabic) {
                        "تنبيه: الامتحانات المولدة هنا للتدريب بالذكاء الاصطناعي وليست امتحانات رسمية من وزارة التربية والتعليم."
                    } else {
                        "Notice: Generated quizzes are AI Practice Exams and NOT official Ministry of Education assessments."
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = AmberAiBadge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Easy", "Medium", "Hard").forEach { diff ->
                    FilterChip(
                        selected = difficulty == diff,
                        onClick = { difficulty = diff },
                        label = { Text(diff) }
                    )
                }
                listOf(3, 5, 8).forEach { qCount ->
                    FilterChip(
                        selected = count == qCount,
                        onClick = { count = qCount },
                        label = { Text("$qCount Q") }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    onGenerateExam(difficulty, count, listOf("MCQ", "TRUE_FALSE", "COMPLETE", "WRITTEN"))
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_exam_button")
            ) {
                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isLoading) (if (isArabic) "جاري إعداد الامتحان..." else "Generating Exam...")
                    else (if (isArabic) "توليد امتحان تدريبي جديد" else "Generate AI Practice Exam")
                )
            }
        }

        if (error != null) {
            item {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                    Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp))
                }
            }
        }

        if (exam != null) {
            item {
                Text(
                    text = "${exam.title} (${exam.difficulty})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(exam.questions, key = { it.number }) { q ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "${q.number}. [${q.type}] ${q.prompt}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (q.options.isNotEmpty()) {
                            q.options.forEach { opt ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = selectedAnswers[q.number] == opt,
                                        onClick = { selectedAnswers[q.number] = opt }
                                    )
                                    Text(opt, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = selectedAnswers[q.number].orEmpty(),
                                onValueChange = { selectedAnswers[q.number] = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(if (isArabic) "اكتب إجابتك..." else "Write your answer...") }
                            )
                        }

                        if (revealAnswers) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "${if (isArabic) "الإجابة الصحيحة:" else "Answer Key:"} ${q.correctAnswer}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = q.explanation,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            revealAnswers = true
                            val mcqTotal = exam.questions.count { it.options.isNotEmpty() }.coerceAtLeast(1)
                            val correct = exam.questions.count {
                                it.options.isNotEmpty() && selectedAnswers[it.number]?.trim().equals(it.correctAnswer.trim(), ignoreCase = true)
                            }
                            onSaveScore((correct * 100) / mcqTotal)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_practice_exam_button")
                    ) {
                        Text(if (isArabic) "تصحيح وعرض الإجابات النموذجية" else "Grade & Show Answer Key")
                    }
                }
            }
        }
    }
}

@Composable
private fun AiResponseCard(
    isArabic: Boolean,
    state: AiTextState,
    extraAction: @Composable () -> Unit = {}
) {
    when (state) {
        AiTextState.Idle -> Unit
        AiTextState.Loading -> {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp))
                    Text(if (isArabic) "جاري التحليل بالذكاء الاصطناعي من المنهج..." else "Analyzing Egyptian curriculum with Gemini AI...")
                }
            }
        }
        is AiTextState.Streaming -> {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(state.text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        is AiTextState.Success -> {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(state.text, style = MaterialTheme.typography.bodyMedium)
                    if (state.citations.isNotEmpty()) {
                        Text(
                            text = "${if (isArabic) "المصادر المعتمدة:" else "Curriculum Citations:"}\n${state.citations.joinToString("\n• ", prefix = "• ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    extraAction()
                }
            }
        }
        is AiTextState.Error -> {
            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(14.dp)) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}
