package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ChemistryBranch
import com.example.data.MasteryLevel
import com.example.data.NeetSubject
import com.example.data.SyllabusChapterEntity
import com.example.ui.components.SubjectBadge
import com.example.ui.theme.AmberTertiaryLight
import com.example.ui.theme.MonoMetricSmall
import java.util.Locale

@Composable
fun SyllabusScreen(
    chapters: List<SyllabusChapterEntity>,
    readinessPct: Int,
    completedTopicsCount: Int,
    chapterStudyStats: Map<String, Pair<Int, Int>>,
    initialSubject: NeetSubject? = null,
    initialChemBranch: ChemistryBranch = ChemistryBranch.ALL,
    selectionVersion: Int = 0,
    onToggleCompleted: (SyllabusChapterEntity, Boolean) -> Unit,
    onIncrementRevision: (SyllabusChapterEntity) -> Unit,
    onUpdateMastery: (SyllabusChapterEntity, MasteryLevel) -> Unit,
    onUpdateConfidence: (SyllabusChapterEntity, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf<NeetSubject?>(initialSubject) }
    var selectedChemBranch by remember { mutableStateOf(initialChemBranch) }
    var selectedClass by remember { mutableIntStateOf(0) } // 0 = All, 11, 12
    var highYieldOnly by remember { mutableStateOf(false) }

    LaunchedEffect(selectionVersion, initialSubject, initialChemBranch) {
        selectedSubject = initialSubject
        selectedChemBranch = initialChemBranch
    }

    val filtered = remember(
        chapters,
        searchQuery,
        selectedSubject,
        selectedChemBranch,
        selectedClass,
        highYieldOnly
    ) {
        chapters.filter { ch ->
            val matchSubj = selectedSubject == null || ch.subject == selectedSubject
            val matchChemBranch = if (selectedSubject == NeetSubject.CHEMISTRY && selectedChemBranch != ChemistryBranch.ALL) {
                selectedChemBranch.matches(ch.chapterName)
            } else {
                true
            }
            val matchClass = selectedClass == 0 || ch.classLevel == selectedClass
            val matchYield = !highYieldOnly || ch.isHighYield
            val matchQuery = searchQuery.isBlank() || ch.chapterName.contains(searchQuery, ignoreCase = true)
            matchSubj && matchChemBranch && matchClass && matchYield && matchQuery
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "NCERT Syllabus Checklist", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                text = "$completedTopicsCount of ${chapters.size} topics completed · Tap checkbox to save",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$readinessPct%",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (readinessPct / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search NCERT chapters (e.g., Optics, Genetics)") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("syllabus_search_input")
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedSubject == null,
                            onClick = {
                                selectedSubject = null
                                selectedChemBranch = ChemistryBranch.ALL
                            },
                            shape = CircleShape,
                            label = { Text("All (${chapters.size})") }
                        )
                        NeetSubject.entries.forEach { subj ->
                            FilterChip(
                                selected = selectedSubject == subj,
                                onClick = {
                                    selectedSubject = if (selectedSubject == subj) null else subj
                                    if (selectedSubject != NeetSubject.CHEMISTRY) {
                                        selectedChemBranch = ChemistryBranch.ALL
                                    }
                                },
                                shape = CircleShape,
                                label = { Text(subj.displayName) }
                            )
                        }
                    }

                    if (selectedSubject == NeetSubject.CHEMISTRY) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ChemistryBranch.entries.forEach { branch ->
                                FilterChip(
                                    selected = selectedChemBranch == branch,
                                    onClick = { selectedChemBranch = branch },
                                    shape = CircleShape,
                                    label = { Text(branch.label) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedClass == 11,
                            onClick = { selectedClass = if (selectedClass == 11) 0 else 11 },
                            shape = CircleShape,
                            label = { Text("Class 11") }
                        )
                        FilterChip(
                            selected = selectedClass == 12,
                            onClick = { selectedClass = if (selectedClass == 12) 0 else 12 },
                            shape = CircleShape,
                            label = { Text("Class 12") }
                        )
                        FilterChip(
                            selected = highYieldOnly,
                            onClick = { highYieldOnly = !highYieldOnly },
                            shape = CircleShape,
                            leadingIcon = {
                                Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text("High Yield") }
                        )
                    }
                }
            }

            items(filtered, key = { it.id }) { chapter ->
                val stats = chapterStudyStats[chapter.chapterName] ?: Pair(0, 0)
                ChapterCard(
                    chapter = chapter,
                    studiedMinutes = stats.first,
                    mcqsSolved = stats.second,
                    onToggleCompleted = { checked -> onToggleCompleted(chapter, checked) },
                    onIncrementRevision = { onIncrementRevision(chapter) },
                    onUpdateMastery = { level -> onUpdateMastery(chapter, level) },
                    onUpdateConfidence = { stars -> onUpdateConfidence(chapter, stars) }
                )
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: SyllabusChapterEntity,
    studiedMinutes: Int,
    mcqsSolved: Int,
    onToggleCompleted: (Boolean) -> Unit,
    onIncrementRevision: () -> Unit,
    onUpdateMastery: (MasteryLevel) -> Unit,
    onUpdateConfidence: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubjectBadge(subject = chapter.subject)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Cl ${chapter.classLevel} · ${String.format(Locale.US, "%.1f", chapter.weightagePct)}%",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (chapter.isHighYield) {
                        Surface(
                            color = AmberTertiaryLight.copy(alpha = 0.16f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "High Yield",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberTertiaryLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                FilledTonalButton(
                    onClick = onIncrementRevision,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("increment_revision_${chapter.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.cd_increment_revision),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${chapter.ncertRevisions}x NCERT",
                        style = MonoMetricSmall
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleCompleted(!chapter.isCompleted) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = chapter.isCompleted,
                    onCheckedChange = onToggleCompleted,
                    modifier = Modifier.testTag("chapter_checkbox_${chapter.id}")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = chapter.chapterName,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (chapter.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (chapter.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            if (studiedMinutes > 0 || mcqsSolved > 0) {
                Text(
                    text = "Logged: ${studiedMinutes}m studied · $mcqsSolved MCQs practiced",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MasteryLevel.entries.forEach { level ->
                    FilterChip(
                        selected = chapter.mastery == level,
                        onClick = { onUpdateMastery(level) },
                        shape = CircleShape,
                        label = { Text(level.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Confidence",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { star ->
                        val filled = star <= chapter.confidence
                        Icon(
                            imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Confidence $star stars",
                            tint = if (filled) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onUpdateConfidence(star) }
                        )
                    }
                }
            }
        }
    }
}
