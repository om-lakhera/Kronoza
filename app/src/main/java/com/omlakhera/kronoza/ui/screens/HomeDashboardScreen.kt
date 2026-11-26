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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.omlakhera.kronoza.data.local.EventEntity
import com.omlakhera.kronoza.data.local.TaskEntity
import com.omlakhera.kronoza.model.EventCategory
import com.omlakhera.kronoza.model.RepeatFrequency
import com.omlakhera.kronoza.model.TaskPriority
import com.omlakhera.kronoza.model.UserPreferences
import com.omlakhera.kronoza.ui.components.KronozaAnimatedCheckbox
import com.omlakhera.kronoza.ui.components.KronozaCard
import com.omlakhera.kronoza.ui.components.KronozaEmptyStateCard
import com.omlakhera.kronoza.ui.components.KronozaIcon
import com.omlakhera.kronoza.ui.components.KronozaIcons
import com.omlakhera.kronoza.ui.components.KronozaMotion
import com.omlakhera.kronoza.ui.components.KronozaStaggeredReveal
import com.omlakhera.kronoza.ui.components.LocalReducedMotion
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors
import com.omlakhera.kronoza.util.CalendarEngine
import com.omlakhera.kronoza.util.EventOccurrence
import com.omlakhera.kronoza.util.KronozaDate
import com.omlakhera.kronoza.util.TaskFilterSort
import java.util.Calendar

@Composable
fun HomeDashboardScreen(
    events: List<EventEntity>,
    tasks: List<TaskEntity>,
    selectedDate: KronozaDate,
    displayedMonth: Pair<Int, Int>,
    preferences: UserPreferences,
    onSelectDate: (KronozaDate) -> Unit,
    onShiftMonth: (Int) -> Unit,
    onOpenFullDay: () -> Unit,
    onOpenEventDetails: (Long, Long) -> Unit,
    onCreateEventForDate: (KronozaDate) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onEditTask: (Long) -> Unit,
    onOpenTasksTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val reduced = LocalReducedMotion.current
    val today = remember { KronozaDate.today() }
    val nowMinutes = remember { KronozaDate.currentTimeMinutes() }
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    val firstName = preferences.userName.trim().substringBefore(" ")
    val greetingTitle = if (firstName.isNotBlank()) "$greeting, $firstName" else greeting

    var miniCalExpanded by rememberSaveable { mutableStateOf(true) }
    var monthDirection by remember { mutableIntStateOf(1) }

    val selectedDayOccurrences = remember(events, selectedDate) {
        CalendarEngine.occurrencesOnDate(events, selectedDate.epochDay)
    }

    val pendingTaskCount = remember(tasks) { tasks.count { !it.completed } }
    val overdueTaskCount = remember(tasks, today, nowMinutes) {
        tasks.count { TaskFilterSort.isOverdue(it, today.epochDay, nowMinutes) }
    }

    val focusTasks = remember(tasks, selectedDate, today, nowMinutes) {
        tasks.filter { task ->
            !task.completed && (
                task.dueEpochDay == selectedDate.epochDay ||
                    TaskFilterSort.isOverdue(task, today.epochDay, nowMinutes)
                )
        }.sortedWith(compareBy({ it.dueEpochDay }, { it.priority.sortWeight }, { it.dueMinutes }))
            .take(4)
    }

    val tomorrow = remember(selectedDate) { selectedDate.plusDays(1) }
    val upcomingOccurrences = remember(events, tomorrow) {
        CalendarEngine.expandEventsInRange(
            events = events,
            startEpochDay = tomorrow.epochDay,
            endEpochDay = tomorrow.plusDays(14).epochDay
        ).take(4)
    }

    val (dispYear, dispMonth) = displayedMonth
    val monthGrid = remember(dispYear, dispMonth, preferences.startWeekMonday) {
        KronozaDate.buildMonthGrid(dispYear, dispMonth, preferences.startWeekMonday)
    }
    val gridOccurrencesByDay = remember(events, monthGrid) {
        val first = monthGrid.firstOrNull()?.epochDay ?: today.epochDay
        val last = monthGrid.lastOrNull()?.epochDay ?: today.epochDay
        CalendarEngine.expandEventsInRange(events, first, last).groupBy { it.occurrenceEpochDay }
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
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Greeting Header
            item {
                KronozaStaggeredReveal(index = 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${today.weekdayName()}, ${today.formatWithPreference(preferences.dateFormat)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ext.textSecondary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = greetingTitle,
                                style = MaterialTheme.typography.displayLarge,
                                color = ext.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = ext.lavenderContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable(onClick = onOpenFullDay)
                                .minimumInteractiveComponentSize()
                                .testTag("home_today_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Clock,
                                    contentDescription = null,
                                    tint = ext.lavenderAccent,
                                    size = 15.dp
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "${selectedDayOccurrences.size} today",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.SemiBold,
                                    color = ext.onLavenderContainer
                                )
                            }
                        }
                    }
                }
            }

            // Compact Status Summary Strip
            item {
                KronozaStaggeredReveal(index = 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HomeSummaryPill(
                            icon = KronozaIcons.CalendarOutline,
                            label = if (selectedDate == today) "Today's Events" else selectedDate.formatShortDate(),
                            value = selectedDayOccurrences.size.toString(),
                            container = ext.lavenderContainer.copy(alpha = 0.65f),
                            accent = ext.lavenderAccent,
                            onClick = onOpenFullDay,
                            modifier = Modifier.weight(1f)
                        )
                        HomeSummaryPill(
                            icon = KronozaIcons.TasksOutline,
                            label = if (overdueTaskCount > 0) "$overdueTaskCount Overdue" else "Pending Tasks",
                            value = pendingTaskCount.toString(),
                            container = if (overdueTaskCount > 0) ext.coralContainer.copy(alpha = 0.8f) else ext.mintContainer.copy(alpha = 0.7f),
                            accent = if (overdueTaskCount > 0) ext.coralAccent else ext.mintAccent,
                            onClick = onOpenTasksTab,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Expandable Embedded Mini Calendar Card
            item {
                KronozaStaggeredReveal(index = 2) {
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = KronozaDate(dispYear, dispMonth, 1).formatMonthYear(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = ext.textPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = ext.mutedSurface
                                ) {
                                    Text(
                                        text = "W${selectedDate.weekOfYear}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                        color = ext.textSecondary,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                if (miniCalExpanded) {
                                    IconButton(
                                        onClick = {
                                            monthDirection = -1
                                            onShiftMonth(-1)
                                        },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("home_prev_month")
                                    ) {
                                        KronozaIcon(
                                            imageVector = KronozaIcons.ChevronLeft,
                                            contentDescription = "Previous month",
                                            tint = ext.textSecondary,
                                            size = 18.dp
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            monthDirection = 1
                                            onShiftMonth(1)
                                        },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("home_next_month")
                                    ) {
                                        KronozaIcon(
                                            imageVector = KronozaIcons.ChevronRight,
                                            contentDescription = "Next month",
                                            tint = ext.textSecondary,
                                            size = 18.dp
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { miniCalExpanded = !miniCalExpanded },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("home_toggle_calendar")
                                ) {
                                    KronozaIcon(
                                        imageVector = if (miniCalExpanded) KronozaIcons.ExpandLess else KronozaIcons.ExpandMore,
                                        contentDescription = if (miniCalExpanded) "Collapse calendar" else "Expand calendar",
                                        tint = ext.textSecondary,
                                        size = 18.dp
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = miniCalExpanded,
                                enter = expandVertically(KronozaMotion.mediumTween(reduced)) + fadeIn(KronozaMotion.fastTween(reduced)),
                                exit = shrinkVertically(KronozaMotion.mediumTween(reduced)) + fadeOut(KronozaMotion.fastTween(reduced))
                            ) {
                                Column {
                                    Spacer(Modifier.height(10.dp))
                                    val weekdayHeaders = if (preferences.startWeekMonday) {
                                        listOf("M", "T", "W", "T", "F", "S", "S")
                                    } else {
                                        listOf("S", "M", "T", "W", "T", "F", "S")
                                    }
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        weekdayHeaders.forEach { dayChar ->
                                            Text(
                                                text = dayChar,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ext.textSecondary,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))

                                    AnimatedContent(
                                        targetState = dispYear to dispMonth,
                                        transitionSpec = { KronozaMotion.directionalSlideTransform(monthDirection, reduced) },
                                        label = "home_month_grid"
                                    ) { (y, m) ->
                                        val grid = remember(y, m, preferences.startWeekMonday) {
                                            KronozaDate.buildMonthGrid(y, m, preferences.startWeekMonday)
                                        }
                                        Column {
                                            grid.chunked(7).forEach { week ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    week.forEach { date ->
                                                        val inMonth = date.month == m && date.year == y
                                                        val isSelected = date == selectedDate
                                                        val isToday = date == today
                                                        val dayDots = gridOccurrencesByDay[date.epochDay]
                                                            .orEmpty()
                                                            .map { ext.eventAccent(it.event) }
                                                            .distinct()
                                                            .take(3)

                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(40.dp)
                                                                .clip(CircleShape)
                                                                .clickable { onSelectDate(date) }
                                                                .testTag("home_day_${date.epochDay}"),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(36.dp)
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
                                                                    if (dayDots.isNotEmpty()) {
                                                                        Spacer(Modifier.height(1.dp))
                                                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                                            dayDots.forEach { dotColor ->
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
                            }
                        }
                    }
                }
            }

            // Today's Schedule Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedDate == today) "Today's Schedule" else "${selectedDate.formatShortDate()} Schedule",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ext.textPrimary
                    )
                    Text(
                        text = "Full day",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenFullDay)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("home_full_day_link")
                    )
                }
            }

            if (selectedDayOccurrences.isEmpty()) {
                item {
                    KronozaEmptyStateCard(
                        icon = KronozaIcons.CalendarOutline,
                        title = "Clear schedule for ${if (selectedDate == today) "today" else selectedDate.formatShortDate()}",
                        subtitle = "Enjoy focused uninterrupted time, or tap to schedule a block.",
                        actionLabel = "Add event",
                        onAction = { onCreateEventForDate(selectedDate) }
                    )
                }
            } else {
                items(
                    items = selectedDayOccurrences,
                    key = { "home_ev_${it.event.id}_${it.occurrenceEpochDay}" }
                ) { occ ->
                    HomeScheduleEventCard(
                        occurrence = occ,
                        use24h = preferences.timeFormat24h,
                        onClick = { onOpenEventDetails(occ.event.id, occ.occurrenceEpochDay) }
                    )
                }
            }

            // Tasks Due & Overdue Section
            if (focusTasks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tasks & Priorities",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ext.textPrimary
                        )
                        Text(
                            text = "All tasks",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenTasksTab)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("home_all_tasks_link")
                        )
                    }
                }
                items(focusTasks, key = { "home_task_${it.id}" }) { task ->
                    val overdue = TaskFilterSort.isOverdue(task, today.epochDay, nowMinutes)
                    val priorityColor = when (task.priority) {
                        TaskPriority.HIGH -> ext.coralAccent
                        TaskPriority.MEDIUM -> ext.lavenderAccent
                        TaskPriority.LOW -> ext.mintAccent
                    }
                    KronozaCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_task_card_${task.id}"),
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
                                onToggle = { onToggleTask(task) },
                                modifier = Modifier.testTag("home_task_check_${task.id}")
                            )
                            Spacer(Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ext.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${KronozaDate.fromEpochDay(task.dueEpochDay).formatWithPreference(preferences.dateFormat)} • ${KronozaDate.formatTime(task.dueMinutes, preferences.timeFormat24h)}",
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
                            } else {
                                Surface(shape = RoundedCornerShape(999.dp), color = ext.mutedSurface) {
                                    Text(
                                        text = task.category.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.textSecondary,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Upcoming Events Section
            if (upcomingOccurrences.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming Events",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ext.textPrimary
                        )
                        Text(
                            text = tomorrow.formatWeekdayShortDate(),
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.textSecondary
                        )
                    }
                }

                items(
                    items = upcomingOccurrences,
                    key = { "upcoming_${it.event.id}_${it.occurrenceEpochDay}" }
                ) { occ ->
                    UpcomingEventRowCard(
                        occurrence = occ,
                        preferences = preferences,
                        onClick = { onOpenEventDetails(occ.event.id, occ.occurrenceEpochDay) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSummaryPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    container: Color,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    KronozaCard(
        containerColor = ext.card,
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(container),
                contentAlignment = Alignment.Center
            ) {
                KronozaIcon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    size = 18.dp
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                    color = ext.textPrimary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HomeScheduleEventCard(
    occurrence: EventOccurrence,
    use24h: Boolean,
    onClick: () -> Unit
) {
    val ext = LocalKronozaColors.current
    val event = occurrence.event
    val accent = ext.eventAccent(event)
    val badgeBg = ext.eventContainer(event)
    val badgeFg = ext.onEventContainer(event)

    KronozaCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_event_card_${event.id}"),
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
                    .height(54.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
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
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        event.allDay -> "All day"
                        occurrence.isMultiDayContinuation -> "Continues • ends ${KronozaDate.formatTime(occurrence.spanEndMinutes, use24h)}"
                        else -> KronozaDate.formatTimeRange(occurrence.spanStartMinutes, occurrence.spanEndMinutes, use24h)
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                    color = ext.textSecondary
                )
                if (event.location.isNotBlank() || event.attendees.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KronozaIcon(
                            imageVector = if (event.meetingUrl.isNotBlank()) KronozaIcons.VideoCall else KronozaIcons.LocationPin,
                            contentDescription = null,
                            tint = ext.textSecondary,
                            size = 14.dp
                        )
                        Spacer(Modifier.width(4.dp))
                        val metaText = buildString {
                            if (event.location.isNotBlank()) append(event.location.substringBefore("•").trim())
                            if (event.attendees.isNotEmpty()) {
                                if (isNotEmpty()) append(" • ")
                                append("${event.attendees.size} attendees")
                            }
                        }
                        Text(
                            text = metaText,
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            val pillLabel = event.badgeLabel.ifBlank { event.category.displayName }
            Spacer(Modifier.width(10.dp))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = badgeBg
            ) {
                Text(
                    text = pillLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = badgeFg,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun UpcomingEventRowCard(
    occurrence: EventOccurrence,
    preferences: UserPreferences,
    onClick: () -> Unit
) {
    val ext = LocalKronozaColors.current
    val event = occurrence.event
    val container = ext.eventContainer(event)
    val accent = ext.eventAccent(event)
    val icon = when (event.category) {
        EventCategory.WORK -> if (event.meetingUrl.isNotBlank()) KronozaIcons.VideoCall else KronozaIcons.CalendarOutline
        EventCategory.DEEP_WORK -> KronozaIcons.Bolt
        EventCategory.PERSONAL -> KronozaIcons.Clock
        EventCategory.DEADLINE -> KronozaIcons.Flag
    }
    val occDate = remember(occurrence.occurrenceEpochDay) {
        KronozaDate.fromEpochDay(occurrence.occurrenceEpochDay)
    }

    KronozaCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_upcoming_${event.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(container),
                contentAlignment = Alignment.Center
            ) {
                KronozaIcon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    size = 19.dp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = ext.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                val timePart = if (event.allDay) "All day" else KronozaDate.formatTime(occurrence.spanStartMinutes, preferences.timeFormat24h)
                Text(
                    text = "${occDate.formatWithPreference(preferences.dateFormat)} • $timePart",
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                    color = ext.textSecondary
                )
            }
            KronozaIcon(
                imageVector = KronozaIcons.ChevronRight,
                contentDescription = "Open event",
                tint = ext.textSecondary,
                size = 18.dp
            )
        }
    }
}
