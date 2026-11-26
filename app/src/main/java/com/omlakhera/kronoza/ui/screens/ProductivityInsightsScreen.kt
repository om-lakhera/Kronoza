package com.omlakhera.kronoza.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omlakhera.kronoza.model.InsightsPeriod
import com.omlakhera.kronoza.model.InsightsSummary
import com.omlakhera.kronoza.ui.components.KronozaCard
import com.omlakhera.kronoza.ui.components.KronozaEmptyStateCard
import com.omlakhera.kronoza.ui.components.KronozaIcon
import com.omlakhera.kronoza.ui.components.KronozaIcons
import com.omlakhera.kronoza.ui.components.KronozaMotion
import com.omlakhera.kronoza.ui.components.KronozaStaggeredReveal
import com.omlakhera.kronoza.ui.components.LocalReducedMotion
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors
import kotlin.math.roundToInt

@Composable
fun ProductivityInsightsScreen(
    summary: InsightsSummary,
    selectedPeriod: InsightsPeriod,
    onSelectPeriod: (InsightsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val reduced = LocalReducedMotion.current
    val totalCategoryHours = summary.categoryBreakdown.sumOf { it.hours }.let { (it * 10).roundToInt() / 10.0 }

    val statusLabel = when {
        !summary.hasActivityData -> "No Activity"
        summary.overdueTasks > 0 -> "${summary.overdueTasks} Overdue"
        summary.streakDays >= 3 || summary.completionRatePercent >= 70 -> "Peak Flow"
        else -> "Active Rhythm"
    }
    val statusBg = when {
        !summary.hasActivityData -> ext.mutedSurface
        summary.overdueTasks > 0 -> ext.coralContainer
        else -> ext.mintContainer
    }
    val statusFg = when {
        !summary.hasActivityData -> ext.textSecondary
        summary.overdueTasks > 0 -> ext.onCoralContainer
        else -> ext.onMintContainer
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ext.canvas),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Segmented Switch + Genuine Status Pill
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = ext.mutedSurface
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            InsightsPeriod.entries.forEach { period ->
                                val selected = period == selectedPeriod
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .clickable { onSelectPeriod(period) }
                                        .testTag("insights_period_${period.name.lowercase()}")
                                ) {
                                    Text(
                                        text = period.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary else ext.textSecondary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = statusBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KronozaIcon(
                                imageVector = if (summary.overdueTasks > 0) KronozaIcons.AlertTriangle else KronozaIcons.Bolt,
                                contentDescription = null,
                                tint = statusFg,
                                size = 14.dp
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = statusFg
                            )
                        }
                    }
                }
            }

            if (!summary.hasActivityData) {
                item {
                    KronozaEmptyStateCard(
                        icon = KronozaIcons.InsightsOutline,
                        title = "No activity recorded yet",
                        subtitle = "Schedule focus blocks or complete tasks to see genuine charts, category balance, and streak analytics."
                    )
                }
                return@LazyColumn
            }

            // 2x2 Genuine KPI Grid
            item {
                KronozaStaggeredReveal(index = 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val deltaSign = if (summary.focusDeltaPercent >= 0) "+" else ""
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            KpiTileCard(
                                icon = KronozaIcons.Bolt,
                                iconBg = ext.lavenderContainer,
                                iconTint = ext.lavenderAccent,
                                badgeText = "$deltaSign${summary.focusDeltaPercent}%",
                                badgeBg = if (summary.focusDeltaPercent >= 0) ext.mintContainer else ext.coralContainer,
                                badgeFg = if (summary.focusDeltaPercent >= 0) ext.onMintContainer else ext.onCoralContainer,
                                value = "${summary.focusHours}h",
                                label = "Focus Hours",
                                modifier = Modifier.weight(1f)
                            )
                            KpiTileCard(
                                icon = KronozaIcons.TasksFilled,
                                iconBg = ext.mintContainer,
                                iconTint = ext.mintAccent,
                                badgeText = "${summary.completionRatePercent}% rate",
                                badgeBg = ext.mutedSurface,
                                badgeFg = ext.textSecondary,
                                value = summary.tasksDone.toString(),
                                label = "Tasks Done",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            KpiTileCard(
                                icon = KronozaIcons.Clock,
                                iconBg = ext.skyBlueContainer,
                                iconTint = ext.skyBlueAccent,
                                badgeText = "${summary.eventsCount} events",
                                badgeBg = ext.skyBlueContainer,
                                badgeFg = ext.onSkyBlueContainer,
                                value = "${summary.meetingHours}h",
                                label = "Meetings",
                                modifier = Modifier.weight(1f)
                            )
                            KpiTileCard(
                                icon = KronozaIcons.Flame,
                                iconBg = ext.lavenderContainer,
                                iconTint = ext.lavenderAccent,
                                badgeText = "Best: ${summary.bestStreakDays}d",
                                badgeBg = ext.lavenderContainer,
                                badgeFg = ext.onLavenderContainer,
                                value = "${summary.streakDays}d",
                                label = "Active Streak",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Genuine Queue Strip: Pending / Overdue / Events
            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Pending Tasks", style = MaterialTheme.typography.labelSmall, color = ext.textSecondary)
                            Text(
                                text = summary.pendingTasks.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                                color = ext.textPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Overdue", style = MaterialTheme.typography.labelSmall, color = ext.textSecondary)
                            Text(
                                text = summary.overdueTasks.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                                color = if (summary.overdueTasks > 0) ext.coralAccent else ext.mintAccent
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Period Events", style = MaterialTheme.typography.labelSmall, color = ext.textSecondary)
                            Text(
                                text = summary.eventsCount.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                                color = ext.textPrimary
                            )
                        }
                    }
                }
            }

            // Focus vs. Meetings Bar Chart Card
            item {
                KronozaStaggeredReveal(index = 1) {
                    val maxVal = (summary.dailyBars.maxOfOrNull { maxOf(it.focusHours, it.meetingHours) } ?: 1f)
                        .coerceAtLeast(1f)
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "Focus vs. Meetings",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = ext.textPrimary
                                    )
                                    Text(
                                        text = "Actual scheduled hours this week",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ext.textSecondary
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ChartLegendDot("Focus", MaterialTheme.colorScheme.primary.copy(alpha = 0.65f))
                                    ChartLegendDot("Meet", ext.skyBlueAccent)
                                }
                            }
                            Spacer(Modifier.height(20.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(152.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                summary.dailyBars.forEach { bar ->
                                    val targetFocusFrac = if (bar.focusHours <= 0f) 0.04f else (bar.focusHours / maxVal).coerceIn(0.08f, 1f)
                                    val targetMeetFrac = if (bar.meetingHours <= 0f) 0.04f else (bar.meetingHours / maxVal).coerceIn(0.08f, 1f)
                                    val focusFrac by animateFloatAsState(
                                        targetValue = targetFocusFrac,
                                        animationSpec = KronozaMotion.chartTween(reduced),
                                        label = "focus_bar_${bar.dayLabel}"
                                    )
                                    val meetFrac by animateFloatAsState(
                                        targetValue = targetMeetFrac,
                                        animationSpec = KronozaMotion.chartTween(reduced),
                                        label = "meet_bar_${bar.dayLabel}"
                                    )
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .height(122.dp)
                                                .padding(horizontal = 4.dp),
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(12.dp)
                                                    .height((118f * focusFrac).dp)
                                                    .clip(RoundedCornerShape(999.dp))
                                                    .background(
                                                        when {
                                                            bar.focusHours <= 0f -> ext.mutedSurface
                                                            bar.isPeak -> MaterialTheme.colorScheme.primary
                                                            else -> ext.lavenderAccent.copy(alpha = 0.45f)
                                                        }
                                                    )
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .width(12.dp)
                                                    .height((118f * meetFrac).dp)
                                                    .clip(RoundedCornerShape(999.dp))
                                                    .background(
                                                        if (bar.meetingHours <= 0f) ext.mutedSurface
                                                        else ext.skyBlueAccent.copy(alpha = 0.72f)
                                                    )
                                            )
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = bar.dayLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (bar.isPeak) FontWeight.Bold else FontWeight.Medium,
                                            color = if (bar.isPeak) MaterialTheme.colorScheme.primary else ext.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4-Week Activity Trend Card
            if (summary.weeklyTrendPoints.isNotEmpty()) {
                item {
                    val maxHours = (summary.weeklyTrendPoints.maxOfOrNull { it.scheduledHours } ?: 1f)
                        .coerceAtLeast(1f)
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "4-Week Activity Trend",
                                style = MaterialTheme.typography.headlineSmall,
                                color = ext.textPrimary
                            )
                            summary.weeklyTrendPoints.forEach { point ->
                                val targetFrac = if (point.scheduledHours <= 0f) 0.03f
                                else (point.scheduledHours / maxHours).coerceIn(0.06f, 1f)
                                val frac by animateFloatAsState(
                                    targetValue = targetFrac,
                                    animationSpec = KronozaMotion.chartTween(reduced),
                                    label = "trend_${point.weekLabel}"
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = point.weekLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = ext.textPrimary
                                        )
                                        Text(
                                            text = "${point.scheduledHours}h scheduled • ${point.completedTasks} tasks done",
                                            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                            color = ext.textSecondary
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { frac },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(999.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = ext.mutedSurface,
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Time by Category Card
            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Time by Category",
                                style = MaterialTheme.typography.headlineSmall,
                                color = ext.textPrimary
                            )
                            Text(
                                text = "${totalCategoryHours}h total",
                                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                color = ext.textSecondary
                            )
                        }
                        if (summary.categoryBreakdown.isEmpty()) {
                            Text(
                                text = "No scheduled events in this period yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.textSecondary
                            )
                        } else {
                            summary.categoryBreakdown.forEach { share ->
                                val accent = ext.categoryAccent(share.category)
                                val animFrac by animateFloatAsState(
                                    targetValue = (share.percent / 100f).coerceIn(0.04f, 1f),
                                    animationSpec = KronozaMotion.chartTween(reduced),
                                    label = "cat_${share.label}"
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(9.dp)
                                                    .clip(CircleShape)
                                                    .background(accent)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = share.label,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = ext.textPrimary
                                            )
                                        }
                                        Text(
                                            text = "${share.hours}h · ${share.percent}%",
                                            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                            color = ext.textSecondary
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { animFrac },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(999.dp)),
                                        color = accent,
                                        trackColor = ext.mutedSurface,
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Highlight Banner Card
            item {
                KronozaCard(
                    containerColor = ext.lavenderContainer.copy(alpha = 0.6f),
                    borderColor = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ext.card),
                            contentAlignment = Alignment.Center
                        ) {
                            KronozaIcon(
                                imageVector = if (summary.overdueTasks > 0) KronozaIcons.AlertTriangle else KronozaIcons.InsightsFilled,
                                contentDescription = null,
                                tint = if (summary.overdueTasks > 0) ext.coralAccent else MaterialTheme.colorScheme.primary,
                                size = 20.dp
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = summary.highlightTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = ext.textPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = summary.highlightBody,
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.onLavenderContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiTileCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    badgeText: String,
    badgeBg: Color,
    badgeFg: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    KronozaCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    KronozaIcon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        size = 18.dp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.SemiBold,
                        color = badgeFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = "tnum"),
                color = ext.textPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = ext.textSecondary
            )
        }
    }
}

@Composable
private fun ChartLegendDot(label: String, color: Color) {
    val ext = LocalKronozaColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = ext.textSecondary)
    }
}
