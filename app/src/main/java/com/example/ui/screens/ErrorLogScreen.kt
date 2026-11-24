package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.example.data.MockTestEntity
import com.example.data.NeetSubject
import com.example.data.StudySessionEntity
import com.example.data.SyllabusChapterEntity
import com.example.ui.components.GeminiGradientPillButton
import com.example.ui.components.SubjectBadge
import com.example.ui.theme.MonoMetricSmall
import com.example.ui.theme.NegativeCoral
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ErrorLogScreen(
    sessions: List<StudySessionEntity>,
    mockTests: List<MockTestEntity>,
    chapters: List<SyllabusChapterEntity>,
    onLogMistakeSession: (NeetSubject, String, Int, Int, Int, String) -> Unit,
    onDeleteSession: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubject by remember { mutableStateOf<NeetSubject?>(null) }
    var showAddMistakeDialog by remember { mutableStateOf(false) }

    val mistakeSessions = remember(sessions, selectedSubject) {
        sessions.filter { session ->
            val hasMistakeSignal = session.notes.isNotBlank() ||
                (session.questionsSolved > 0 && session.questionsCorrect < session.questionsSolved)
            val subjectMatch = selectedSubject == null || session.subject == selectedSubject
            hasMistakeSignal && subjectMatch
        }
    }

    val mockMistakes = remember(mockTests) {
        mockTests.filter { it.mistakeNotes.isNotBlank() || it.totalNegativeMarks > 0 }
            .sortedByDescending { it.testDateMillis }
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM · hh:mm a", Locale.getDefault()) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Error Log & Mistakes",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Wrong MCQs, conceptual gaps & mock negative bleed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    GeminiGradientPillButton(
                        text = "Log Error",
                        icon = Icons.Filled.Add,
                        onClick = { showAddMistakeDialog = true },
                        modifier = Modifier.testTag("log_error_button")
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedSubject == null,
                        onClick = { selectedSubject = null },
                        label = { Text("All") }
                    )
                    NeetSubject.entries.forEach { subj ->
                        FilterChip(
                            selected = selectedSubject == subj,
                            onClick = { selectedSubject = if (selectedSubject == subj) null else subj },
                            label = { Text(subj.displayName) }
                        )
                    }
                }
            }

            if (mockMistakes.isNotEmpty() && selectedSubject == null) {
                item {
                    Text(
                        text = "Mock Test Mistake Analysis",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(mockMistakes, key = { "mock_${it.id}" }) { mock ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = mock.title, style = MaterialTheme.typography.titleSmall)
                                Surface(
                                    color = NegativeCoral.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "-${mock.totalNegativeMarks} neg",
                                        style = MonoMetricSmall,
                                        color = NegativeCoral,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (mock.mistakeNotes.isNotBlank()) {
                                Text(
                                    text = mock.mistakeNotes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = "${mock.totalWrong} wrong answers across Phy (${mock.physicsWrong}), Chem (${mock.chemistryWrong}), Bio (${mock.botanyWrong + mock.zoologyWrong}).",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Chapter Mistake Notes (${mistakeSessions.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (mistakeSessions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "No mistakes logged yet",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Tap 'Log Error' to record tricky NCERT exceptions, formula slips, or wrong MCQs for revision.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(mistakeSessions, key = { it.id }) { session ->
                    val wrongCount = (session.questionsSolved - session.questionsCorrect).coerceAtLeast(0)
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
                            verticalAlignment = Alignment.Top
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
                                    if (wrongCount > 0) {
                                        Text(
                                            text = "$wrongCount wrong / ${session.questionsSolved}",
                                            style = MonoMetricSmall,
                                            color = NegativeCoral
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
                            IconButton(onClick = { onDeleteSession(session.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete mistake entry",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showAddMistakeDialog) {
        LogMistakeDialog(
            chapters = chapters,
            onDismiss = { showAddMistakeDialog = false },
            onSave = { subject, chapter, note ->
                onLogMistakeSession(subject, chapter, 15, 1, 0, note)
                showAddMistakeDialog = false
            }
        )
    }
}

@Composable
private fun LogMistakeDialog(
    chapters: List<SyllabusChapterEntity>,
    onDismiss: () -> Unit,
    onSave: (NeetSubject, String, String) -> Unit
) {
    var subject by remember { mutableStateOf(NeetSubject.PHYSICS) }
    val subjectChapters = remember(chapters, subject) { chapters.filter { it.subject == subject } }
    var chapterName by remember(subject) {
        mutableStateOf(subjectChapters.firstOrNull()?.chapterName ?: "General Revision")
    }
    var chapterExpanded by remember { mutableStateOf(false) }
    var mistakeNote by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Mistake / Weak Concept") },
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
                TextButton(onClick = { chapterExpanded = true }) {
                    Text("Pick from NCERT syllabus")
                }
                OutlinedTextField(
                    value = mistakeNote,
                    onValueChange = { mistakeNote = it },
                    label = { Text("What went wrong? (Formula, sign, NCERT line)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanNote = mistakeNote.ifBlank { "Needs NCERT concept review" }
                    onSave(subject, chapterName.ifBlank { "General Revision" }, cleanNote)
                }
            ) {
                Text("Save Error")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
