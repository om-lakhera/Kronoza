package com.omlakhera.kronoza.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors
import com.omlakhera.kronoza.util.CalendarEngine
import com.omlakhera.kronoza.util.EventOccurrence
import com.omlakhera.kronoza.util.KronozaDate
import com.omlakhera.kronoza.util.TaskFilterSort

private sealed class AgendaRow {
    data class DayHeader(val date: KronozaDate, val itemCount: Int, val isPast: Boolean) : AgendaRow()
    data class EventRow(val occurrence: EventOccurrence) : AgendaRow()
    data class TaskRow(val task: TaskEntity, val overdue: Boolean, val dayEpoch: Long) : AgendaRow()
}

@Composable
fun AgendaCalendarScreen(
    events: List<EventEntity>,
    tasks: List<TaskEntity>,
    selectedDate: KronozaDate,
    preferences: UserPreferences,
    onJumpToToday: () -> Unit,
    onChangeViewMode: (CalendarViewMode) -> Unit,
    onOpenEventDetails: (Long, Long) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onEditTask: (Long) -> Unit,
    onCreateEventForDate: (KronozaDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val today = remember { KronozaDate.today() }
    val nowMinutes = remember { KronozaDate.currentTimeMinutes() }
    val tzLabel = remember { KronozaDate.localTimezoneSummary() }
    var showPast by rememberSaveable { mutableStateOf(false) }

    val (upcomingRows, pastRows) = remember(events, tasks, selectedDate, today, nowMinutes) {
        val pastStart = today.epochDay - 21
        val futureEnd = maxOf(today.epochDay + 45, selectedDate.epochDay + 30)

        val expanded = CalendarEngine.expandEventsInRange(events, pastStart, futureEnd)
        val eventsByDay = expanded.groupBy { it.occurrenceEpochDay }
        val tasksByDay = tasks
            .filter { it.dueEpochDay in pastStart..futureEnd || (!it.completed && it.dueEpochDay < today.epochDay) }
            .groupBy { it.dueEpochDay }

        val allDays = (eventsByDay.keys + tasksByDay.keys).distinct().sorted()
        val upcoming = mutableListOf<AgendaRow>()
        val past = mutableListOf<AgendaRow>()

        allDays.forEach { dayEpoch ->
            val dayOccs = eventsByDay[dayEpoch].orEmpty()
            val dayTasks = tasksByDay[dayEpoch].orEmpty().sortedWith(compareBy({ it.completed }, { it.dueMinutes }))
            if (dayOccs.isNotEmpty() || dayTasks.isNotEmpty()) {
                val isPastDay = dayEpoch < today.epochDay
                val target = if (isPastDay) past else upcoming
                target.add(AgendaRow.DayHeader(KronozaDate.fromEpochDay(dayEpoch), dayOccs.size + dayTasks.size, isPastDay))
                dayOccs.forEach { target.add(AgendaRow.EventRow(it)) }
                dayTasks.forEach { task ->
                    target.add(
                        AgendaRow.TaskRow(
                            task = task,
                            overdue = TaskFilterSort.isOverdue(task, today.epochDay, nowMinutes),
                            dayEpoch = dayEpoch
                        )
                    )
                }
            }
        }
        upcoming to past
    }

    val pastItemCount = remember(pastRows) {
        pastRows.count { it !is AgendaRow.DayHeader }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CalendarSegmentedToggle(
                        selectedMode = CalendarViewMode.AGENDA,
                        onSelectMode = onChangeViewMode
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Agenda Timeline",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ext.textPrimary
                        )
                        Text(
                            text = tzLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.textSecondary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (pastItemCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (showPast) ext.skyBlueContainer else ext.mutedSurface,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable { showPast = !showPast }
                                    .testTag("agenda_past_toggle")
                            ) {
                                Text(
                                    text = if (showPast) "Hide Past" else "Past ($pastItemCount)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (showPast) ext.onSkyBlueContainer else ext.textSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = ext.lavenderContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable(onClick = onJumpToToday)
                                .testTag("agenda_today_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.CalendarOutline,
                                    contentDescription = null,
                                    tint = ext.onLavenderContainer,
                                    size = 15.dp
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Today",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ext.onLavenderContainer
                                )
                            }
                        }
                    }
                }
            }

            if (showPast && pastRows.isNotEmpty()) {
                item {
                    Text(
                        text = "PAST (LAST 21 DAYS)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ext.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(
                    items = pastRows,
                    key = { row ->
                        when (row) {
                            is AgendaRow.DayHeader -> "past_hdr_${row.date.epochDay}"
                            is AgendaRow.EventRow -> "past_ev_${row.occurrence.event.id}_${row.occurrence.occurrenceEpochDay}"
                            is AgendaRow.TaskRow -> "past_tk_${row.task.id}_${row.dayEpoch}"
                        }
                    }
                ) { row ->
                    AgendaRowItem(
                        row = row,
                        today = today,
                        preferences = preferences,
                        onOpenEventDetails = onOpenEventDetails,
                        onToggleTask = onToggleTask,
                        onEditTask = onEditTask
                    )
                }
                item {
                    Text(
                        text = "UPCOMING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            if (upcomingRows.isEmpty()) {
                item {
                    KronozaEmptyStateCard(
                        icon = KronozaIcons.CalendarOutline,
                        title = "Your upcoming agenda is clear",
                        subtitle = "Schedule an event or task to see your timeline come together.",
                        actionLabel = "Create event",
                        onAction = { onCreateEventForDate(selectedDate) }
                    )
                }
            } else {
                items(
                    items = upcomingRows,
                    key = { row ->
                        when (row) {
                            is AgendaRow.DayHeader -> "up_hdr_${row.date.epochDay}"
                            is AgendaRow.EventRow -> "up_ev_${row.occurrence.event.id}_${row.occurrence.occurrenceEpochDay}"
                            is AgendaRow.TaskRow -> "up_tk_${row.task.id}_${row.dayEpoch}"
                        }
                    }
                ) { row ->
                    AgendaRowItem(
                        row = row,
                        today = today,
                        preferences = preferences,
                        onOpenEventDetails = onOpenEventDetails,
                        onToggleTask = onToggleTask,
                        onEditTask = onEditTask
                    )
                }
            }
        }
    }
}

@Composable
private fun AgendaRowItem(
    row: AgendaRow,
    today: KronozaDate,
    preferences: UserPreferences,
    onOpenEventDetails: (Long, Long) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onEditTask: (Long) -> Unit
) {
    val ext = LocalKronozaColors.current
    when (row) {
        is AgendaRow.DayHeader -> {
            val formattedDate = row.date.formatWithPreference(preferences.dateFormat)
            val label = when (row.date.epochDay) {
                today.epochDay -> "Today • $formattedDate"
                today.plusDays(1).epochDay -> "Tomorrow • $formattedDate"
                today.plusDays(-1).epochDay -> "Yesterday • $formattedDate"
                else -> "${row.date.shortWeekdayName()}, $formattedDate"
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = when {
                        row.date == today -> MaterialTheme.colorScheme.primary
                        row.isPast -> ext.textSecondary
                        else -> ext.textPrimary
                    }
                )
                Text(
                    text = "${row.itemCount} items",
                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                    color = ext.textSecondary
                )
            }
        }

        is AgendaRow.EventRow -> {
            val occ = row.occurrence
            val event = occ.event
            val accent = ext.eventAccent(event)
            val pillBg = ext.eventContainer(event)
            val pillFg = ext.onEventContainer(event)
            KronozaCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("agenda_list_event_${event.id}"),
                onClick = { onOpenEventDetails(event.id, occ.occurrenceEpochDay) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(accent)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = ext.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (event.repeatFrequency != RepeatFrequency.NONE) {
                                Spacer(Modifier.width(6.dp))
                                KronozaIcon(
                                    imageVector = KronozaIcons.Repeat,
                                    contentDescription = "Recurring",
                                    tint = ext.textSecondary,
                                    size = 13.dp
                                )
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when {
                                    event.allDay -> "All day"
                                    occ.isMultiDayContinuation -> "Continues • ends ${KronozaDate.formatTime(occ.spanEndMinutes, preferences.timeFormat24h)}"
                                    else -> KronozaDate.formatTimeRange(occ.spanStartMinutes, occ.spanEndMinutes, preferences.timeFormat24h)
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                color = ext.textSecondary
                            )
                            if (event.location.isNotBlank()) {
                                Spacer(Modifier.width(8.dp))
                                KronozaIcon(
                                    imageVector = if (event.meetingUrl.isNotBlank()) KronozaIcons.VideoCall else KronozaIcons.LocationPin,
                                    contentDescription = null,
                                    tint = ext.textSecondary,
                                    size = 13.dp
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = event.location.substringBefore("•").trim(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(999.dp), color = pillBg) {
                        Text(
                            text = event.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = pillFg,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        is AgendaRow.TaskRow -> {
            val task = row.task
            val priorityColor = when (task.priority) {
                TaskPriority.HIGH -> ext.coralAccent
                TaskPriority.MEDIUM -> ext.lavenderAccent
                TaskPriority.LOW -> ext.mintAccent
            }
            KronozaCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("agenda_list_task_${task.id}"),
                onClick = { onEditTask(task.id) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
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
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Task due ${KronozaDate.formatTime(task.dueMinutes, preferences.timeFormat24h)} • ${task.category.label}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                            color = if (row.overdue) ext.coralAccent else ext.textSecondary
                        )
                    }
                    if (row.overdue) {
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
