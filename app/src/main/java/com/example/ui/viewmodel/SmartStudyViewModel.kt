package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.DownloadedPdfInfo
import com.example.data.repository.MoePortalCheckResult
import com.example.data.repository.MoePortalSyncService
import com.example.data.repository.SmartStudyAiRepository
import com.example.data.repository.SmartStudyRepository
import com.example.domain.AdminAuditLogRecord
import com.example.domain.AiConversation
import com.example.domain.AiMode
import com.example.domain.AssessmentType
import com.example.domain.CurriculumDiffRecord
import com.example.domain.CurriculumSearchEngine
import com.example.domain.CurriculumVersionRecord
import com.example.domain.GeneratedPracticeExam
import com.example.domain.LessonProgress
import com.example.domain.LessonSpec
import com.example.domain.MoeSyncLogRecord
import com.example.domain.OfficialAssessmentItem
import com.example.domain.OfficialAssessmentRecord
import com.example.domain.OfficialEgyptianCurriculumCatalog
import com.example.domain.PracticeExamRecord
import com.example.domain.SavedSummary
import com.example.domain.SearchHit
import com.example.domain.SubjectSpec
import com.example.domain.SummaryType
import com.example.domain.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed interface AiTextState {
    data object Idle : AiTextState
    data object Loading : AiTextState
    data class Streaming(val text: String, val citations: List<String>) : AiTextState
    data class Success(val text: String, val citations: List<String>) : AiTextState
    data class Error(val message: String) : AiTextState
}

data class TutorChatMessage(
    val isUser: Boolean,
    val text: String,
    val citations: List<String> = emptyList()
)

data class SmartStudyUiPreferences(
    val isArabic: Boolean = true,
    val isDarkTheme: Boolean = false,
    val selectedGradeId: String = "prep_1",
    val selectedTrackId: String = "general",
    val selectedSemester: Int = 1,
    val academicYear: String = "2026-2027"
)

class SmartStudyViewModel(
    private val repository: SmartStudyRepository,
    private val aiRepository: SmartStudyAiRepository,
    private val moeSyncService: MoePortalSyncService,
    private val currentUserId: String
) : ViewModel() {

    private val currentUserEmail = Firebase.auth.currentUser?.email.orEmpty()
    private val currentUserName = Firebase.auth.currentUser?.displayName ?: "Egyptian Student"
    val isAdminUser: Boolean = currentUserEmail.equals("seif82502@gmail.com", ignoreCase = true)

    private val _prefs = MutableStateFlow(SmartStudyUiPreferences())
    val prefs: StateFlow<SmartStudyUiPreferences> = _prefs.asStateFlow()

    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile(currentUserId)
        .catch { e ->
            Log.w(TAG, "Profile stream error: ${e.message}")
            emit(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val lessonProgress: StateFlow<List<LessonProgress>> = repository.observeLessonProgress(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val savedSummaries: StateFlow<List<SavedSummary>> = repository.observeSavedSummaries(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val conversations: StateFlow<List<AiConversation>> = repository.observeConversations(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val practiceExams: StateFlow<List<PracticeExamRecord>> = repository.observePracticeExams(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val publishedCloudVersions: StateFlow<List<CurriculumVersionRecord>> =
        repository.observePublishedCurriculumVersions()
            .catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val cloudVerifiedAssessments: StateFlow<List<OfficialAssessmentRecord>> =
        repository.observeVerifiedAssessments()
            .catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Admin-only streams (only subscribed when user is verified admin)
    val adminAllVersions: StateFlow<List<CurriculumVersionRecord>> =
        (if (isAdminUser) repository.observeAllCurriculumVersionsForAdmin() else flowOf(emptyList()))
            .catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val adminDiffs: StateFlow<List<CurriculumDiffRecord>> =
        (if (isAdminUser) repository.observeCurriculumDiffsForAdmin() else flowOf(emptyList()))
            .catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val adminSyncLogs: StateFlow<List<MoeSyncLogRecord>> =
        (if (isAdminUser) repository.observeMoeSyncLogsForAdmin() else flowOf(emptyList()))
            .catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val adminAuditLogs: StateFlow<List<AdminAuditLogRecord>> =
        (if (isAdminUser) repository.observeAdminAuditLogs() else flowOf(emptyList()))
            .catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Selected Curriculum Context
    private val _selectedSubject = MutableStateFlow<SubjectSpec?>(null)
    val selectedSubject: StateFlow<SubjectSpec?> = _selectedSubject.asStateFlow()

    private val _selectedLesson = MutableStateFlow<LessonSpec?>(null)
    val selectedLesson: StateFlow<LessonSpec?> = _selectedLesson.asStateFlow()

    // Bilingual Search Engine State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchGradeFilter = MutableStateFlow<String?>(null)
    val searchGradeFilter: StateFlow<String?> = _searchGradeFilter.asStateFlow()

    private val _searchSemesterFilter = MutableStateFlow<Int?>(null)
    val searchSemesterFilter: StateFlow<Int?> = _searchSemesterFilter.asStateFlow()

    val searchResults: StateFlow<List<SearchHit>> = _searchQuery
        .map { q ->
            if (q.isBlank()) emptyList()
            else CurriculumSearchEngine.search(
                query = q,
                gradeIdFilter = _searchGradeFilter.value,
                semesterFilter = _searchSemesterFilter.value
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // AI Assistant State
    private val _aiMode = MutableStateFlow(AiMode.SUMMARIZER)
    val aiMode: StateFlow<AiMode> = _aiMode.asStateFlow()

    private val _aiTextState = MutableStateFlow<AiTextState>(AiTextState.Idle)
    val aiTextState: StateFlow<AiTextState> = _aiTextState.asStateFlow()

    private val _tutorMessages = MutableStateFlow<List<TutorChatMessage>>(emptyList())
    val tutorMessages: StateFlow<List<TutorChatMessage>> = _tutorMessages.asStateFlow()

    private val _tutorStreaming = MutableStateFlow(false)
    val tutorStreaming: StateFlow<Boolean> = _tutorStreaming.asStateFlow()

    private val _problemImage = MutableStateFlow<Bitmap?>(null)
    val problemImage: StateFlow<Bitmap?> = _problemImage.asStateFlow()

    private val _generatedExam = MutableStateFlow<GeneratedPracticeExam?>(null)
    val generatedExam: StateFlow<GeneratedPracticeExam?> = _generatedExam.asStateFlow()

    private val _examLoading = MutableStateFlow(false)
    val examLoading: StateFlow<Boolean> = _examLoading.asStateFlow()

    private val _examError = MutableStateFlow<String?>(null)
    val examError: StateFlow<String?> = _examError.asStateFlow()

    // MOE Sync & Local PDF Viewer State
    private val _moeCheckResult = MutableStateFlow<MoePortalCheckResult?>(null)
    val moeCheckResult: StateFlow<MoePortalCheckResult?> = _moeCheckResult.asStateFlow()

    private val _moeSyncBusy = MutableStateFlow(false)
    val moeSyncBusy: StateFlow<Boolean> = _moeSyncBusy.asStateFlow()

    private val _localPdfFiles = MutableStateFlow<List<File>>(moeSyncService.listLocalPdfs())
    val localPdfFiles: StateFlow<List<File>> = _localPdfFiles.asStateFlow()

    private val _activePdfFile = MutableStateFlow<File?>(null)
    val activePdfFile: StateFlow<File?> = _activePdfFile.asStateFlow()

    private val _activePdfBitmap = MutableStateFlow<Bitmap?>(null)
    val activePdfBitmap: StateFlow<Bitmap?> = _activePdfBitmap.asStateFlow()

    private val _activePdfPage = MutableStateFlow(0)
    val activePdfPage: StateFlow<Int> = _activePdfPage.asStateFlow()

    private val _activePdfTotalPages = MutableStateFlow(0)
    val activePdfTotalPages: StateFlow<Int> = _activePdfTotalPages.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    init {
        viewModelScope.launch {
            userProfile.collect { profile ->
                if (profile != null) {
                    _prefs.value = _prefs.value.copy(
                        isArabic = profile.preferredLang != "EN",
                        selectedGradeId = profile.selectedGradeId,
                        selectedTrackId = profile.selectedTrackId,
                        academicYear = profile.academicYear
                    )
                }
            }
        }
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun toggleLanguage() {
        val nextArabic = !_prefs.value.isArabic
        _prefs.value = _prefs.value.copy(isArabic = nextArabic)
        persistProfilePrefs()
    }

    fun toggleDarkTheme() {
        _prefs.value = _prefs.value.copy(isDarkTheme = !_prefs.value.isDarkTheme)
    }

    fun selectGradeAndTrack(gradeId: String, trackId: String, semester: Int = _prefs.value.selectedSemester) {
        val grade = OfficialEgyptianCurriculumCatalog.findGrade(gradeId)
        val validTrack = if (grade.tracks.any { it.id == trackId }) trackId else grade.tracks.first().id
        _prefs.value = _prefs.value.copy(
            selectedGradeId = gradeId,
            selectedTrackId = validTrack,
            selectedSemester = semester
        )
        _selectedSubject.value = null
        _selectedLesson.value = null
        persistProfilePrefs()
    }

    fun selectSemester(semester: Int) {
        _prefs.value = _prefs.value.copy(selectedSemester = semester)
    }

    private fun persistProfilePrefs() {
        val p = _prefs.value
        viewModelScope.launch {
            repository.upsertUserProfile(
                displayName = currentUserName,
                selectedGradeId = p.selectedGradeId,
                selectedTrackId = p.selectedTrackId,
                academicYear = p.academicYear,
                preferredLang = if (p.isArabic) "AR" else "EN"
            )
        }
    }

    fun selectSubject(subject: SubjectSpec?) {
        _selectedSubject.value = subject
        _selectedLesson.value = subject?.units?.firstOrNull()?.chapters?.firstOrNull()?.lessons?.firstOrNull()
    }

    fun selectLesson(lesson: LessonSpec?) {
        _selectedLesson.value = lesson
    }

    fun updateSearch(query: String, gradeFilter: String? = _searchGradeFilter.value, semesterFilter: Int? = _searchSemesterFilter.value) {
        _searchGradeFilter.value = gradeFilter
        _searchSemesterFilter.value = semesterFilter
        _searchQuery.value = query
    }

    fun setAiMode(mode: AiMode) {
        _aiMode.value = mode
    }

    fun toggleLessonCompleted(lesson: LessonSpec, subject: SubjectSpec, currentCompleted: Boolean) {
        viewModelScope.launch {
            val nextCompleted = !currentCompleted
            repository.setLessonProgress(
                lessonId = lesson.id,
                subjectId = subject.id,
                gradeId = subject.gradeId,
                completed = nextCompleted,
                masteryScore = if (nextCompleted) 90 else 40,
                weakTopic = !nextCompleted
            )
        }
    }

    fun flagWeakTopic(lesson: LessonSpec, subject: SubjectSpec, isWeak: Boolean) {
        viewModelScope.launch {
            repository.setLessonProgress(
                lessonId = lesson.id,
                subjectId = subject.id,
                gradeId = subject.gradeId,
                completed = false,
                masteryScore = if (isWeak) 35 else 75,
                weakTopic = isWeak
            )
        }
    }

    // ==================== AI LESSON SUMMARIZER ====================

    fun generateLessonSummary(summaryType: SummaryType) {
        val p = _prefs.value
        val grade = OfficialEgyptianCurriculumCatalog.findGrade(p.selectedGradeId)
        val subject = _selectedSubject.value ?: OfficialEgyptianCurriculumCatalog.subjectsFor(p.selectedGradeId, p.selectedTrackId).firstOrNull()
        val lesson = _selectedLesson.value ?: subject?.units?.firstOrNull()?.chapters?.firstOrNull()?.lessons?.firstOrNull()

        if (subject == null || lesson == null) {
            _aiTextState.value = AiTextState.Error(
                if (p.isArabic) "يرجى اختيار مادة ودرس أولًا." else "Please select a subject and lesson first."
            )
            return
        }

        val (ragCtx, citations) = CurriculumSearchEngine.retrieveRagContext(
            query = if (p.isArabic) lesson.titleAr else lesson.titleEn,
            gradeId = grade.id,
            subjectId = subject.id,
            selectedLesson = lesson,
            publishedCloudVersions = publishedCloudVersions.value,
            isArabic = p.isArabic
        )

        viewModelScope.launch {
            _aiTextState.value = AiTextState.Loading
            try {
                var finalText = ""
                aiRepository.streamLessonSummary(
                    lessonTitle = if (p.isArabic) lesson.titleAr else lesson.titleEn,
                    subjectName = if (p.isArabic) subject.nameAr else subject.nameEn,
                    gradeName = if (p.isArabic) grade.nameAr else grade.nameEn,
                    summaryType = summaryType,
                    ragContext = ragCtx,
                    citations = citations,
                    isArabic = p.isArabic
                ).collect { streamed ->
                    finalText = streamed
                    _aiTextState.value = AiTextState.Streaming(streamed, citations)
                }
                _aiTextState.value = AiTextState.Success(finalText, citations)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiTextState.value = AiTextState.Error(e.localizedMessage ?: "AI summary failed.")
            }
        }
    }

    fun saveCurrentSummaryToProfile(summaryType: SummaryType) {
        val state = _aiTextState.value
        val content = when (state) {
            is AiTextState.Success -> state.text
            is AiTextState.Streaming -> state.text
            else -> return
        }
        val p = _prefs.value
        val subject = _selectedSubject.value ?: OfficialEgyptianCurriculumCatalog.subjectsFor(p.selectedGradeId, p.selectedTrackId).firstOrNull()
        val lesson = _selectedLesson.value ?: subject?.units?.firstOrNull()?.chapters?.firstOrNull()?.lessons?.firstOrNull()
        viewModelScope.launch {
            val res = repository.saveSummary(
                lessonId = lesson?.id ?: "general",
                lessonTitle = lesson?.let { if (p.isArabic) it.titleAr else it.titleEn } ?: "Lesson Summary",
                subjectName = subject?.let { if (p.isArabic) it.nameAr else it.nameEn } ?: "Curriculum",
                summaryType = summaryType.name,
                content = content,
                language = if (p.isArabic) "AR" else "EN",
                origin = "AI_GENERATED"
            )
            _statusBanner.value = if (res.isSuccess) {
                if (p.isArabic) "تم حفظ الملخص في لوحة الطالب بنجاح" else "Summary saved to your Student Dashboard"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to save summary"
            }
        }
    }

    fun deleteSummary(summaryId: String) {
        viewModelScope.launch { repository.deleteSavedSummary(summaryId) }
    }

    // ==================== AI PROBLEM SOLVER (TEXT + PHOTO PICKER) ====================

    fun loadProblemBitmapFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val decoded = BitmapFactory.decodeStream(stream)
                    if (decoded != null) {
                        val maxDim = 1280
                        val scaled = if (decoded.width > maxDim || decoded.height > maxDim) {
                            val ratio = minOf(maxDim.toFloat() / decoded.width, maxDim.toFloat() / decoded.height)
                            Bitmap.createScaledBitmap(decoded, (decoded.width * ratio).toInt(), (decoded.height * ratio).toInt(), true)
                        } else decoded
                        _problemImage.value = scaled
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode problem image", e)
            }
        }
    }

    fun clearProblemBitmap() {
        _problemImage.value = null
    }

    fun solveProblem(problemText: String) {
        val p = _prefs.value
        val grade = OfficialEgyptianCurriculumCatalog.findGrade(p.selectedGradeId)
        val subject = _selectedSubject.value ?: OfficialEgyptianCurriculumCatalog.subjectsFor(p.selectedGradeId, p.selectedTrackId).firstOrNull()
        val image = _problemImage.value
        if (problemText.isBlank() && image == null) {
            _aiTextState.value = AiTextState.Error(
                if (p.isArabic) "اكتب المسألة أو أرفق صورة التمرين." else "Enter a question or pick an exercise photo."
            )
            return
        }

        val (ragCtx, citations) = CurriculumSearchEngine.retrieveRagContext(
            query = problemText.ifBlank { subject?.nameEn ?: "" },
            gradeId = grade.id,
            subjectId = subject?.id,
            selectedLesson = _selectedLesson.value,
            publishedCloudVersions = publishedCloudVersions.value,
            isArabic = p.isArabic
        )

        viewModelScope.launch {
            _aiTextState.value = AiTextState.Loading
            try {
                var finalText = ""
                aiRepository.streamProblemSolution(
                    problemText = problemText,
                    problemImage = image,
                    gradeName = if (p.isArabic) grade.nameAr else grade.nameEn,
                    subjectName = subject?.let { if (p.isArabic) it.nameAr else it.nameEn } ?: "General",
                    ragContext = ragCtx,
                    isArabic = p.isArabic
                ).collect { streamed ->
                    finalText = streamed
                    _aiTextState.value = AiTextState.Streaming(streamed, citations)
                }
                _aiTextState.value = AiTextState.Success(finalText, citations)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiTextState.value = AiTextState.Error(e.localizedMessage ?: "Problem solver failed.")
            }
        }
    }

    // ==================== AI STUDY TUTOR CHAT ====================

    fun sendTutorMessage(userMessage: String) {
        if (userMessage.isBlank() || _tutorStreaming.value) return
        val p = _prefs.value
        val grade = OfficialEgyptianCurriculumCatalog.findGrade(p.selectedGradeId)
        val subject = _selectedSubject.value ?: OfficialEgyptianCurriculumCatalog.subjectsFor(p.selectedGradeId, p.selectedTrackId).firstOrNull()
        val historyPairs = _tutorMessages.value.map { (if (it.isUser) "Student" else "Tutor") to it.text }
        _tutorMessages.value = _tutorMessages.value + TutorChatMessage(isUser = true, text = userMessage)

        val (ragCtx, citations) = CurriculumSearchEngine.retrieveRagContext(
            query = userMessage,
            gradeId = grade.id,
            subjectId = subject?.id,
            selectedLesson = _selectedLesson.value,
            publishedCloudVersions = publishedCloudVersions.value,
            isArabic = p.isArabic
        )

        viewModelScope.launch {
            _tutorStreaming.value = true
            _tutorMessages.value = _tutorMessages.value + TutorChatMessage(isUser = false, text = "...", citations = citations)
            try {
                var finalReply = ""
                aiRepository.streamTutorReply(
                    question = userMessage,
                    gradeName = if (p.isArabic) grade.nameAr else grade.nameEn,
                    subjectName = subject?.let { if (p.isArabic) it.nameAr else it.nameEn } ?: "Egyptian Curriculum",
                    ragContext = ragCtx,
                    citations = citations,
                    conversationHistory = historyPairs,
                    isArabic = p.isArabic
                ).collect { partial ->
                    finalReply = partial
                    val list = _tutorMessages.value.toMutableList()
                    if (list.isNotEmpty()) {
                        list[list.lastIndex] = TutorChatMessage(isUser = false, text = partial, citations = citations)
                        _tutorMessages.value = list
                    }
                }
                repository.saveConversationSnapshot(
                    subjectId = subject?.id ?: "general",
                    lessonId = _selectedLesson.value?.id ?: "general",
                    title = userMessage.take(80),
                    lastMessage = finalReply.take(400),
                    messageCount = _tutorMessages.value.size
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val list = _tutorMessages.value.toMutableList()
                if (list.isNotEmpty()) {
                    list[list.lastIndex] = TutorChatMessage(
                        isUser = false,
                        text = "Error: ${e.localizedMessage ?: "Unable to connect to AI Tutor."}"
                    )
                    _tutorMessages.value = list
                }
            } finally {
                _tutorStreaming.value = false
            }
        }
    }

    // ==================== AI PRACTICE EXAM GENERATOR ====================

    fun generatePracticeExam(
        difficulty: String,
        questionCount: Int,
        questionTypes: List<String>,
        topicOverride: String? = null
    ) {
        val p = _prefs.value
        val grade = OfficialEgyptianCurriculumCatalog.findGrade(p.selectedGradeId)
        val subject = _selectedSubject.value ?: OfficialEgyptianCurriculumCatalog.subjectsFor(p.selectedGradeId, p.selectedTrackId).firstOrNull()
        val lesson = _selectedLesson.value ?: subject?.units?.firstOrNull()?.chapters?.firstOrNull()?.lessons?.firstOrNull()
        val topicLabel = topicOverride ?: lesson?.let { if (p.isArabic) it.titleAr else it.titleEn } ?: (subject?.nameAr ?: "General")

        val (ragCtx, _) = CurriculumSearchEngine.retrieveRagContext(
            query = topicLabel,
            gradeId = grade.id,
            subjectId = subject?.id,
            selectedLesson = lesson,
            publishedCloudVersions = publishedCloudVersions.value,
            isArabic = p.isArabic
        )

        viewModelScope.launch {
            _examLoading.value = true
            _examError.value = null
            try {
                val exam = aiRepository.generatePracticeExam(
                    gradeName = if (p.isArabic) grade.nameAr else grade.nameEn,
                    subjectName = subject?.let { if (p.isArabic) it.nameAr else it.nameEn } ?: "General",
                    lessonTitle = topicLabel,
                    difficulty = difficulty,
                    questionCount = questionCount,
                    questionTypes = questionTypes,
                    ragContext = ragCtx,
                    isArabic = p.isArabic
                )
                _generatedExam.value = exam
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _examError.value = e.localizedMessage ?: "Failed to generate practice exam."
            } finally {
                _examLoading.value = false
            }
        }
    }

    fun recordPracticeExamScore(scorePercent: Int) {
        val exam = _generatedExam.value ?: return
        val p = _prefs.value
        val subjectId = _selectedSubject.value?.id ?: "general"
        viewModelScope.launch {
            repository.savePracticeExamScore(
                title = exam.title,
                gradeId = p.selectedGradeId,
                subjectId = subjectId,
                difficulty = exam.difficulty,
                questionCount = exam.questions.size,
                score = scorePercent
            )
            _statusBanner.value = if (p.isArabic) "تم حفظ نتيجة التدريب ($scorePercent%)" else "Practice score saved ($scorePercent%)"
        }
    }

    // ==================== MOE SYNC & LOCAL PDF VIEWER ====================

    fun checkOfficialMoePortal(portalUrl: String) {
        viewModelScope.launch {
            _moeSyncBusy.value = true
            val res = moeSyncService.checkMoePortal(portalUrl)
            _moeCheckResult.value = res
            if (isAdminUser) {
                repository.recordMoeSyncLog(res.portalUrl, res.statusLabel, res.message)
            }
            _moeSyncBusy.value = false
        }
    }

    fun downloadAndOpenMoePdf(pdfUrl: String, fileKey: String) {
        viewModelScope.launch {
            _moeSyncBusy.value = true
            val res = moeSyncService.downloadOfficialPdf(pdfUrl, fileKey)
            _moeSyncBusy.value = false
            res.onSuccess { info ->
                _localPdfFiles.value = moeSyncService.listLocalPdfs()
                openLocalPdf(info.localFile)
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to download MOE PDF"
            }
        }
    }

    fun openLocalPdf(file: File) {
        _activePdfFile.value = file
        _activePdfPage.value = 0
        renderPdfPage(file, 0)
    }

    fun closePdfViewer() {
        _activePdfFile.value = null
        _activePdfBitmap.value = null
    }

    fun navigatePdfPage(delta: Int) {
        val f = _activePdfFile.value ?: return
        val total = _activePdfTotalPages.value.coerceAtLeast(1)
        val next = (_activePdfPage.value + delta).coerceIn(0, total - 1)
        _activePdfPage.value = next
        renderPdfPage(f, next)
    }

    private fun renderPdfPage(file: File, pageIndex: Int) {
        viewModelScope.launch {
            moeSyncService.renderPdfPage(file, pageIndex).onSuccess { (bmp, total) ->
                _activePdfBitmap.value = bmp
                _activePdfTotalPages.value = total
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Unable to render PDF"
            }
        }
    }

    fun openExternalMoePortal(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    // ==================== ADMIN WORKFLOWS ====================

    fun importAdminPdfAndStageVersion(
        context: Context,
        uri: Uri,
        gradeId: String,
        subjectId: String,
        semester: Int,
        languageCode: String,
        sourceUrl: String
    ) {
        if (!isAdminUser) return
        val p = _prefs.value
        viewModelScope.launch {
            _moeSyncBusy.value = true
            val imported: Result<DownloadedPdfInfo> = moeSyncService.importLocalPdfFromUri(
                uri = uri,
                fileKey = "${gradeId}_${subjectId}_s${semester}_${languageCode}"
            )
            imported.onSuccess { info ->
                _localPdfFiles.value = moeSyncService.listLocalPdfs()
                val pageBmp = moeSyncService.renderPdfPage(info.localFile, 0).getOrNull()?.first
                val grade = OfficialEgyptianCurriculumCatalog.findGrade(gradeId)
                val subject = OfficialEgyptianCurriculumCatalog.subjects.firstOrNull { it.id == subjectId }
                val extractedText = if (pageBmp != null) {
                    try {
                        aiRepository.extractCurriculumSummaryFromPdfBitmap(
                            pageBitmap = pageBmp,
                            subjectName = subject?.nameAr ?: subjectId,
                            gradeName = grade.nameAr,
                            isArabic = languageCode == "AR"
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        "Imported PDF (${info.pageCount} pages, SHA-256: ${info.sha256.take(12)}). Review attached document."
                    }
                } else {
                    "Imported PDF (${info.pageCount} pages, SHA-256: ${info.sha256.take(12)})."
                }

                repository.stageCurriculumVersionAndDiff(
                    gradeId = gradeId,
                    subjectId = subjectId,
                    academicYear = p.academicYear,
                    semester = semester,
                    language = languageCode,
                    origin = "OFFICIAL_MOE",
                    sourceUrl = sourceUrl.ifBlank { OfficialEgyptianCurriculumCatalog.MOE_BOOKS_PORTAL },
                    fileSha256 = info.sha256,
                    extractedUnitsSummary = extractedText,
                    addedTopics = "Extracted ${info.pageCount}-page MOE document (${info.sha256.take(8)})",
                    removedTopics = "None automatically removed pending admin review",
                    modifiedLessons = extractedText.take(600),
                    diffSummary = "Staged ${subject?.nameEn ?: subjectId} (${info.pageCount} pages) for Admin approval."
                ).onSuccess {
                    _statusBanner.value = if (p.isArabic) {
                        "تم استخراج المنهج وإعداده للمراجعة (لم يُنشر تلقائيًا)"
                    } else {
                        "Curriculum PDF staged as PENDING_REVIEW with diff report"
                    }
                }.onFailure { err ->
                    _statusBanner.value = err.localizedMessage
                }
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "PDF import failed"
            }
            _moeSyncBusy.value = false
        }
    }

    fun approveCurriculumVersion(versionId: String, diffId: String?) {
        if (!isAdminUser) return
        viewModelScope.launch {
            repository.approveAndPublishVersion(versionId, diffId).onSuccess {
                _statusBanner.value = if (_prefs.value.isArabic) "تم اعتماد ونشر النسخة للطلاب" else "Curriculum version approved & published"
            }
        }
    }

    fun rejectCurriculumVersion(versionId: String, diffId: String?) {
        if (!isAdminUser) return
        viewModelScope.launch {
            repository.rejectCurriculumVersion(versionId, diffId).onSuccess {
                _statusBanner.value = if (_prefs.value.isArabic) "تم رفض النسخة" else "Staged version rejected"
            }
        }
    }

    fun publishOfficialAssessment(
        titleAr: String,
        titleEn: String,
        gradeId: String,
        subjectId: String,
        semester: Int,
        periodLabel: String,
        type: AssessmentType,
        languageCode: String,
        sourceUrl: String
    ) {
        if (!isAdminUser) return
        viewModelScope.launch {
            repository.publishOfficialAssessment(
                titleAr = titleAr,
                titleEn = titleEn.ifBlank { titleAr },
                gradeId = gradeId,
                subjectId = subjectId,
                academicYear = _prefs.value.academicYear,
                semester = semester,
                periodLabel = periodLabel,
                assessmentType = type.name,
                language = languageCode,
                sourceUrl = sourceUrl.ifBlank { OfficialEgyptianCurriculumCatalog.MOE_ASSESSMENTS_PORTAL },
                publishedDateLabel = "2026-11-16"
            ).onSuccess {
                _statusBanner.value = if (_prefs.value.isArabic) "تم نشر التقييم الرسمي" else "Official MOE assessment published"
            }.onFailure {
                _statusBanner.value = it.localizedMessage
            }
        }
    }

    private companion object {
        const val TAG = "SmartStudyVM"
    }
}
