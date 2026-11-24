package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.ChemistryBranch
import com.example.data.GoalsPreferences
import com.example.data.NeetDatabase
import com.example.data.NeetRepository
import com.example.data.NeetSubject
import com.example.data.TimerPreferences
import com.example.ui.FocusTimerState
import com.example.ui.NeetUiState
import com.example.ui.NeetViewModel
import com.example.ui.components.GeminiSlideOutDrawer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.example.data.SquadCodeUtils
import com.example.data.SquadMember
import com.example.data.SquadPreferences
import com.example.data.SquadRepository
import com.example.ui.auth.authStateFlow
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ErrorLogScreen
import com.example.ui.screens.GoalsSettingsDialog
import com.example.ui.screens.MocksScreen
import com.example.ui.screens.SquadRoute
import com.example.ui.screens.SyllabusScreen
import com.example.ui.theme.NeetPulseTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_ANALYTICS = "analytics"
const val ROUTE_SYLLABUS = "syllabus"
const val ROUTE_MOCKS = "mocks"
const val ROUTE_ERRORS = "errors"
const val ROUTE_SQUAD = "squad"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = NeetDatabase.getInstance(applicationContext)
        val repository = NeetRepository(
            studySessionDao = database.studySessionDao(),
            mockTestDao = database.mockTestDao(),
            syllabusDao = database.syllabusDao(),
            goalsPreferences = GoalsPreferences(applicationContext),
            timerPreferences = TimerPreferences(applicationContext)
        )

        setContent {
            val viewModel: NeetViewModel = viewModel(factory = NeetViewModel.Factory(repository))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val timerState by viewModel.timerState.collectAsStateWithLifecycle()
            val lifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.syncTimerFromClock()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            val useDarkTheme = when (uiState.goals.themeMode) {
                "LIGHT" -> false
                "SYSTEM" -> androidx.compose.foundation.isSystemInDarkTheme()
                else -> true
            }

            NeetPulseTheme(darkTheme = useDarkTheme) {
                NeetPulseApp(
                    viewModel = viewModel,
                    uiState = uiState,
                    timerState = timerState,
                    isDarkTheme = useDarkTheme
                )
            }
        }
    }
}

@Composable
private fun NeetPulseApp(
    viewModel: NeetViewModel,
    uiState: NeetUiState,
    timerState: FocusTimerState,
    isDarkTheme: Boolean
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_DASHBOARD

    var drawerOpen by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var openAddTaskSignal by remember { mutableIntStateOf(0) }

    var syllabusFilterSubject by remember { mutableStateOf<NeetSubject?>(null) }
    var syllabusFilterChemBranch by remember { mutableStateOf(ChemistryBranch.ALL) }
    var syllabusFilterVersion by remember { mutableIntStateOf(0) }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val physicsChapters = remember(uiState.chapters) {
        uiState.chapters.filter { it.subject == NeetSubject.PHYSICS }
    }
    val physicsProgressPct = remember(physicsChapters) {
        if (physicsChapters.isEmpty()) 0
        else ((physicsChapters.count { it.isCompleted }.toFloat() / physicsChapters.size) * 100f).roundToInt()
    }

    val activeNavKey = when (currentRoute) {
        ROUTE_DASHBOARD -> "dashboard"
        ROUTE_ANALYTICS -> "analytics"
        ROUTE_MOCKS -> "mocks"
        ROUTE_ERRORS -> "errors"
        ROUTE_SQUAD -> "squad"
        ROUTE_SYLLABUS -> when (syllabusFilterSubject) {
            NeetSubject.PHYSICS -> "syllabus_physics"
            NeetSubject.CHEMISTRY -> "syllabus_chemistry_${syllabusFilterChemBranch.name.lowercase()}"
            NeetSubject.BOTANY -> "syllabus_botany"
            NeetSubject.ZOOLOGY -> "syllabus_zoology"
            null -> "syllabus_all"
        }
        else -> "dashboard"
    }

    val topTitle = when (currentRoute) {
        ROUTE_DASHBOARD -> stringResource(id = R.string.app_name)
        ROUTE_ANALYTICS -> "Analytics"
        ROUTE_SYLLABUS -> syllabusFilterSubject?.displayName ?: "Syllabus"
        ROUTE_MOCKS -> "Mock Tests"
        ROUTE_ERRORS -> "Error Log"
        ROUTE_SQUAD -> stringResource(id = R.string.nav_squad)
        else -> stringResource(id = R.string.app_name)
    }

    // Auto-sync Study Squad status & stats when authenticated and joined to a room
    val appContext = LocalContext.current.applicationContext
    val auth = remember {
        try {
            Firebase.auth
        } catch (_: Exception) {
            null
        }
    }
    val signedInUser = if (auth != null) {
        auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser).value
    } else null

    val latestUiState by rememberUpdatedState(uiState)
    val latestTimerState by rememberUpdatedState(timerState)

    if (signedInUser != null) {
        LaunchedEffect(signedInUser.uid) {
            val prefs = SquadPreferences(appContext)
            val db = FirebaseFirestore.getInstance(appContext.getString(R.string.firestore_database_id))
            val squadRepo = SquadRepository(db)
            viewModel.squadSyncSignal.collectLatest {
                // Brief delay lets Room flow recompute todayMinutes / latestMock before syncing
                delay(250L)
                val localProfile = prefs.readSquadProfile()
                val code = SquadCodeUtils.normalize(localProfile.roomCode)
                if (SquadCodeUtils.isValidRoomCode(code)) {
                    val displayName = localProfile.displayName.ifBlank {
                        signedInUser.displayName?.trim().orEmpty().ifBlank { "Aspirant" }
                    }
                    val status = if (latestTimerState.isRunning) {
                        SquadMember.STATUS_STUDYING
                    } else {
                        SquadMember.STATUS_IDLE
                    }
                    squadRepo.syncMemberProgress(
                        roomCode = code,
                        displayName = displayName,
                        todayMinutes = latestUiState.todayMinutes,
                        status = status,
                        latestMockScore = latestUiState.latestMock?.totalScore ?: 0
                    )
                }
            }
        }
    }

    GeminiSlideOutDrawer(
        isOpen = drawerOpen,
        activeNavKey = activeNavKey,
        physicsProgressPct = physicsProgressPct,
        isDarkTheme = isDarkTheme,
        onClose = { drawerOpen = false },
        onNewStudySession = {
            navigateTo(ROUTE_DASHBOARD)
            openAddTaskSignal++
        },
        onSelectDashboard = { navigateTo(ROUTE_DASHBOARD) },
        onSelectPhysics = {
            syllabusFilterSubject = NeetSubject.PHYSICS
            syllabusFilterChemBranch = ChemistryBranch.ALL
            syllabusFilterVersion++
            navigateTo(ROUTE_SYLLABUS)
        },
        onSelectChemistryBranch = { branch ->
            syllabusFilterSubject = NeetSubject.CHEMISTRY
            syllabusFilterChemBranch = branch
            syllabusFilterVersion++
            navigateTo(ROUTE_SYLLABUS)
        },
        onSelectBiologyBranch = { bioSubject ->
            syllabusFilterSubject = bioSubject
            syllabusFilterChemBranch = ChemistryBranch.ALL
            syllabusFilterVersion++
            navigateTo(ROUTE_SYLLABUS)
        },
        onSelectAnalytics = { navigateTo(ROUTE_ANALYTICS) },
        onSelectSyllabus = {
            syllabusFilterSubject = null
            syllabusFilterChemBranch = ChemistryBranch.ALL
            syllabusFilterVersion++
            navigateTo(ROUTE_SYLLABUS)
        },
        onSelectMocks = { navigateTo(ROUTE_MOCKS) },
        onSelectErrors = { navigateTo(ROUTE_ERRORS) },
        onSelectSquad = { navigateTo(ROUTE_SQUAD) },
        onOpenSettings = { showSettingsDialog = true },
        onToggleTheme = { dark -> viewModel.setThemeMode(if (dark) "DARK" else "LIGHT") }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                GeminiMinimalTopBar(
                    title = topTitle,
                    onMenuClick = { drawerOpen = true },
                    onSettingsClick = { showSettingsDialog = true }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = ROUTE_DASHBOARD
                ) {
                    composable(ROUTE_DASHBOARD) {
                        DashboardScreen(
                            uiState = uiState,
                            timerState = timerState,
                            openAddTaskSignal = openAddTaskSignal,
                            onSelectSubject = { subj, defaultCh ->
                                viewModel.selectTimerSubject(subj, defaultCh)
                            },
                            onSelectChapter = { ch -> viewModel.selectTimerChapter(ch) },
                            onSelectMode = { mode -> viewModel.selectTimerMode(mode) },
                            onToggleTimer = { viewModel.toggleTimer() },
                            onResetTimer = { viewModel.resetTimer() },
                            onDismissBanner = { viewModel.clearLastLoggedBanner() },
                            onSaveTimerSession = { solved, correct, notes ->
                                viewModel.saveTimerSession(solved, correct, notes)
                            },
                            onLogManualSession = { subj, ch, mins, solved, correct, notes ->
                                viewModel.logManualSession(subj, ch, mins, solved, correct, notes)
                            },
                            onDeleteSession = { id -> viewModel.deleteSession(id) },
                            onUpdateSubjectGoals = { phy, chem, bio ->
                                viewModel.updateSubjectGoals(phy, chem, bio)
                            },
                            onNavigateToAnalytics = { navigateTo(ROUTE_ANALYTICS) },
                            onLoadSampleData = { viewModel.loadSampleAnalytics() }
                        )
                    }
                    composable(ROUTE_ANALYTICS) {
                        AnalyticsScreen(
                            uiState = uiState,
                            onNavigateToHomeTimer = { navigateTo(ROUTE_DASHBOARD) },
                            onNavigateToMocks = { navigateTo(ROUTE_MOCKS) },
                            onLoadSampleData = { viewModel.loadSampleAnalytics() }
                        )
                    }
                    composable(ROUTE_SYLLABUS) {
                        SyllabusScreen(
                            chapters = uiState.chapters,
                            readinessPct = uiState.syllabusReadinessPct,
                            completedTopicsCount = uiState.completedTopicsCount,
                            chapterStudyStats = uiState.chapterStudyStats,
                            initialSubject = syllabusFilterSubject,
                            initialChemBranch = syllabusFilterChemBranch,
                            selectionVersion = syllabusFilterVersion,
                            onToggleCompleted = { ch, completed ->
                                viewModel.toggleChapterCompletion(ch, completed)
                            },
                            onIncrementRevision = { ch -> viewModel.incrementChapterRevision(ch) },
                            onUpdateMastery = { ch, level -> viewModel.updateChapterMastery(ch, level) },
                            onUpdateConfidence = { ch, stars -> viewModel.updateChapterConfidence(ch, stars) }
                        )
                    }
                    composable(ROUTE_MOCKS) {
                        MocksScreen(
                            mockTests = uiState.mockTests,
                            targetScore = uiState.goals.targetScore,
                            worstNegativeSubject = uiState.worstNegativeSubject,
                            onAddMockTest = { test -> viewModel.addMockTest(test) },
                            onDeleteMockTest = { id -> viewModel.deleteMockTest(id) }
                        )
                    }
                    composable(ROUTE_ERRORS) {
                        ErrorLogScreen(
                            sessions = uiState.sessions,
                            mockTests = uiState.mockTests,
                            chapters = uiState.chapters,
                            onLogMistakeSession = { subj, ch, mins, solved, correct, notes ->
                                viewModel.logManualSession(subj, ch, mins, solved, correct, notes)
                            },
                            onDeleteSession = { id -> viewModel.deleteSession(id) }
                        )
                    }
                    composable(ROUTE_SQUAD) {
                        SquadRoute(
                            todayMinutes = uiState.todayMinutes,
                            isStudying = timerState.isRunning,
                            latestMockScore = uiState.latestMock?.totalScore ?: 0
                        )
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        GoalsSettingsDialog(
            currentGoals = uiState.goals,
            onDismiss = { showSettingsDialog = false },
            onSave = { targetScore, phy, chem, bio, examDateMillis, themeMode ->
                viewModel.updateGoals(targetScore, phy, chem, bio, examDateMillis, themeMode)
            },
            onLoadSampleData = { viewModel.loadSampleAnalytics() },
            onResetAllData = { viewModel.resetAllData() }
        )
    }
}

@Composable
private fun GeminiMinimalTopBar(
    title: String,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("menu_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Open navigation menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (title == stringResource(id = R.string.app_name)) {
                    Image(
                        painter = painterResource(id = R.drawable.aura_icon),
                        contentDescription = "Aura logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = stringResource(id = R.string.cd_settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
