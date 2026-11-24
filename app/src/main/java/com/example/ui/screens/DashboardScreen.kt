package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AspirantGoals
import com.example.data.DailySubjectHours
import com.example.data.NeetSubject
import com.example.data.StudyMode
import com.example.data.StudySessionEntity
import com.example.data.SyllabusChapterEntity
import com.example.ui.FocusTimerState
import com.example.ui.NeetUiState
import com.example.ui.components.FocusTimerRing
import com.example.ui.components.SubjectBadge
import com.example.ui.theme.BotanyColor
import com.example.ui.theme.ChemistryColor
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GeminiGradientColors
import com.example.ui.theme.MonoMetricSmall
import com.example.ui.theme.MonoStopwatchHero
import com.example.ui.theme.PhysicsColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    uiState: NeetUiState,
    timerState: FocusTimerState,
    openAddTaskSignal: Int,
    onSelectSubject: (NeetSubject, String) -> Unit,
    onSelectChapter: (String) -> Unit,
    onSelectMode: (StudyMode) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onDismissBanner: () -> Unit,
    onSaveTimerSession: (solved: Int, correct: Int, notes: String) -> Unit,
    onLogManualSession: (NeetSubject, String, Int, Int, Int, String) -> Unit,
    onDeleteSession: (Long) -> Unit,
    onUpdateSubjectGoals: (physicsHours: Float, chemistryHours: Float, biologyHours: Float) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onLoadSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSubjectGoalsDialog by remember { mutableStateOf(false) }
    var showFinishTimerDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    LaunchedEffect(openAddTaskSignal) {
        if (openAddTaskSignal > 0) {
            showAddTaskDialog = true
        }
    }

    val subjectChapters = remember(uiState.chapters, timerState.selectedSubject) {
        uiState.chapters.filter { it.subject == timerState.selectedSubject }
    }

    val greetingLabel = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning, Aspirant"
            hour < 17 -> "Good afternoon, Aspirant"
            else -> "Good evening, Aspirant"
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .testTag("dashboard_list"),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 112.dp // clearance for bottom floating pill bar
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Minimal Gemini Gradient Greeting
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = greetingLabel,
                        style = MaterialTheme.typography.headlineMedium.merge(
                            TextStyle(brush = Brush.linearGradient(GeminiGradientColors))
                        )
                    )
                    Text(
                        text = "NEET D-${uiState.daysToNeet} · ${uiState.streakDays}d streak · Target ${uiState.goals.targetScore}/720",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Confirmation banner when a study block finishes or is logged
            timerState.lastLoggedBanner?.let { bannerText ->
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pomodoro_logged_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bannerText,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = onDismissBanner) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Dismiss confirmation",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Centered Stopwatch / Focus Ring with Mode & Chapter Pills
            item {
                CenteredStopwatchSection(
                    timerState = timerState,
                    subjectChapters = subjectChapters,
                    onSelectMode = onSelectMode,
                    onSelectSubject = { subj ->
                        val firstChapter = uiState.chapters.firstOrNull { it.subject == subj }?.chapterName
                            ?: "General Revision"
                        onSelectSubject(subj, firstChapter)
                    },
                    onSelectChapter = onSelectChapter
                )
            }

            // Compact, rounded (24dp) Subject Daily Progress Summary Cards
            item {
                SubjectProgressCardsSection(
                    today = uiState.todaySubjectHours,
                    goals = uiState.goals,
                    onEditGoals = { showSubjectGoalsDialog = true }
                )
            }

            // Clean quick link to Analytics or Sample Data if empty
            if (uiState.sessions.isEmpty() && uiState.mockTests.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Start tracking study hours",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Or preview charts with a sample NEET week",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = onLoadSampleData,
                                modifier = Modifier.testTag("empty_load_sample_button")
                            ) {
                                Text("Sample Week")
                            }
                        }
                    }
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Study Sessions",
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = onNavigateToAnalytics) {
                            Text("View Analytics")
                        }
                    }
                }

                items(uiState.sessions.take(5), key = { it.id }) { session ->
                    DashboardSessionRow(
                        session = session,
                        onDelete = { onDeleteSession(session.id) }
                    )
                }
            }
        }

        // Bottom Floating Pill Bar (Gemini prompt bar inspired)
        BottomFloatingControlPill(
            timerState = timerState,
            onToggleTimer = onToggleTimer,
            onResetTimer = onResetTimer,
            onSaveClick = { showFinishTimerDialog = true },
            onAddTaskClick = { showAddTaskDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }

    if (showSubjectGoalsDialog) {
        SubjectGoalsDialog(
            currentGoals = uiState.goals,
            onDismiss = { showSubjectGoalsDialog = false },
            onSave = onUpdateSubjectGoals
        )
    }

    if (showFinishTimerDialog) {
        SaveTimerSessionDialog(
            timerState = timerState,
            onDismiss = { showFinishTimerDialog = false },
            onConfirm = { solved, correct, notes ->
                onSaveTimerSession(solved, correct, notes)
                showFinishTimerDialog = false
            }
        )
    }

    if (showAddTaskDialog) {
        AddStudyTaskDialog(
            chapters = uiState.chapters,
            onDismiss = { showAddTaskDialog = false },
            onSave = { subject, chapter, mins, solved, correct, notes ->
                onLogManualSession(subject, chapter, mins, solved, correct, notes)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
private fun CenteredStopwatchSection(
    timerState: FocusTimerState,
    subjectChapters: List<SyllabusChapterEntity>,
    onSelectMode: (StudyMode) -> Unit,
    onSelectSubject: (NeetSubject) -> Unit,
    onSelectChapter: (String) -> Unit
) {
    var subjectExpanded by remember { mutableStateOf(false) }
    var chapterExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Minimal mode selector pills
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StudyMode.entries.forEach { mode ->
                val selected = timerState.selectedMode == mode
                FilterChip(
                    selected = selected,
                    onClick = { onSelectMode(mode) },
                    shape = CircleShape,
                    border = null,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    label = {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }

        // Centered Gemini Gradient Stopwatch Ring
        FocusTimerRing(
            progress = timerState.progressFraction,
            isRunning = timerState.isRunning,
            accentColor = timerState.selectedSubject.accentColor,
            modifier = Modifier.size(236.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatDashboardSeconds(timerState.displaySeconds),
                    style = MonoStopwatchHero,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (timerState.isRunning) {
                        "Focusing · ${timerState.selectedSubject.shortName}"
                    } else if (timerState.elapsedSeconds > 0) {
                        "Paused · ${timerState.selectedSubject.shortName}"
                    } else {
                        timerState.selectedMode.label
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Compact subject + chapter selector pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Surface(
                    onClick = { subjectExpanded = true },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(timerState.selectedSubject.accentColor)
                        )
                        Text(
                            text = timerState.selectedSubject.displayName,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Icon(
                            imageVector = Icons.Filled.ExpandMore,
                            contentDescription = "Select subject",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                DropdownMenu(
                    expanded = subjectExpanded,
                    onDismissRequest = { subjectExpanded = false }
                ) {
                    NeetSubject.entries.forEach { subj ->
                        DropdownMenuItem(
                            text = { Text(subj.displayName) },
                            onClick = {
                                onSelectSubject(subj)
                                subjectExpanded = false
                            }
                        )
                    }
                }
            }

            Box {
                Surface(
                    onClick = { chapterExpanded = true },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("timer_chapter_selector")
                ) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = 210.dp)
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = timerState.selectedChapter,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Icon(
                            imageVector = Icons.Filled.ExpandMore,
                            contentDescription = "Select chapter",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                DropdownMenu(
                    expanded = chapterExpanded,
                    onDismissRequest = { chapterExpanded = false }
                ) {
                    subjectChapters.forEach { chapter ->
                        DropdownMenuItem(
                            text = { Text("${chapter.chapterName} (Cl ${chapter.classLevel})") },
                            onClick = {
                                onSelectChapter(chapter.chapterName)
                                chapterExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectProgressCardsSection(
    today: DailySubjectHours,
    goals: AspirantGoals,
    onEditGoals: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Subject Progress",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(
                onClick = onEditGoals,
                modifier = Modifier.testTag("edit_subject_goals_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit Goals")
            }
        }

        // Three compact, 24dp rounded summary cards for Physics, Chemistry, and Biology
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompactSubjectProgressCard(
                label = "Physics",
                loggedHours = today.physicsHours,
                goalHours = goals.physicsGoalHours,
                color = PhysicsColor,
                onClick = onEditGoals,
                modifier = Modifier.weight(1f)
            )
            CompactSubjectProgressCard(
                label = "Chemistry",
                loggedHours = today.chemistryHours,
                goalHours = goals.chemistryGoalHours,
                color = ChemistryColor,
                onClick = onEditGoals,
                modifier = Modifier.weight(1f)
            )
            CompactSubjectProgressCard(
                label = "Biology",
                loggedHours = today.biologyHours,
                goalHours = goals.biologyGoalHours,
                color = BotanyColor,
                onClick = onEditGoals,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompactSubjectProgressCard(
    label: String,
    loggedHours: Float,
    goalHours: Float,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val safeGoal = goalHours.coerceAtLeast(0.5f)
    val rawProgress = (loggedHours / safeGoal).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 400),
        label = "subjectCardProgress"
    )
    val pct = ((loggedHours / safeGoal) * 100f).roundToInt()
    val goalMet = loggedHours >= safeGoal

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                if (goalMet) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "$label goal met",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${String.format(Locale.US, "%.1f", loggedHours)}/${String.format(Locale.US, "%.1f", safeGoal)}h",
                style = MonoMetricSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (goalMet) EmeraldSuccess else color,
                trackColor = color.copy(alpha = 0.16f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun BottomFloatingControlPill(
    timerState: FocusTimerState,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSaveClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 0.dp,
        modifier = modifier.widthIn(max = 560.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Primary Gemini Gradient Start / Pause Action
            Surface(
                onClick = onToggleTimer,
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(GeminiGradientColors))
                    .testTag("timer_toggle_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (timerState.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (timerState.isRunning) "Pause" else if (timerState.elapsedSeconds > 0) "Resume" else "Start",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            // Reset button
            Surface(
                onClick = onResetTimer,
                enabled = timerState.elapsedSeconds > 0,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("timer_reset_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reset timer",
                        tint = if (timerState.elapsedSeconds > 0) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Save logged duration button (when elapsed >= 1s)
            if (timerState.elapsedSeconds >= 1) {
                Surface(
                    onClick = onSaveClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("timer_save_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Save study block",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Quick '+ Add Task' button
            Surface(
                onClick = onAddTaskClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("quick_add_task_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Add Task",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSessionRow(
    session: StudySessionEntity,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM · hh:mm a", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubjectBadge(subject = session.subject)
                    Text(
                        text = "${session.durationMinutes} min",
                        style = MonoMetricSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (session.questionsSolved > 0) {
                        Text(
                            text = "· ${session.questionsCorrect}/${session.questionsSolved} MCQs",
                            style = MonoMetricSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = session.chapterName,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = dateFormat.format(Date(session.timestampMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = stringResource(id = R.string.cd_delete_item),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SaveTimerSessionDialog(
    timerState: FocusTimerState,
    onDismiss: () -> Unit,
    onConfirm: (solved: Int, correct: Int, notes: String) -> Unit
) {
    var solvedText by remember { mutableStateOf("") }
    var correctText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Study Block (${timerState.loggedMinutesSoFar} min)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${timerState.selectedSubject.displayName} · ${timerState.selectedChapter}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = solvedText,
                        onValueChange = { solvedText = it.filter(Char::isDigit).take(3) },
                        label = { Text("MCQs Solved") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = correctText,
                        onValueChange = { correctText = it.filter(Char::isDigit).take(3) },
                        label = { Text("Correct") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Revision notes / mistakes") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val solved = solvedText.toIntOrNull() ?: 0
                    val correct = (correctText.toIntOrNull() ?: 0).coerceAtMost(solved)
                    onConfirm(solved, correct, notes)
                }
            ) {
                Text("Save Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Keep Timing") }
        }
    )
}

@Composable
private fun AddStudyTaskDialog(
    chapters: List<SyllabusChapterEntity>,
    onDismiss: () -> Unit,
    onSave: (NeetSubject, String, Int, Int, Int, String) -> Unit
) {
    var subject by remember { mutableStateOf(NeetSubject.PHYSICS) }
    val subjectChapters = remember(chapters, subject) { chapters.filter { it.subject == subject } }
    var chapterName by remember(subject) {
        mutableStateOf(subjectChapters.firstOrNull()?.chapterName ?: "General Revision")
    }
    var chapterExpanded by remember { mutableStateOf(false) }
    var minutesText by remember { mutableStateOf("60") }
    var solvedText by remember { mutableStateOf("40") }
    var correctText by remember { mutableStateOf("34") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Study Session / Task") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NeetSubject.entries.forEach { item ->
                        FilterChip(
                            selected = subject == item,
                            onClick = { subject = item },
                            label = { Text(item.shortName) }
                        )
                    }
                }

                Box {
                    OutlinedTextField(
                        value = chapterName,
                        onValueChange = { chapterName = it },
                        label = { Text("Chapter") },
                        trailingIcon = {
                            IconButton(onClick = { chapterExpanded = true }) {
                                Icon(Icons.Filled.ExpandMore, contentDescription = "Pick chapter")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = chapterExpanded,
                        onDismissRequest = { chapterExpanded = false }
                    ) {
                        subjectChapters.forEach { ch ->
                            DropdownMenuItem(
                                text = { Text(ch.chapterName) },
                                onClick = {
                                    chapterName = ch.chapterName
                                    chapterExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { minutesText = it.filter(Char::isDigit).take(3) },
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = solvedText,
                        onValueChange = { solvedText = it.filter(Char::isDigit).take(3) },
                        label = { Text("MCQs Solved") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = correctText,
                        onValueChange = { correctText = it.filter(Char::isDigit).take(3) },
                        label = { Text("Correct") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Mistakes") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mins = (minutesText.toIntOrNull() ?: 30).coerceIn(5, 720)
                    val solved = solvedText.toIntOrNull() ?: 0
                    val correct = (correctText.toIntOrNull() ?: 0).coerceAtMost(solved)
                    onSave(subject, chapterName.ifBlank { "General Revision" }, mins, solved, correct, notes)
                },
                modifier = Modifier.testTag("confirm_manual_session_button")
            ) {
                Text("Save Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun formatDashboardSeconds(totalSec: Int): String {
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
