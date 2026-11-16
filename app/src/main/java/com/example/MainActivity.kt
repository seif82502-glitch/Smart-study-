package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.MoePortalSyncService
import com.example.data.repository.SmartStudyAiRepository
import com.example.data.repository.SmartStudyRepository
import com.example.domain.AiMode
import com.example.domain.OfficialEgyptianCurriculumCatalog
import com.example.firebase.attemptAutoSignIn
import com.example.firebase.authStateFlow
import com.example.firebase.configureAppCheck
import com.example.firebase.signOut
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AiTutorScreen
import com.example.ui.screens.AssessmentsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CurriculumScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NativePdfViewerDialog
import com.example.ui.theme.SmartStudyTheme
import com.example.ui.viewmodel.SmartStudyViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

private object Routes {
    const val DASHBOARD = "dashboard"
    const val CURRICULUM = "curriculum"
    const val ASSESSMENTS = "assessments"
    const val AI_TUTOR = "ai_tutor"
    const val ADMIN = "admin"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: Configure App Check with launch intent BEFORE Compose UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            SmartStudyRoot()
        }
    }
}

@Composable
fun SmartStudyRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val currentUser by Firebase.auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    var guestLangArabic by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    val user = currentUser
    if (user == null) {
        SmartStudyTheme(isArabic = guestLangArabic) {
            AuthScreen(
                isArabic = guestLangArabic,
                onToggleLanguage = { guestLangArabic = !guestLangArabic },
                onAuthSuccess = {}
            )
        }
    } else {
        key(user.uid) {
            AuthenticatedSmartStudyApp(
                currentUserId = user.uid,
                credentialManager = credentialManager
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatedSmartStudyApp(
    currentUserId: String,
    credentialManager: CredentialManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vm: SmartStudyViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) { "APPLICATION_KEY missing" }
                SmartStudyViewModel(
                    repository = SmartStudyRepository(app),
                    aiRepository = SmartStudyAiRepository(),
                    moeSyncService = MoePortalSyncService(app),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val prefs by vm.prefs.collectAsStateWithLifecycle()
    val progressList by vm.lessonProgress.collectAsStateWithLifecycle()
    val savedSummaries by vm.savedSummaries.collectAsStateWithLifecycle()
    val conversations by vm.conversations.collectAsStateWithLifecycle()
    val practiceExams by vm.practiceExams.collectAsStateWithLifecycle()
    val publishedVersions by vm.publishedCloudVersions.collectAsStateWithLifecycle()
    val cloudAssessments by vm.cloudVerifiedAssessments.collectAsStateWithLifecycle()
    val selectedSubject by vm.selectedSubject.collectAsStateWithLifecycle()
    val selectedLesson by vm.selectedLesson.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val searchResults by vm.searchResults.collectAsStateWithLifecycle()
    val aiMode by vm.aiMode.collectAsStateWithLifecycle()
    val aiTextState by vm.aiTextState.collectAsStateWithLifecycle()
    val tutorMessages by vm.tutorMessages.collectAsStateWithLifecycle()
    val tutorStreaming by vm.tutorStreaming.collectAsStateWithLifecycle()
    val problemImage by vm.problemImage.collectAsStateWithLifecycle()
    val generatedExam by vm.generatedExam.collectAsStateWithLifecycle()
    val examLoading by vm.examLoading.collectAsStateWithLifecycle()
    val examError by vm.examError.collectAsStateWithLifecycle()
    val moeCheckResult by vm.moeCheckResult.collectAsStateWithLifecycle()
    val moeBusy by vm.moeSyncBusy.collectAsStateWithLifecycle()
    val localPdfFiles by vm.localPdfFiles.collectAsStateWithLifecycle()
    val activePdfFile by vm.activePdfFile.collectAsStateWithLifecycle()
    val activePdfBitmap by vm.activePdfBitmap.collectAsStateWithLifecycle()
    val activePdfPage by vm.activePdfPage.collectAsStateWithLifecycle()
    val activePdfTotalPages by vm.activePdfTotalPages.collectAsStateWithLifecycle()
    val statusBanner by vm.statusBanner.collectAsStateWithLifecycle()

    val adminAllVersions by vm.adminAllVersions.collectAsStateWithLifecycle()
    val adminDiffs by vm.adminDiffs.collectAsStateWithLifecycle()
    val adminSyncLogs by vm.adminSyncLogs.collectAsStateWithLifecycle()
    val adminAuditLogs by vm.adminAuditLogs.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusBanner) {
        val msg = statusBanner
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            vm.clearStatusBanner()
        }
    }

    val isArabic = prefs.isArabic
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.DASHBOARD

    val navItems = buildList {
        add(Triple(Routes.DASHBOARD, if (isArabic) "الرئيسية" else "Dashboard", Icons.Default.Home))
        add(Triple(Routes.CURRICULUM, if (isArabic) "المناهج" else "Curricula", Icons.AutoMirrored.Filled.MenuBook))
        add(Triple(Routes.ASSESSMENTS, if (isArabic) "التقييمات" else "Assessments", Icons.AutoMirrored.Filled.Assignment))
        add(Triple(Routes.AI_TUTOR, if (isArabic) "المعلم الذكي" else "AI Tutor", Icons.Default.AutoAwesome))
        if (vm.isAdminUser) {
            add(Triple(Routes.ADMIN, if (isArabic) "الإدارة" else "Admin", Icons.Default.AdminPanelSettings))
        }
    }

    SmartStudyTheme(darkTheme = prefs.isDarkTheme, isArabic = isArabic) {
        if (activePdfFile != null) {
            NativePdfViewerDialog(
                file = activePdfFile!!,
                bitmap = activePdfBitmap,
                pageIndex = activePdfPage,
                totalPages = activePdfTotalPages,
                isArabic = isArabic,
                onNavigatePage = vm::navigatePdfPage,
                onDismiss = vm::closePdfViewer
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 680.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (isArabic) "Smart Study — التعليم المصري" else "Smart Study Egypt",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        actions = {
                            IconButton(
                                onClick = vm::toggleLanguage,
                                modifier = Modifier.testTag("top_language_toggle")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = if (isArabic) "تغيير اللغة" else "Switch Language"
                                )
                            }
                            IconButton(
                                onClick = vm::toggleDarkTheme,
                                modifier = Modifier.testTag("top_theme_toggle")
                            ) {
                                Icon(
                                    imageVector = if (prefs.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = if (isArabic) "تغيير المظهر" else "Toggle theme"
                                )
                            }
                            IconButton(
                                onClick = { signOut(credentialManager, scope) },
                                modifier = Modifier.testTag("top_sign_out")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = if (isArabic) "تسجيل الخروج" else "Sign out"
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar {
                            navItems.forEach { (route, label, icon) ->
                                NavigationBarItem(
                                    selected = currentRoute == route,
                                    onClick = {
                                        navController.navigate(route) {
                                            popUpTo(Routes.DASHBOARD) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label, maxLines = 1) },
                                    modifier = Modifier.testTag("nav_$route")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail {
                            navItems.forEach { (route, label, icon) ->
                                NavigationRailItem(
                                    selected = currentRoute == route,
                                    onClick = {
                                        navController.navigate(route) {
                                            popUpTo(Routes.DASHBOARD) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label, maxLines = 1) },
                                    modifier = Modifier.testTag("rail_$route")
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        NavHost(
                            navController = navController,
                            startDestination = Routes.DASHBOARD
                        ) {
                            composable(Routes.DASHBOARD) {
                                DashboardScreen(
                                    prefs = prefs,
                                    progressList = progressList,
                                    savedSummaries = savedSummaries,
                                    conversations = conversations,
                                    practiceExams = practiceExams,
                                    onSelectGradeAndTrack = { g, t -> vm.selectGradeAndTrack(g, t) },
                                    onOpenSubject = { sub ->
                                        vm.selectSubject(sub)
                                        navController.navigate(Routes.CURRICULUM)
                                    },
                                    onOpenLesson = { sub, les ->
                                        vm.selectSubject(sub)
                                        vm.selectLesson(les)
                                        navController.navigate(Routes.CURRICULUM)
                                    },
                                    onOpenAssessments = { navController.navigate(Routes.ASSESSMENTS) },
                                    onOpenAiTutor = { navController.navigate(Routes.AI_TUTOR) },
                                    onDeleteSummary = vm::deleteSummary
                                )
                            }

                            composable(Routes.CURRICULUM) {
                                CurriculumScreen(
                                    prefs = prefs,
                                    selectedSubject = selectedSubject,
                                    selectedLesson = selectedLesson,
                                    searchQuery = searchQuery,
                                    searchResults = searchResults,
                                    progressList = progressList,
                                    publishedCloudVersions = publishedVersions,
                                    localPdfFiles = localPdfFiles,
                                    onSearchChange = { q -> vm.updateSearch(q) },
                                    onSelectGradeAndTrack = { g, t -> vm.selectGradeAndTrack(g, t) },
                                    onSelectSemester = vm::selectSemester,
                                    onSelectSubject = vm::selectSubject,
                                    onSelectLesson = vm::selectLesson,
                                    onToggleCompleted = vm::toggleLessonCompleted,
                                    onFlagWeakTopic = vm::flagWeakTopic,
                                    onAskAiForLesson = { sub, les ->
                                        vm.selectSubject(sub)
                                        vm.selectLesson(les)
                                        vm.setAiMode(AiMode.SUMMARIZER)
                                        navController.navigate(Routes.AI_TUTOR)
                                    },
                                    onOpenMoePortal = { url -> vm.openExternalMoePortal(context, url) },
                                    onOpenLocalPdf = vm::openLocalPdf
                                )
                            }

                            composable(Routes.ASSESSMENTS) {
                                AssessmentsScreen(
                                    prefs = prefs,
                                    cloudAssessments = cloudAssessments,
                                    moeCheckResult = moeCheckResult,
                                    moeBusy = moeBusy,
                                    onCheckMoeAssessmentsPortal = {
                                        vm.checkOfficialMoePortal(OfficialEgyptianCurriculumCatalog.MOE_ASSESSMENTS_PORTAL)
                                    },
                                    onOpenUrl = { url -> vm.openExternalMoePortal(context, url) },
                                    onDownloadPdf = vm::downloadAndOpenMoePdf,
                                    onStartAiPracticeForAssessment = { item ->
                                        val sub = OfficialEgyptianCurriculumCatalog.subjects.firstOrNull { it.id == item.subjectId }
                                        vm.selectSubject(sub)
                                        vm.setAiMode(AiMode.EXAM_GENERATOR)
                                        vm.generatePracticeExam(
                                            difficulty = "Medium",
                                            questionCount = 5,
                                            questionTypes = listOf("MCQ", "TRUE_FALSE", "WRITTEN"),
                                            topicOverride = if (isArabic) item.titleAr else item.titleEn
                                        )
                                        navController.navigate(Routes.AI_TUTOR)
                                    }
                                )
                            }

                            composable(Routes.AI_TUTOR) {
                                AiTutorScreen(
                                    prefs = prefs,
                                    aiMode = aiMode,
                                    selectedSubject = selectedSubject,
                                    selectedLesson = selectedLesson,
                                    aiTextState = aiTextState,
                                    tutorMessages = tutorMessages,
                                    tutorStreaming = tutorStreaming,
                                    problemImage = problemImage,
                                    generatedExam = generatedExam,
                                    examLoading = examLoading,
                                    examError = examError,
                                    onSelectMode = vm::setAiMode,
                                    onSelectSubject = vm::selectSubject,
                                    onSelectLesson = vm::selectLesson,
                                    onGenerateSummary = vm::generateLessonSummary,
                                    onSaveSummary = vm::saveCurrentSummaryToProfile,
                                    onPickProblemImage = { uri -> vm.loadProblemBitmapFromUri(context, uri) },
                                    onClearProblemImage = vm::clearProblemBitmap,
                                    onSolveProblem = vm::solveProblem,
                                    onSendTutorMessage = vm::sendTutorMessage,
                                    onGeneratePracticeExam = { diff, count, types ->
                                        vm.generatePracticeExam(diff, count, types)
                                    },
                                    onSaveExamScore = vm::recordPracticeExamScore
                                )
                            }

                            composable(Routes.ADMIN) {
                                AdminPanelScreen(
                                    prefs = prefs,
                                    isAdminUser = vm.isAdminUser,
                                    allVersions = adminAllVersions,
                                    diffs = adminDiffs,
                                    syncLogs = adminSyncLogs,
                                    auditLogs = adminAuditLogs,
                                    moeCheckResult = moeCheckResult,
                                    busy = moeBusy,
                                    onCheckMoePortals = vm::checkOfficialMoePortal,
                                    onImportCurriculumPdf = { uri, gradeId, subId, sem, lang, src ->
                                        vm.importAdminPdfAndStageVersion(context, uri, gradeId, subId, sem, lang, src)
                                    },
                                    onApproveVersion = vm::approveCurriculumVersion,
                                    onRejectVersion = vm::rejectCurriculumVersion,
                                    onPublishAssessment = vm::publishOfficialAssessment
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
