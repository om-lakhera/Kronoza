package com.omlakhera.kronoza.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.omlakhera.kronoza.R
import com.omlakhera.kronoza.data.local.EventEntity
import com.omlakhera.kronoza.data.local.TaskEntity
import com.omlakhera.kronoza.model.CalendarViewMode
import com.omlakhera.kronoza.model.EventCategory
import com.omlakhera.kronoza.model.RepeatFrequency
import com.omlakhera.kronoza.model.TaskPriority
import com.omlakhera.kronoza.model.UserPreferences
import com.omlakhera.kronoza.ui.components.CalendarSegmentedToggle
import com.omlakhera.kronoza.ui.components.KronozaAnimatedCheckbox
import com.omlakhera.kronoza.ui.components.KronozaCard
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
fun DailyAgendaScreen(
    events: List<EventEntity>,
    tasks: List<TaskEntity>,
    selectedDate: KronozaDate,
    preferences: UserPreferences,
    onSelectDate: (KronozaDate) -> Unit,
    onChangeViewMode: (CalendarViewMode) -> Unit,
    onOpenEventDetails: (Long, Long) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onEditTask: (Long) -> Unit,
    onCreateEventAtHour: (KronozaDate, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val reduced = LocalReducedMotion.current
    val today = remember { KronozaDate.today() }
    val nowMinutes = remember { KronozaDate.currentTimeMinutes() }
    var dayDirection by remember { mutableIntStateOf(1) }

    val weekStart = remember(selectedDate, preferences.startWeekMonday) {
        selectedDate.startOfWeek(preferences.startWeekMonday)
    }
    val weekDays = remember(weekStart) { List(7) { weekStart.plusDays(it) } }
    val weekOccurrencesByDay = remember(events, weekStart) {
        CalendarEngine.expandEventsInRange(events, weekStart.epochDay, weekStart.plusDays(6).epochDay)
            .groupBy { it.occurrenceEpochDay }
    }

    val dayOccurrences = remember(events, selectedDate) {
        CalendarEngine.occurrencesOnDate(events, selectedDate.epochDay)
    }
    val allDayOccurrences = remember(dayOccurrences) {
        dayOccurrences.filter { it.event.allDay }
    }
    val placedTimedEvents = remember(dayOccurrences) {
        CalendarEngine.layoutOverlappingEvents(dayOccurrences)
    }

    val dayTasks = remember(tasks, selectedDate) {
        tasks.filter { it.dueEpochDay == selectedDate.epochDay }
            .sortedWith(compareBy({ it.completed }, { it.dueMinutes }))
    }

    val startHour = 7
    val endHour = 20
    val tzLabel = remember { KronozaDate.localTimezoneOffsetLabel() }

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
            // Top Month + Day Header + Segmented Toggle
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CalendarSegmentedToggle(
                            selectedMode = CalendarViewMode.DAY,
                            onSelectMode = onChangeViewMode
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${selectedDate.formatWithPreference(preferences.dateFormat)} • $tzLabel",
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.textSecondary
                            )
                            Text(
                                text = "${selectedDate.weekdayName()}, ${selectedDate.dayOfMonth}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = ext.textPrimary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(shape = CircleShape, color = ext.card, modifier = Modifier.size(38.dp)) {
                                IconButton(
                                    onClick = {
                                        dayDirection = -1
                                        onSelectDate(selectedDate.plusDays(-1))
                                    },
                                    modifier = Modifier.testTag("day_prev_btn")
                                ) {
                                    KronozaIcon(
                                        imageVector = KronozaIcons.ChevronLeft,
                                        contentDescription = "Previous day",
                                        tint = ext.textPrimary,
                                        size = 17.dp
                                    )
                                }
                            }
                            Surface(shape = CircleShape, color = ext.card, modifier = Modifier.size(38.dp)) {
                                IconButton(
                                    onClick = {
                                        dayDirection = 1
                                        onSelectDate(selectedDate.plusDays(1))
                                    },
                                    modifier = Modifier.testTag("day_next_btn")
                                ) {
                                    KronozaIcon(
                                        imageVector = KronozaIcons.ChevronRight,
                                        contentDescription = "Next day",
                                        tint = ext.textPrimary,
                                        size = 17.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Week Strip Card
            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDays.forEach { day ->
                            val isSelected = day == selectedDate
                            val firstOcc = weekOccurrencesByDay[day.epochDay]?.firstOrNull()
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        dayDirection = if (day.epochDay >= selectedDate.epochDay) 1 else -1
                                        onSelectDate(day)
                                    }
                                    .testTag("day_strip_${day.epochDay}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = day.shortWeekdayName(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                        else ext.textSecondary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = day.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else ext.textPrimary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                                    firstOcc != null -> ext.eventAccent(firstOcc.event)
                                                    else -> Color.Transparent
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // All-Day Banner Events
            if (allDayOccurrences.isNotEmpty()) {
                item {
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "All-day & Multi-day",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ext.textSecondary
                            )
                            allDayOccurrences.forEach { occ ->
                                val ev = occ.event
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = ext.eventContainer(ev),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onOpenEventDetails(ev.id, occ.occurrenceEpochDay) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(ext.eventAccent(ev))
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = ev.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = ext.textPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = ev.category.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ext.onEventContainer(ev)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Due Tasks for Selected Day
            if (dayTasks.isNotEmpty()) {
                item {
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tasks Due ${selectedDate.formatShortDate()}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ext.textPrimary
                                )
                                Text(
                                    text = "${dayTasks.count { !it.completed }} pending",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            dayTasks.forEach { task ->
                                val overdue = TaskFilterSort.isOverdue(task, today.epochDay, nowMinutes)
                                val priorityColor = when (task.priority) {
                                    TaskPriority.HIGH -> ext.coralAccent
                                    TaskPriority.MEDIUM -> ext.lavenderAccent
                                    TaskPriority.LOW -> ext.mintAccent
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ext.mutedSurface)
                                        .clickable { onEditTask(task.id) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    KronozaAnimatedCheckbox(
                                        checked = task.completed,
                                        accentColor = ext.mintAccent,
                                        borderColor = priorityColor,
                                        onToggle = { onToggleTask(task) },
                                        boxSize = 20.dp
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (task.completed) ext.textSecondary else ext.textPrimary,
                                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = KronozaDate.formatTime(task.dueMinutes, preferences.timeFormat24h),
                                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                        color = if (overdue) ext.coralAccent else ext.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Hourly Timeline Card with Animated Day Switch & Side-by-Side Overlapping Events
            item {
                AnimatedContent(
                    targetState = selectedDate,
                    transitionSpec = { KronozaMotion.directionalSlideTransform(dayDirection, reduced) },
                    label = "day_timeline_anim"
                ) { activeDate ->
                    val activeDayOccs = remember(events, activeDate) {
                        CalendarEngine.occurrencesOnDate(events, activeDate.epochDay)
                    }
                    val activePlaced = remember(activeDayOccs) {
                        CalendarEngine.layoutOverlappingEvents(activeDayOccs)
                    }

                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (hour in startHour..endHour) {
                                val hourStartMin = hour * 60
                                val hourEndMin = (hour + 1) * 60
                                val startingPlaced = activePlaced.filter {
                                    it.occurrence.spanStartMinutes in hourStartMin until hourEndMin
                                }
                                val occupiedByEarlier = activePlaced.any {
                                    it.occurrence.spanStartMinutes < hourStartMin && it.occurrence.spanEndMinutes > hourStartMin
                                }
                                val showNowLineHere = activeDate == today &&
                                    nowMinutes in hourStartMin until hourEndMin

                                if (showNowLineHere) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(2.dp)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = KronozaDate.formatTime(nowMinutes, preferences.timeFormat24h),
                                                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = KronozaDate.formatHourLabel(hour, preferences.timeFormat24h),
                                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                                        color = ext.textSecondary,
                                        modifier = Modifier
                                            .width(56.dp)
                                            .padding(top = 12.dp)
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (startingPlaced.isEmpty() && !occupiedByEarlier) {
                                            val slotTitle = when (hour) {
                                                8 -> "Quiet morning focus"
                                                16 -> "Open slot"
                                                else -> ""
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = ext.mutedSurface.copy(alpha = 0.65f),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable { onCreateEventAtHour(activeDate, hour) }
                                                    .testTag("agenda_empty_slot_$hour")
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = slotTitle,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = ext.textSecondary
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        KronozaIcon(
                                                            imageVector = KronozaIcons.Plus,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            size = 15.dp
                                                        )
                                                        Spacer(Modifier.width(3.dp))
                                                        Text(
                                                            text = "Add",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                        } else if (startingPlaced.isNotEmpty()) {
                                            // If overlapping events start in the same hour, display side-by-side cleanly
                                            if (startingPlaced.size > 1) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    startingPlaced.forEach { placed ->
                                                        DailyAgendaEventCard(
                                                            occurrence = placed.occurrence,
                                                            use24h = preferences.timeFormat24h,
                                                            compact = true,
                                                            onClick = {
                                                                onOpenEventDetails(
                                                                    placed.occurrence.event.id,
                                                                    placed.occurrence.occurrenceEpochDay
                                                                )
                                                            },
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                }
                                            } else {
                                                val placed = startingPlaced.first()
                                                DailyAgendaEventCard(
                                                    occurrence = placed.occurrence,
                                                    use24h = preferences.timeFormat24h,
                                                    compact = false,
                                                    onClick = {
                                                        onOpenEventDetails(
                                                            placed.occurrence.event.id,
                                                            placed.occurrence.occurrenceEpochDay
                                                        )
                                                    },
                                                    modifier = Modifier.fillMaxWidth()
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

@Composable
private fun DailyAgendaEventCard(
    occurrence: EventOccurrence,
    use24h: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val event = occurrence.event
    val bg = ext.eventContainer(event)
    val fg = ext.onEventContainer(event)
    val accent = ext.eventAccent(event)
    val durationMins = (occurrence.spanEndMinutes - occurrence.spanStartMinutes).coerceAtLeast(15)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bg,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("agenda_event_${event.id}")
    ) {
        if (durationMins <= 35 || compact) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = ext.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (event.repeatFrequency != RepeatFrequency.NONE && !compact) {
                        Spacer(Modifier.width(6.dp))
                        KronozaIcon(
                            imageVector = KronozaIcons.Repeat,
                            contentDescription = "Recurring",
                            tint = fg,
                            size = 13.dp
                        )
                    }
                }
                Text(
                    text = KronozaDate.formatTimeRange(occurrence.spanStartMinutes, occurrence.spanEndMinutes, use24h),
                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                    color = fg,
                    maxLines = 1
                )
            }
        } else {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (event.category == EventCategory.PERSONAL) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ext.card.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.Clock,
                                contentDescription = null,
                                tint = accent,
                                size = 19.dp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (event.category == EventCategory.DEADLINE) fg else ext.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (event.repeatFrequency != RepeatFrequency.NONE) {
                                Spacer(Modifier.width(6.dp))
                                KronozaIcon(
                                    imageVector = KronozaIcons.Repeat,
                                    contentDescription = "Recurring",
                                    tint = fg,
                                    size = 13.dp
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        val timeMeta = KronozaDate.formatTimeRange(occurrence.spanStartMinutes, occurrence.spanEndMinutes, use24h) +
                            if (event.location.isNotBlank()) " • ${event.location.substringBefore("•").trim()}" else ""
                        Text(
                            text = timeMeta,
                            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                            color = fg,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (event.isMilestone) {
                        KronozaIcon(
                            imageVector = KronozaIcons.Flag,
                            contentDescription = "Key Milestone",
                            tint = fg,
                            size = 17.dp
                        )
                    } else if (event.category == EventCategory.PERSONAL) {
                        Text(
                            text = "${durationMins}m",
                            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                            color = fg
                        )
                    }
                }

                if (event.attendees.isNotEmpty() || event.isMilestone) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (event.attendees.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val displayedAttendees = event.attendees.take(3)
                                Box(modifier = Modifier.width((26 + (displayedAttendees.size - 1).coerceAtLeast(0) * 18).dp).height(26.dp)) {
                                    displayedAttendees.forEachIndexed { idx, attendee ->
                                        val initials = attendee.name.trim()
                                            .split(Regex("\\s+"))
                                            .filter { it.isNotEmpty() }
                                            .take(2)
                                            .joinToString("") { it.first().uppercaseChar().toString() }
                                            .ifBlank { "?" }
                                        Box(
                                            modifier = Modifier
                                                .offset(x = (idx * 18).dp)
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(ext.card)
                                                .border(1.5.dp, bg, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = initials,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = fg
                                            )
                                        }
                                    }
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = ext.card.copy(alpha = 0.72f)
                            ) {
                                Text(
                                    text = "${event.attendees.size} attending",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = fg,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        } else if (event.isMilestone) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = ext.card.copy(alpha = 0.78f)
                            ) {
                                Text(
                                    text = "Key Milestone",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = fg,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "${durationMins}m",
                                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                color = fg
                            )
                        }
                    }
                }
            }
        }
    }
}
