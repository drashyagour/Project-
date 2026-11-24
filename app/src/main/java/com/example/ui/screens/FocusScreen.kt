package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.NeetSubject
import com.example.data.StudyMode
import com.example.data.StudySessionEntity
import com.example.data.SyllabusChapterEntity
import com.example.ui.FocusTimerState
import com.example.ui.components.FocusTimerRing
import com.example.ui.components.SubjectBadge
import com.example.ui.theme.MonoMetricLarge
import com.example.ui.theme.MonoMetricSmall
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.Close
import com.example.data.DailySubjectHours

@Composable
fun FocusScreen(
    timerState: FocusTimerState,
    todaySubjectHours: DailySubjectHours,
    sessions: List<StudySessionEntity>,
    chapters: List<SyllabusChapterEntity>,
    onSelectSubject: (NeetSubject, String) -> Unit,
    onSelectChapter: (String) -> Unit,
    onSelectMode: (StudyMode) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onDismissBanner: () -> Unit,
    onSaveTimerSession: (solved: Int, correct: Int, notes: String) -> Unit,
    onLogManualSession: (NeetSubject, String, Int, Int, Int, String) -> Unit,
    onDeleteSession: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFinishTimerDialog by remember { mutableStateOf(false) }
    var showManualLogDialog by remember { mutableStateOf(false) }
    var filterSubject by remember { mutableStateOf<NeetSubject?>(null) }

    val subjectChapters = remember(chapters, timerState.selectedSubject) {
        chapters.filter { it.subject == timerState.selectedSubject }
    }

    val filteredSessions = remember(sessions, filterSubject) {
        if (filterSubject == null) sessions else sessions.filter { it.subject == filterSubject }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Pomodoro & Focus Timer", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = "Today: Phy ${String.format(Locale.US, "%.1fh", todaySubjectHours.physicsHours)} · Chem ${String.format(Locale.US, "%.1fh", todaySubjectHours.chemistryHours)} · Bio ${String.format(Locale.US, "%.1fh", todaySubjectHours.biologyHours)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        onClick = { showManualLogDialog = true },
                        modifier = Modifier.testTag("log_past_session_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Past")
                    }
                }
            }

            timerState.lastLoggedBanner?.let { bannerText ->
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pomodoro_logged_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
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

            item {
                LiveTimerCard(
                    timerState = timerState,
                    subjectChapters = subjectChapters,
                    onSelectSubject = { subj ->
                        val firstChapter = chapters.firstOrNull { it.subject == subj }?.chapterName ?: "General Revision"
                        onSelectSubject(subj, firstChapter)
                    },
                    onSelectChapter = onSelectChapter,
                    onSelectMode = onSelectMode,
                    onToggleTimer = onToggleTimer,
                    onResetTimer = onResetTimer,
                    onFinishClick = { showFinishTimerDialog = true }
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Recent Study Sessions (${filteredSessions.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterSubject == null,
                            onClick = { filterSubject = null },
                            label = { Text("All") }
                        )
                        NeetSubject.entries.forEach { subj ->
                            FilterChip(
                                selected = filterSubject == subj,
                                onClick = { filterSubject = if (filterSubject == subj) null else subj },
                                label = { Text(subj.displayName) }
                            )
                        }
                    }
                }
            }

            if (filteredSessions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No sessions logged yet",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start the timer above or tap 'Log Past' to record study hours & MCQs.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredSessions, key = { it.id }) { session ->
                    StudySessionCard(
                        session = session,
                        onDelete = { onDeleteSession(session.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    if (showFinishTimerDialog) {
        CompleteTimerDialog(
            timerState = timerState,
            onDismiss = { showFinishTimerDialog = false },
            onConfirm = { solved, correct, notes ->
                onSaveTimerSession(solved, correct, notes)
                showFinishTimerDialog = false
            }
        )
    }

    if (showManualLogDialog) {
        ManualSessionDialog(
            chapters = chapters,
            onDismiss = { showManualLogDialog = false },
            onSave = { subject, chapter, mins, solved, correct, notes ->
                onLogManualSession(subject, chapter, mins, solved, correct, notes)
                showManualLogDialog = false
            }
        )
    }
}

@Composable
private fun LiveTimerCard(
    timerState: FocusTimerState,
    subjectChapters: List<SyllabusChapterEntity>,
    onSelectSubject: (NeetSubject) -> Unit,
    onSelectChapter: (String) -> Unit,
    onSelectMode: (StudyMode) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onFinishClick: () -> Unit
) {
    var chapterMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StudyMode.entries.forEach { mode ->
                    FilterChip(
                        selected = timerState.selectedMode == mode,
                        onClick = { onSelectMode(mode) },
                        label = { Text(mode.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subject selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeetSubject.entries.forEach { subj ->
                    FilterChip(
                        selected = timerState.selectedSubject == subj,
                        onClick = { onSelectSubject(subj) },
                        label = { Text(subj.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chapter picker
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { chapterMenuExpanded = true }
                        .testTag("timer_chapter_selector")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timerState.selectedChapter,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Filled.ExpandMore, contentDescription = "Select chapter")
                    }
                }

                DropdownMenu(
                    expanded = chapterMenuExpanded,
                    onDismissRequest = { chapterMenuExpanded = false }
                ) {
                    subjectChapters.forEach { chapter ->
                        DropdownMenuItem(
                            text = { Text("${chapter.chapterName} (Cl ${chapter.classLevel})") },
                            onClick = {
                                onSelectChapter(chapter.chapterName)
                                chapterMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            FocusTimerRing(
                progress = timerState.progressFraction,
                isRunning = timerState.isRunning,
                accentColor = timerState.selectedSubject.accentColor,
                modifier = Modifier.size(200.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatTimerSeconds(timerState.displaySeconds),
                        style = MonoMetricLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (timerState.isRunning) "Focusing · ${timerState.selectedSubject.shortName}" else timerState.selectedMode.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onResetTimer,
                    enabled = timerState.elapsedSeconds > 0,
                    modifier = Modifier.testTag("timer_reset_button")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset timer")
                }

                Button(
                    onClick = onToggleTimer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = timerState.selectedSubject.accentColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("timer_toggle_button")
                ) {
                    Icon(
                        imageVector = if (timerState.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (timerState.isRunning) "Pause" else if (timerState.elapsedSeconds > 0) "Resume" else "Start Focus",
                        fontWeight = FontWeight.Bold
                    )
                }

                FilledTonalButton(
                    onClick = onFinishClick,
                    enabled = timerState.elapsedSeconds >= 1,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("timer_save_button")
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun StudySessionCard(
    session: StudySessionEntity,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM · hh:mm a", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                            text = "· ${session.questionsCorrect}/${session.questionsSolved} MCQs (${session.accuracyPercent}%)",
                            style = MonoMetricSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(text = session.chapterName, style = MaterialTheme.typography.titleSmall)
                if (session.notes.isNotBlank()) {
                    Text(
                        text = session.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
private fun CompleteTimerDialog(
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
                    label = { Text("Revision notes / weak formulas") },
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
private fun ManualSessionDialog(
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
        title = { Text("Log Past Study Block") },
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

private fun formatTimerSeconds(totalSec: Int): String {
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
