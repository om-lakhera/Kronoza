package com.omlakhera.kronoza.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.omlakhera.kronoza.data.local.EventEntity
import com.omlakhera.kronoza.data.local.TaskEntity
import com.omlakhera.kronoza.model.CalendarViewMode
import com.omlakhera.kronoza.model.RepeatFrequency
import com.omlakhera.kronoza.model.TaskPriority
import com.omlakhera.kronoza.model.UserPreferences
import com.omlakhera.kronoza.ui.components.CalendarSegmentedToggle
import com.omlakhera.kronoza.ui.components.KronozaAnimatedCheckbox
import com.omlakhera.kronoza.ui.components.KronozaCard
import com.omlakhera.kronoza.ui.components.KronozaEmptyStateCard
import com.omlakhera.kronoza.ui.components.KronozaIcon
import com.omlakhera.kronoza.ui.components.KronozaIcons
import com.omlakhera.kronoza.ui.components.KronozaMotion
import com.omlakhera.kronoza.ui.components.LocalReducedMotion
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors
import com.omlakhera.kronoza.util.CalendarEngine
import com.omlakhera.kronoza.util.EventOccurrence
import com.omlakhera.kronoza.util.KronozaDate
import com.omlakhera.kronoza.util.TaskFilterSort

@Composable
fun MonthlyCalendarScreen(
    events: List<EventEntity>,
    tasks: List<TaskEntity>,
    selectedDate: KronozaDate,
    displayedMonth: Pair<Int, Int>,
    preferences: UserPreferences,
    onSelectDate: (KronozaDate) -> Unit,
    onJumpToToday: () -> Unit,
    onShiftMonth: (Int) -> Unit,
    onChangeViewMode: (CalendarViewMode) -> Unit,
    onOpenEventDetails: (Long, Long) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onEditTask: (Long) -> Unit,
    onCreateEventForDate: (KronozaDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val reduced = LocalReducedMotion.current
    val today = remember { KronozaDate.today() }
    val nowMinutes = remember { KronozaDate.currentTimeMinutes() }
    val (dispYear, dispMonth) = displayedMonth

    var gridExpanded by rememberSaveable { mutableStateOf(true) }
    var monthDirection by remember { mutableIntStateOf(1) }

    val monthGrid = remember(dispYear, dispMonth, preferences.startWeekMonday) {
        KronozaDate.buildMonthGrid(dispYear, dispMonth, preferences.startWeekMonday)
    }
    val gridOccurrencesByDay = remember(events, monthGrid) {
        val first = monthGrid.firstOrNull()?.epochDay ?: today.epochDay
        val last = monthGrid.lastOrNull()?.epochDay ?: today.epochDay
        CalendarEngine.expandEventsInRange(events, first, last).groupBy { it.occurrenceEpochDay }
    }
    val tasksByDay = remember(tasks) { tasks.filter { !it.completed }.groupBy { it.dueEpochDay } }

    val monthEventCount = remember(events, dispYear, dispMonth) {
        val start = KronozaDate(dispYear, dispMonth, 1).epochDay
        val end = start + KronozaDate.daysInMonth(dispYear, dispMonth) - 1
        CalendarEngine.expandEventsInRange(events, start, end).size
    }

    val selectedDayOccurrences = remember(events, selectedDate) {
        CalendarEngine.occurrencesOnDate(events, selectedDate.epochDay)
    }
    val selectedDayTasks = remember(tasks, selectedDate) {
        tasks.filter { it.dueEpochDay == selectedDate.epochDay }
            .sortedWith(compareBy({ it.completed }, { it.dueMinutes }))
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
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CalendarSegmentedToggle(
                        selectedMode = CalendarViewMode.MONTH,
                        onSelectMode = onChangeViewMode
                    )
                }
            }

            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = KronozaDate(dispYear, dispMonth, 1).formatMonthYear(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = ext.textPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = ext.lavenderContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable(onClick = onJumpToToday)
                                    .testTag("month_today_btn")
                            ) {
                                Text(
                                    text = if (selectedDate == today) "$monthEventCount Events" else "Today",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.SemiBold,
                                    color = ext.onLavenderContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            if (gridExpanded) {
                                Surface(
                                    shape = CircleShape,
                                    color = ext.mutedSurface,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            monthDirection = -1
                                            onShiftMonth(-1)
                                        },
                                        modifier = Modifier.testTag("month_prev_btn")
                                    ) {
                                        KronozaIcon(
                                            imageVector = KronozaIcons.ChevronLeft,
                                            contentDescription = "Previous month",
                                            tint = ext.textPrimary,
                                            size = 18.dp
                                        )
                                    }
                                }
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = ext.mutedSurface,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            monthDirection = 1
                                            onShiftMonth(1)
                                        },
                                        modifier = Modifier.testTag("month_next_btn")
                                    ) {
                                        KronozaIcon(
                                            imageVector = KronozaIcons.ChevronRight,
                                            contentDescription = "Next month",
                                            tint = ext.textPrimary,
                                            size = 18.dp
                                        )
                                    }
                                }
                                Spacer(Modifier.width(4.dp))
                            }
                            IconButton(
                                onClick = { gridExpanded = !gridExpanded },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("month_toggle_grid")
                            ) {
                                KronozaIcon(
                                    imageVector = if (gridExpanded) KronozaIcons.ExpandLess else KronozaIcons.ExpandMore,
                                    contentDescription = if (gridExpanded) "Collapse month" else "Expand month",
                                    tint = ext.textSecondary,
                                    size = 18.dp
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = gridExpanded,
                            enter = expandVertically(KronozaMotion.mediumTween(reduced)) + fadeIn(KronozaMotion.fastTween(reduced)),
                            exit = shrinkVertically(KronozaMotion.mediumTween(reduced)) + fadeOut(KronozaMotion.fastTween(reduced))
                        ) {
                            Column {
                                Spacer(Modifier.height(14.dp))
                                val headers = if (preferences.startWeekMonday) {
                                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                } else {
                                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    headers.forEach { label ->
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ext.textSecondary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(10.dp))

                                AnimatedContent(
                                    targetState = dispYear to dispMonth,
                                    transitionSpec = { KronozaMotion.directionalSlideTransform(monthDirection, reduced) },
                                    label = "month_grid_anim"
                                ) { (y, m) ->
                                    val grid = remember(y, m, preferences.startWeekMonday) {
                                        KronozaDate.buildMonthGrid(y, m, preferences.startWeekMonday)
                                    }
                                    Column {
                                        grid.chunked(7).forEach { week ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                week.forEach { date ->
                                                    val inMonth = date.month == m && date.year == y
                                                    val isSelected = date == selectedDate
                                                    val isToday = date == today
                                                    val dots = gridOccurrencesByDay[date.epochDay]
                                                        .orEmpty()
                                                        .map { ext.eventAccent(it.event) }
                                                        .distinct()
                                                        .take(3)
                                                    val hasDayTasks = tasksByDay[date.epochDay]?.isNotEmpty() == true

                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(44.dp)
                                                            .clip(CircleShape)
                                                            .clickable { onSelectDate(date) }
                                                            .testTag("month_day_${date.epochDay}"),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(40.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    when {
                                                                        isSelected -> MaterialTheme.colorScheme.primary
                                                                        isToday -> ext.lavenderContainer
                                                                        else -> Color.Transparent
                                                                    }
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Column(
                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                verticalArrangement = Arrangement.Center
                                                            ) {
                                                                Text(
                                                                    text = date.dayOfMonth.toString(),
                                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                                        fontFeatureSettings = "tnum"
                                                                    ),
                                                                    fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Medium,
                                                                    color = when {
                                                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                                                        !inMonth -> ext.textSecondary.copy(alpha = 0.4f)
                                                                        else -> ext.textPrimary
                                                                    }
                                                                )
                                                                if (dots.isNotEmpty() || hasDayTasks) {
                                                                    Spacer(Modifier.height(2.dp))
                                                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                                        dots.forEach { dotColor ->
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .size(4.dp)
                                                                                    .clip(CircleShape)
                                                                                    .background(
                                                                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                                                        else dotColor
                                                                                    )
                                                                            )
                                                                        }
                                                                        if (hasDayTasks && dots.size < 3) {
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .size(4.dp)
                                                                                    .clip(RoundedCornerShape(1.dp))
                                                                                    .background(
                                                                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                                                        else ext.coralAccent
                                                                                    )
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CalendarLegendDot("Work", ext.lavenderAccent)
                                    Spacer(Modifier.width(14.dp))
                                    CalendarLegendDot("Deep Work", ext.skyBlueAccent)
                                    Spacer(Modifier.width(14.dp))
                                    CalendarLegendDot("Personal", ext.mintAccent)
                                    Spacer(Modifier.width(14.dp))
                                    CalendarLegendDot("Tasks", ext.coralAccent)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDate.formatWithPreference(preferences.dateFormat),
                        style = MaterialTheme.typography.headlineSmall,
                        color = ext.textPrimary
                    )
                    Text(
                        text = "${selectedDayOccurrences.size} events • ${selectedDayTasks.size} tasks",
                        style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (selectedDayOccurrences.isEmpty() && selectedDayTasks.isEmpty()) {
                item {
                    KronozaEmptyStateCard(
                        icon = KronozaIcons.Plus,
                        title = "No events on ${selectedDate.formatShortDate()}",
                        subtitle = "Tap to schedule a meeting, focus block, or milestone.",
                        actionLabel = "Schedule event",
                        onAction = { onCreateEventForDate(selectedDate) }
                    )
                }
            } else {
                items(
                    items = selectedDayOccurrences,
                    key = { "month_ev_${it.event.id}_${it.occurrenceEpochDay}" }
                ) { occ ->
                    SelectedDayEventCard(
                        occurrence = occ,
                        use24h = preferences.timeFormat24h,
                        onClick = { onOpenEventDetails(occ.event.id, occ.occurrenceEpochDay) }
                    )
                }
                items(selectedDayTasks, key = { "month_tk_${it.id}" }) { task ->
                    val overdue = TaskFilterSort.isOverdue(task, today.epochDay, nowMinutes)
                    val priorityColor = when (task.priority) {
                        TaskPriority.HIGH -> ext.coralAccent
                        TaskPriority.MEDIUM -> ext.lavenderAccent
                        TaskPriority.LOW -> ext.mintAccent
                    }
                    KronozaCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("month_task_${task.id}"),
                        onClick = { onEditTask(task.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KronozaAnimatedCheckbox(
                                checked = task.completed,
                                accentColor = ext.mintAccent,
                                borderColor = priorityColor,
                                onToggle = { onToggleTask(task) }
                            )
                            Spacer(Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (task.completed) ext.textSecondary else ext.textPrimary,
                                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Due ${KronozaDate.formatTime(task.dueMinutes, preferences.timeFormat24h)} • ${task.category.label}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                    color = if (overdue) ext.coralAccent else ext.textSecondary
                                )
                            }
                            if (overdue) {
                                Surface(shape = RoundedCornerShape(999.dp), color = ext.coralContainer) {
                                    Text(
                                        text = "Overdue",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ext.onCoralContainer,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarLegendDot(label: String, color: Color) {
    val ext = LocalKronozaColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = ext.textSecondary)
    }
}

@Composable
private fun SelectedDayEventCard(
    occurrence: EventOccurrence,
    use24h: Boolean,
    onClick: () -> Unit
) {
    val ext = LocalKronozaColors.current
    val event = occurrence.event
    val accent = ext.eventAccent(event)
    val pillBg = ext.eventContainer(event)
    val pillFg = ext.onEventContainer(event)

    KronozaCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("month_event_${event.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = when {
                            event.allDay -> "All Day"
                            occurrence.isMultiDayContinuation -> "Continues • ${KronozaDate.formatTime(occurrence.spanEndMinutes, use24h)}"
                            else -> KronozaDate.formatTimeRange(occurrence.spanStartMinutes, occurrence.spanEndMinutes, use24h)
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        color = ext.textSecondary
                    )
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = pillBg
                    ) {
                        Text(
                            text = event.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = pillFg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    if (event.repeatFrequency != RepeatFrequency.NONE) {
                        KronozaIcon(
                            imageVector = KronozaIcons.Repeat,
                            contentDescription = "Recurring",
                            tint = ext.textSecondary,
                            size = 13.dp
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ext.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ext.mutedSurface),
                contentAlignment = Alignment.Center
            ) {
                KronozaIcon(
                    imageVector = if (event.meetingUrl.isNotBlank()) KronozaIcons.VideoCall else KronozaIcons.LocationPin,
                    contentDescription = null,
                    tint = ext.textSecondary,
                    size = 19.dp
                )
            }
        }
    }
}
