package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AspirantGoals
import com.example.ui.theme.BotanyColor
import com.example.ui.theme.ChemistryColor
import com.example.ui.theme.MonoMetricSmall
import com.example.ui.theme.PhysicsColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SubjectGoalsDialog(
    currentGoals: AspirantGoals,
    onDismiss: () -> Unit,
    onSave: (physicsHours: Float, chemistryHours: Float, biologyHours: Float) -> Unit
) {
    var physicsHours by remember { mutableFloatStateOf(currentGoals.physicsGoalHours) }
    var chemistryHours by remember { mutableFloatStateOf(currentGoals.chemistryGoalHours) }
    var biologyHours by remember { mutableFloatStateOf(currentGoals.biologyGoalHours) }
    val totalDaily = physicsHours + chemistryHours + biologyHours

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Daily Subject Goals", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Total Daily Target: ${String.format(Locale.US, "%.1f", totalDaily)} hrs/day",
                    style = MonoMetricSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                SubjectGoalSlider(
                    label = "Physics",
                    hours = physicsHours,
                    color = PhysicsColor,
                    range = 0.5f..8f,
                    onChange = { physicsHours = (it * 2f).roundToInt() / 2f }
                )
                SubjectGoalSlider(
                    label = "Chemistry",
                    hours = chemistryHours,
                    color = ChemistryColor,
                    range = 0.5f..8f,
                    onChange = { chemistryHours = (it * 2f).roundToInt() / 2f }
                )
                SubjectGoalSlider(
                    label = "Biology (Botany + Zoology)",
                    hours = biologyHours,
                    color = BotanyColor,
                    range = 0.5f..10f,
                    onChange = { biologyHours = (it * 2f).roundToInt() / 2f }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(physicsHours, chemistryHours, biologyHours)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_subject_goals_button")
            ) {
                Text("Save Goals")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsSettingsDialog(
    currentGoals: AspirantGoals,
    onDismiss: () -> Unit,
    onSave: (
        targetScore: Int,
        physicsHours: Float,
        chemistryHours: Float,
        biologyHours: Float,
        examDateMillis: Long,
        themeMode: String
    ) -> Unit,
    onLoadSampleData: () -> Unit,
    onResetAllData: () -> Unit
) {
    var targetScore by remember { mutableIntStateOf(currentGoals.targetScore) }
    var physicsHours by remember { mutableFloatStateOf(currentGoals.physicsGoalHours) }
    var chemistryHours by remember { mutableFloatStateOf(currentGoals.chemistryGoalHours) }
    var biologyHours by remember { mutableFloatStateOf(currentGoals.biologyGoalHours) }
    var examDateMillis by remember { mutableLongStateOf(currentGoals.examDateMillis) }
    var themeMode by remember { mutableStateOf(currentGoals.themeMode) }
    var showDatePicker by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }

    val totalDailyHours = physicsHours + chemistryHours + biologyHours
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "NEET Targets & Preferences", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Target Score", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "$targetScore / 720",
                        style = MonoMetricSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = targetScore.toFloat(),
                    onValueChange = { targetScore = (it / 5f).roundToInt() * 5 },
                    valueRange = 450f..720f
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Daily Subject Goals", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "${String.format(Locale.US, "%.1f", totalDailyHours)}h total",
                        style = MonoMetricSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                SubjectGoalSlider(
                    label = "Physics",
                    hours = physicsHours,
                    color = PhysicsColor,
                    range = 0.5f..8f,
                    onChange = { physicsHours = (it * 2f).roundToInt() / 2f }
                )
                SubjectGoalSlider(
                    label = "Chemistry",
                    hours = chemistryHours,
                    color = ChemistryColor,
                    range = 0.5f..8f,
                    onChange = { chemistryHours = (it * 2f).roundToInt() / 2f }
                )
                SubjectGoalSlider(
                    label = "Biology",
                    hours = biologyHours,
                    color = BotanyColor,
                    range = 0.5f..10f,
                    onChange = { biologyHours = (it * 2f).roundToInt() / 2f }
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Target Exam Date", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = dateFormatter.format(Date(examDateMillis)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Text("Change")
                    }
                }

                Text(text = "Appearance", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark").forEach { (key, label) ->
                        FilterChip(
                            selected = themeMode == key,
                            onClick = { themeMode = key },
                            label = { Text(label) }
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onLoadSampleData()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Load Sample")
                    }
                    OutlinedButton(
                        onClick = { confirmReset = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset Data")
                    }
                }

                Text(
                    text = "Aura · Developer: Drashya",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(targetScore, physicsHours, chemistryHours, biologyHours, examDateMillis, themeMode)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_goals_button")
            ) {
                Text("Save Targets")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = examDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { examDateMillis = it }
                        showDatePicker = false
                    }
                ) {
                    Text("Set Date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset Study Logs & Mocks?") },
            text = {
                Text("This removes all logged study sessions, mock tests, and NCERT revision counts from this device.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        confirmReset = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text("Keep Data")
                }
            }
        )
    }
}

@Composable
private fun SubjectGoalSlider(
    label: String,
    hours: Float,
    color: Color,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${String.format(Locale.US, "%.1f", hours)}h",
                style = MonoMetricSmall,
                color = color
            )
        }
        Slider(
            value = hours,
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color
            )
        )
    }
}
