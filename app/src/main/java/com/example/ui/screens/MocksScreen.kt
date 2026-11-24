package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.MockTestEntity
import com.example.data.NeetSubject
import com.example.ui.components.GeminiGradientPillButton
import com.example.ui.components.MockScoreTrajectoryChart
import com.example.ui.theme.BotanyColor
import com.example.ui.theme.ChemistryColor
import com.example.ui.theme.MonoMetricMedium
import com.example.ui.theme.MonoMetricSmall
import com.example.ui.theme.NegativeCoral
import com.example.ui.theme.PhysicsColor
import com.example.ui.theme.ZoologyColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MocksScreen(
    mockTests: List<MockTestEntity>,
    targetScore: Int,
    worstNegativeSubject: NeetSubject?,
    onAddMockTest: (MockTestEntity) -> Unit,
    onDeleteMockTest: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val sortedDesc = remember(mockTests) { mockTests.sortedByDescending { it.testDateMillis } }

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
                        Text(text = "NTA 720 Mock Analyzer", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = "+4 / -1 score breakdown & negative bleed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    GeminiGradientPillButton(
                        text = "Log Mock",
                        icon = Icons.Filled.Add,
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("add_mock_button")
                    )
                }
            }

            if (sortedDesc.isEmpty()) {
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
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Mock Tests Logged",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap 'Log Mock' to record your Physics, Chemistry, Botany, and Zoology correct/wrong counts out of 45 questions each.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(text = "Score Trajectory", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            MockScoreTrajectoryChart(tests = mockTests, targetScore = targetScore)
                        }
                    }
                }

                item {
                    NegativeMarkingDiagnosticCard(
                        latest = sortedDesc.first(),
                        worstSubject = worstNegativeSubject
                    )
                }

                item {
                    Text(
                        text = "Test History (${sortedDesc.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(sortedDesc, key = { it.id }) { test ->
                    MockTestCard(
                        test = test,
                        onDelete = { onDeleteMockTest(test.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }
        }
    }

    if (showAddDialog) {
        AddMockTestDialog(
            onDismiss = { showAddDialog = false },
            onSave = { test ->
                onAddMockTest(test)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun NegativeMarkingDiagnosticCard(
    latest: MockTestEntity,
    worstSubject: NeetSubject?
) {
    Surface(
        color = NegativeCoral.copy(alpha = 0.12f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = NegativeCoral,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Negative Marking Bleed: -${latest.totalNegativeMarks} marks",
                    style = MaterialTheme.typography.titleSmall,
                    color = NegativeCoral
                )
                val worstNote = worstSubject?.let { " Highest wrong in ${it.displayName}." } ?: ""
                Text(
                    text = "Without wrong guesses, latest score would be ${latest.potentialScoreWithoutNegatives}/720.$worstNote",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MockTestCard(
    test: MockTestEntity,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = test.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${dateFormat.format(Date(test.testDateMillis))} · ${test.accuracyPercent}% accuracy · -${test.totalNegativeMarks} neg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${test.totalScore}/720",
                        style = MonoMetricMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = stringResource(id = R.string.cd_delete_item),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubjectScorePill("PHY", test.physicsScore, PhysicsColor, Modifier.weight(1f))
                SubjectScorePill("CHEM", test.chemistryScore, ChemistryColor, Modifier.weight(1f))
                SubjectScorePill("BOT", test.botanyScore, BotanyColor, Modifier.weight(1f))
                SubjectScorePill("ZOO", test.zoologyScore, ZoologyColor, Modifier.weight(1f))
            }

            if (test.mistakeNotes.isNotBlank()) {
                Text(
                    text = "Mistake Log: ${test.mistakeNotes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SubjectScorePill(
    label: String,
    score: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "$score",
                style = MonoMetricSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AddMockTestDialog(
    onDismiss: () -> Unit,
    onSave: (MockTestEntity) -> Unit
) {
    var title by remember { mutableStateOf("Full Syllabus Mock") }
    var phyC by remember { mutableStateOf("35") }
    var phyW by remember { mutableStateOf("5") }
    var chemC by remember { mutableStateOf("37") }
    var chemW by remember { mutableStateOf("4") }
    var botC by remember { mutableStateOf("41") }
    var botW by remember { mutableStateOf("2") }
    var zooC by remember { mutableStateOf("40") }
    var zooW by remember { mutableStateOf("3") }
    var notes by remember { mutableStateOf("") }

    fun parseSubject(cStr: String, wStr: String): Triple<Int, Int, Int> {
        val c = (cStr.toIntOrNull() ?: 0).coerceIn(0, 45)
        val w = (wStr.toIntOrNull() ?: 0).coerceIn(0, 45 - c)
        val s = (45 - c - w).coerceAtLeast(0)
        return Triple(c, w, s)
    }

    val (pc, pw, ps) = parseSubject(phyC, phyW)
    val (cc, cw, cs) = parseSubject(chemC, chemW)
    val (bc, bw, bs) = parseSubject(botC, botW)
    val (zc, zw, zs) = parseSubject(zooC, zooW)

    val liveTotal = ((pc + cc + bc + zc) * 4) - (pw + cw + bw + zw)
    val liveNeg = pw + cw + bw + zw

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log NTA 720 Mock Test") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Score: $liveTotal / 720",
                            style = MonoMetricMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "-$liveNeg neg",
                            style = MonoMetricSmall,
                            color = NegativeCoral
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Test Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                SubjectCountRow("Physics (45 Qs)", phyC, { phyC = it }, phyW, { phyW = it }, ps)
                SubjectCountRow("Chemistry (45 Qs)", chemC, { chemC = it }, chemW, { chemW = it }, cs)
                SubjectCountRow("Botany (45 Qs)", botC, { botC = it }, botW, { botW = it }, bs)
                SubjectCountRow("Zoology (45 Qs)", zooC, { zooC = it }, zooW, { zooW = it }, zs)

                HorizontalDivider()

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Mistake Analysis Notes") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        MockTestEntity(
                            title = title.ifBlank { "NEET Mock Test" },
                            physicsCorrect = pc, physicsWrong = pw, physicsSkipped = ps,
                            chemistryCorrect = cc, chemistryWrong = cw, chemistrySkipped = cs,
                            botanyCorrect = bc, botanyWrong = bw, botanySkipped = bs,
                            zoologyCorrect = zc, zoologyWrong = zw, zoologySkipped = zs,
                            mistakeNotes = notes.trim()
                        )
                    )
                },
                modifier = Modifier.testTag("confirm_add_mock_button")
            ) {
                Text("Save Mock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SubjectCountRow(
    label: String,
    correctVal: String,
    onCorrectChange: (String) -> Unit,
    wrongVal: String,
    onWrongChange: (String) -> Unit,
    skippedCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Text(
                text = "Skipped: $skippedCount",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = correctVal,
                onValueChange = { onCorrectChange(it.filter(Char::isDigit).take(2)) },
                label = { Text("Correct (+4)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = wrongVal,
                onValueChange = { onWrongChange(it.filter(Char::isDigit).take(2)) },
                label = { Text("Wrong (-1)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
