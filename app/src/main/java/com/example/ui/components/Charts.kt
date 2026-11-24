package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DailySubjectMinutes
import com.example.data.MockTestEntity
import com.example.data.NeetSubject
import com.example.ui.theme.BotanyColor
import com.example.ui.theme.ChemistryColor
import com.example.ui.theme.MonoMetricSmall
import com.example.ui.theme.PhysicsColor
import com.example.ui.theme.ZoologyColor
import java.util.Locale
import kotlin.math.max

@Composable
fun WeeklyStackedHoursChart(
    days: List<DailySubjectMinutes>,
    dailyGoalHours: Float,
    modifier: Modifier = Modifier
) {
    if (days.isEmpty()) return
    var selectedDayIndex by remember { mutableIntStateOf(days.lastIndex) }
    var groupedMode by remember { mutableStateOf(false) }
    val safeSelectedIndex = selectedDayIndex.coerceIn(0, days.lastIndex)
    val selectedDay = days[safeSelectedIndex]

    val selectedBiologyMinutes = selectedDay.botanyMinutes + selectedDay.zoologyMinutes

    val maxHours = if (groupedMode) {
        val peakSubjectHours = days.maxOfOrNull { d ->
            maxOf(
                d.physicsMinutes / 60f,
                d.chemistryMinutes / 60f,
                (d.botanyMinutes + d.zoologyMinutes) / 60f
            )
        } ?: 2f
        max(dailyGoalHours * 0.5f, peakSubjectHours * 1.2f).coerceAtLeast(1.5f)
    } else {
        max(
            dailyGoalHours * 1.15f,
            (days.maxOfOrNull { it.totalHours } ?: 4f) * 1.15f
        ).coerceAtLeast(2f)
    }

    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    val goalLineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val biologyColor = BotanyColor

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend + Stacked/Grouped toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendDot("Physics", PhysicsColor)
                LegendDot("Chemistry", ChemistryColor)
                LegendDot("Biology", biologyColor)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    color = if (!groupedMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .clickable { groupedMode = false }
                        .testTag("chart_mode_stacked")
                ) {
                    Text(
                        text = "Stacked",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (!groupedMode) FontWeight.Bold else FontWeight.Medium,
                        color = if (!groupedMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
                Surface(
                    color = if (groupedMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .clickable { groupedMode = true }
                        .testTag("chart_mode_grouped")
                ) {
                    Text(
                        text = "Grouped",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (groupedMode) FontWeight.Bold else FontWeight.Medium,
                        color = if (groupedMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected day live breakdown strip
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedDay.dayLabel}: ${String.format(Locale.US, "%.1fh", selectedDay.totalHours)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniSubjectStat("Phy", selectedDay.physicsMinutes, PhysicsColor)
                    MiniSubjectStat("Chem", selectedDay.chemistryMinutes, ChemistryColor)
                    MiniSubjectStat("Bio", selectedBiologyMinutes, biologyColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Y-axis scale labels
            Column(
                modifier = Modifier
                    .width(28.dp)
                    .height(165.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${String.format(Locale.US, "%.0f", maxHours)}h",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format(Locale.US, "%.0f", maxHours / 2f)}h",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "0h",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(165.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val chartHeight = size.height
                    val chartWidth = size.width
                    val barSlotWidth = chartWidth / days.size

                    // Horizontal baseline & mid gridline
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, chartHeight),
                        end = Offset(chartWidth, chartHeight),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = outlineColor.copy(alpha = 0.25f),
                        start = Offset(0f, chartHeight / 2f),
                        end = Offset(chartWidth, chartHeight / 2f),
                        strokeWidth = 1.dp.toPx()
                    )

                    if (!groupedMode) {
                        val goalY = chartHeight - ((dailyGoalHours / maxHours).coerceIn(0f, 1f) * chartHeight)
                        drawLine(
                            color = goalLineColor,
                            start = Offset(0f, goalY),
                            end = Offset(chartWidth, goalY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )

                        val barWidth = (barSlotWidth * 0.46f).coerceAtMost(26.dp.toPx())
                        days.forEachIndexed { index, day ->
                            val centerX = barSlotWidth * index + barSlotWidth / 2f
                            val leftX = centerX - barWidth / 2f

                            drawRoundRect(
                                color = trackColor,
                                topLeft = Offset(leftX, 0f),
                                size = Size(barWidth, chartHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )

                            val segments = listOf(
                                day.physicsMinutes to PhysicsColor,
                                day.chemistryMinutes to ChemistryColor,
                                (day.botanyMinutes + day.zoologyMinutes) to biologyColor
                            )

                            var currentBottom = chartHeight
                            segments.forEach { (minutes, color) ->
                                if (minutes > 0) {
                                    val segHours = minutes / 60f
                                    val segHeight = ((segHours / maxHours) * chartHeight).coerceAtLeast(3.dp.toPx())
                                    val topY = (currentBottom - segHeight).coerceAtLeast(0f)
                                    drawRoundRect(
                                        color = if (index == safeSelectedIndex) color else color.copy(alpha = 0.78f),
                                        topLeft = Offset(leftX, topY),
                                        size = Size(barWidth, currentBottom - topY),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                    currentBottom = topY
                                }
                            }
                        }
                    } else {
                        val subBarWidth = ((barSlotWidth * 0.68f) / 3f).coerceAtMost(10.dp.toPx())
                        val gap = 2.dp.toPx()
                        val groupWidth = subBarWidth * 3f + gap * 2f

                        days.forEachIndexed { index, day ->
                            val centerX = barSlotWidth * index + barSlotWidth / 2f
                            val startX = centerX - groupWidth / 2f
                            val subjects = listOf(
                                day.physicsMinutes to PhysicsColor,
                                day.chemistryMinutes to ChemistryColor,
                                (day.botanyMinutes + day.zoologyMinutes) to biologyColor
                            )
                            subjects.forEachIndexed { sIdx, (minutes, color) ->
                                val barX = startX + sIdx * (subBarWidth + gap)
                                drawRoundRect(
                                    color = trackColor,
                                    topLeft = Offset(barX, 0f),
                                    size = Size(subBarWidth, chartHeight),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                )
                                if (minutes > 0) {
                                    val h = ((minutes / 60f) / maxHours * chartHeight).coerceAtLeast(3.dp.toPx())
                                    drawRoundRect(
                                        color = if (index == safeSelectedIndex) color else color.copy(alpha = 0.78f),
                                        topLeft = Offset(barX, (chartHeight - h).coerceAtLeast(0f)),
                                        size = Size(subBarWidth, h),
                                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    days.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { selectedDayIndex = index }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp)
        ) {
            days.forEachIndexed { index, day ->
                val isSelected = index == safeSelectedIndex
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.dayLabel.take(3),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MiniSubjectStat(code: String, minutes: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(7.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "$code ${minutes}m",
            style = MonoMetricSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MockScoreTrajectoryChart(
    tests: List<MockTestEntity>,
    targetScore: Int,
    modifier: Modifier = Modifier
) {
    if (tests.isEmpty()) return
    val sorted = remember(tests) { tests.sortedBy { it.testDateMillis } }
    val primaryColor = MaterialTheme.colorScheme.primary
    val targetLineColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val surfaceColor = MaterialTheme.colorScheme.surface

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .padding(top = 8.dp, bottom = 4.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val horizontalPad = 14.dp.toPx()
                val usableW = (w - horizontalPad * 2).coerceAtLeast(1f)
                val maxScale = 720f
                val minScale = 0f

                // Reference horizontal grid lines (180, 360, 540, 720)
                listOf(180, 360, 540, 720).forEach { mark ->
                    val y = h - ((mark - minScale) / (maxScale - minScale)) * h
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Target score dashed line
                val targetY = h - (targetScore.toFloat() / maxScale) * h
                drawLine(
                    color = targetLineColor,
                    start = Offset(0f, targetY),
                    end = Offset(w, targetY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )

                val points = sorted.mapIndexed { idx, test ->
                    val x = if (sorted.size == 1) {
                        w / 2f
                    } else {
                        horizontalPad + (idx.toFloat() / (sorted.size - 1)) * usableW
                    }
                    val y = h - (test.totalScore.coerceIn(0, 720) / maxScale) * h
                    Offset(x, y)
                }

                if (points.size >= 2) {
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val midX = (prev.x + curr.x) / 2f
                            cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(points.last().x, h)
                        lineTo(points.first().x, h)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.28f),
                                primaryColor.copy(alpha = 0.01f)
                            )
                        )
                    )

                    drawPath(
                        path = linePath,
                        color = primaryColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                points.forEach { pt ->
                    drawCircle(
                        color = surfaceColor,
                        radius = 6.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Scale: 0 – 720 marks",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Dashed: Goal $targetScore",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun FocusTimerRing(
    progress: Float,
    isRunning: Boolean = false,
    accentColor: Color = Color(0xFF4285F4),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350),
        label = "timerRingProgress"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isRunning) 0.22f else 0.08f,
        animationSpec = tween(durationMillis = 350),
        label = "timerRingGlow"
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    val geminiColors = listOf(
        Color(0xFF4285F4),
        Color(0xFF9B72CB),
        Color(0xFFD96570),
        Color(0xFF4285F4)
    )

    Box(
        modifier = modifier.testTag("focus_timer_ring"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val ringPad = 14.dp.toPx()
            val inset = ringPad + strokeWidth / 2f
            val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
            val topLeft = Offset(inset, inset)

            // Lightweight soft radial glow behind the stopwatch ring (zero heavy blur)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4285F4).copy(alpha = glowAlpha),
                        Color(0xFF9B72CB).copy(alpha = glowAlpha * 0.55f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.minDimension * 0.52f
                )
            )

            // Minimal background track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Subtle ambient gradient halo ring when running, or active sweep progress
            val sweepAngle = if (isRunning && animatedProgress <= 0.01f) 360f else 360f * animatedProgress
            if (isRunning) {
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4285F4).copy(alpha = 0.28f),
                            Color(0xFF9B72CB).copy(alpha = 0.28f),
                            Color(0xFFD96570).copy(alpha = 0.28f)
                        )
                    ),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            if (sweepAngle > 0f) {
                drawArc(
                    brush = Brush.linearGradient(
                        colors = geminiColors
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}
