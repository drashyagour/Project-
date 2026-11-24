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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.NeetSubject
import com.example.ui.NeetUiState
import com.example.ui.components.EmptyAnalyticsCard
import com.example.ui.components.MetricTileCard
import com.example.ui.components.MockScoreTrajectoryChart
import com.example.ui.components.WeeklyStackedHoursChart
import com.example.ui.theme.BotanyColor
import com.example.ui.theme.ChemistryColor
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MonoMetricSmall
import com.example.ui.theme.PhysicsColor
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(
    uiState: NeetUiState,
    onNavigateToHomeTimer: () -> Unit,
    onNavigateToMocks: () -> Unit,
    onLoadSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Study & Exam Analytics",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Weekly hours, subject balance & mock score trajectory",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState.sessions.isEmpty() && uiState.mockTests.isEmpty()) {
                item {
                    EmptyAnalyticsCard(
                        onStartFocus = onNavigateToHomeTimer,
                        onLoadSample = onLoadSampleData
                    )
                }
            } else {
                item {
                    WeeklyDistributionCard(uiState = uiState)
                }

                item {
                    AnalyticsMetricsGrid(uiState = uiState)
                }

                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.primaryInsight,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                item {
                    SubjectBalanceCard(uiState = uiState)
                }

                if (uiState.mockTests.isNotEmpty()) {
                    item {
                        MockTrajectoryPreviewCard(
                            uiState = uiState,
                            onNavigateToMocks = onNavigateToMocks
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun AnalyticsMetricsGrid(uiState: NeetUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTileCard(
                label = "7-Day Study",
                value = "${String.format(Locale.US, "%.1f", uiState.weeklyHours)}h",
                subLabel = "Avg ${String.format(Locale.US, "%.1f", uiState.weeklyAvgHours)}h / day",
                icon = Icons.Filled.Schedule,
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            MetricTileCard(
                label = "MCQ Accuracy",
                value = if (uiState.totalMcqsSolved > 0) "${uiState.overallMcqAccuracy}%" else "—",
                subLabel = "${uiState.totalMcqsSolved} MCQs solved",
                icon = Icons.Filled.CheckCircle,
                accent = EmeraldSuccess,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTileCard(
                label = "Best Mock",
                value = if (uiState.mockTests.isNotEmpty()) "${uiState.bestMockScore}" else "—",
                subLabel = if (uiState.mockTests.isNotEmpty()) "Avg neg -${uiState.avgNegativeMarks} marks" else "No mocks yet",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                accent = PhysicsColor,
                modifier = Modifier.weight(1f)
            )
            MetricTileCard(
                label = "NCERT Checklist",
                value = "${uiState.syllabusReadinessPct}%",
                subLabel = "${uiState.completedTopicsCount}/${uiState.chapters.size} topics done",
                icon = Icons.Filled.MenuBook,
                accent = ChemistryColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WeeklyDistributionCard(uiState: NeetUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Study Hours",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Physics, Chemistry & Biology logged",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Goal ${String.format(Locale.US, "%.0f", uiState.goals.dailyGoalHours)}h",
                    style = MonoMetricSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            WeeklyStackedHoursChart(
                days = uiState.weeklyBreakdown,
                dailyGoalHours = uiState.goals.dailyGoalHours
            )
        }
    }
}

@Composable
private fun SubjectBalanceCard(uiState: NeetUiState) {
    val totalHours = uiState.subjectHours7d.values.sum().coerceAtLeast(0.01f)
    val bioHours = (uiState.subjectHours7d[NeetSubject.BOTANY] ?: 0f) + (uiState.subjectHours7d[NeetSubject.ZOOLOGY] ?: 0f)
    val phyHours = uiState.subjectHours7d[NeetSubject.PHYSICS] ?: 0f
    val chemHours = uiState.subjectHours7d[NeetSubject.CHEMISTRY] ?: 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = "Subject Balance vs Exam Share",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "7-day hours compared to 720-mark NEET pattern",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SubjectBalanceRow(
                label = "Biology (Botany + Zoology)",
                actualPct = ((bioHours / totalHours) * 100f).roundToInt(),
                targetPct = 50,
                hours = bioHours,
                color = BotanyColor
            )
            SubjectBalanceRow(
                label = "Physics",
                actualPct = ((phyHours / totalHours) * 100f).roundToInt(),
                targetPct = 25,
                hours = phyHours,
                color = PhysicsColor
            )
            SubjectBalanceRow(
                label = "Chemistry",
                actualPct = ((chemHours / totalHours) * 100f).roundToInt(),
                targetPct = 25,
                hours = chemHours,
                color = ChemistryColor
            )
        }
    }
}

@Composable
private fun SubjectBalanceRow(
    label: String,
    actualPct: Int,
    targetPct: Int,
    hours: Float,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Text(
                text = "${String.format(Locale.US, "%.1fh", hours)} · $actualPct% (Exam $targetPct%)",
                style = MonoMetricSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { (actualPct / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.16f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun MockTrajectoryPreviewCard(
    uiState: NeetUiState,
    onNavigateToMocks: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onNavigateToMocks
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mock Test Trajectory",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${uiState.mockTests.size} tests logged · Tap to analyze",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            MockScoreTrajectoryChart(
                tests = uiState.mockTests,
                targetScore = uiState.goals.targetScore
            )
        }
    }
}
