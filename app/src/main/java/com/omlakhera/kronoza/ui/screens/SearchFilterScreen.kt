package com.omlakhera.kronoza.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.omlakhera.kronoza.data.local.EventEntity
import com.omlakhera.kronoza.data.local.TaskEntity
import com.omlakhera.kronoza.model.DateFilterOption
import com.omlakhera.kronoza.model.EventCategory
import com.omlakhera.kronoza.model.PriorityFilterOption
import com.omlakhera.kronoza.model.SearchCompletionFilter
import com.omlakhera.kronoza.model.SearchEventTypeFilter
import com.omlakhera.kronoza.model.SearchFilter
import com.omlakhera.kronoza.model.SearchSortOption
import com.omlakhera.kronoza.model.TaskPriority
import com.omlakhera.kronoza.model.UserPreferences
import com.omlakhera.kronoza.ui.components.KronozaAnimatedCheckbox
import com.omlakhera.kronoza.ui.components.KronozaCard
import com.omlakhera.kronoza.ui.components.KronozaEmptyStateCard
import com.omlakhera.kronoza.ui.components.KronozaIcon
import com.omlakhera.kronoza.ui.components.KronozaIcons
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors
import com.omlakhera.kronoza.util.KronozaDate
import com.omlakhera.kronoza.util.TaskFilterSort

@Composable
fun SearchFilterScreen(
    events: List<EventEntity>,
    tasks: List<TaskEntity>,
    preferences: UserPreferences,
    onOpenEventDetails: (Long, Long) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onEditTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val today = remember { KronozaDate.today() }
    val nowMinutes = remember { KronozaDate.currentTimeMinutes() }
    val weekStart = remember(today, preferences.startWeekMonday) {
        today.startOfWeek(startMonday = preferences.startWeekMonday).epochDay
    }
    val weekEnd = remember(weekStart) { weekStart + 6 }

    var query by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(SearchFilter.ALL) }
    var dateFilter by remember { mutableStateOf(DateFilterOption.ALL) }
    var priorityFilter by remember { mutableStateOf(PriorityFilterOption.ALL) }
    var completionFilter by remember { mutableStateOf(SearchCompletionFilter.ALL) }
    var eventTypeFilter by remember { mutableStateOf(SearchEventTypeFilter.ALL) }
    var sortOption by remember { mutableStateOf(SearchSortOption.DATE) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val hasActiveFilters = query.isNotBlank() ||
        activeFilter != SearchFilter.ALL ||
        dateFilter != DateFilterOption.ALL ||
        priorityFilter != PriorityFilterOption.ALL ||
        completionFilter != SearchCompletionFilter.ALL ||
        eventTypeFilter != SearchEventTypeFilter.ALL

    val matchingEvents = remember(
        events,
        query,
        activeFilter,
        dateFilter,
        priorityFilter,
        completionFilter,
        eventTypeFilter,
        sortOption,
        today,
        weekStart
    ) {
        if (priorityFilter != PriorityFilterOption.ALL) emptyList()
        else TaskFilterSort.filterSearchEvents(
            events = events,
            query = query,
            scopeFilter = activeFilter,
            dateFilter = dateFilter,
            eventTypeFilter = eventTypeFilter,
            completionFilter = completionFilter,
            sortOption = sortOption,
            todayEpochDay = today.epochDay,
            weekStartEpochDay = weekStart,
            weekEndEpochDay = weekEnd
        )
    }

    val matchingTasks = remember(
        tasks,
        query,
        activeFilter,
        dateFilter,
        priorityFilter,
        completionFilter,
        eventTypeFilter,
        sortOption,
        today,
        nowMinutes,
        weekStart
    ) {
        if (eventTypeFilter != SearchEventTypeFilter.ALL) emptyList()
        else TaskFilterSort.filterSearchTasks(
            tasks = tasks,
            query = query,
            scopeFilter = activeFilter,
            dateFilter = dateFilter,
            priorityFilter = priorityFilter,
            completionFilter = completionFilter,
            sortOption = sortOption,
            todayEpochDay = today.epochDay,
            nowMinutes = nowMinutes,
            weekStartEpochDay = weekStart,
            weekEndEpochDay = weekEnd
        )
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
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pill Search Input + Sort Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KronozaCard(
                        cornerRadius = 999,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.Search,
                                contentDescription = null,
                                tint = ext.textSecondary,
                                size = 19.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            BasicTextField(
                                value = query,
                                onValueChange = { query = it },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ext.textPrimary),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("search_query_input"),
                                decorationBox = { inner ->
                                    if (query.isEmpty()) {
                                        Text(
                                            text = "Search events, tasks, or notes...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = ext.textSecondary
                                        )
                                    }
                                    inner()
                                }
                            )
                            if (query.isNotEmpty()) {
                                IconButton(
                                    onClick = { query = "" },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    KronozaIcon(
                                        imageVector = KronozaIcons.Close,
                                        contentDescription = "Clear search",
                                        tint = ext.textSecondary,
                                        size = 17.dp
                                    )
                                }
                            }
                        }
                    }

                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ext.card,
                            border = BorderStroke(1.dp, ext.cardBorder),
                            modifier = Modifier
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { sortMenuExpanded = true }
                                .testTag("search_sort_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Sort,
                                    contentDescription = "Sort search results",
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 18.dp
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = sortOption.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ext.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            SearchSortOption.entries.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text("Sort: ${opt.label}") },
                                    onClick = {
                                        sortOption = opt
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Scope Filter Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchFilter.entries.forEach { filter ->
                        val selected = filter == activeFilter
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else ext.card,
                            border = if (selected) null else BorderStroke(1.dp, ext.cardBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { activeFilter = filter }
                                .testTag("search_filter_${filter.name.lowercase()}")
                        ) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else ext.textPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                            )
                        }
                    }
                }
            }

            // Date & Completion Filter Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DateFilterOption.entries.forEach { opt ->
                        val selected = opt == dateFilter
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) ext.lavenderContainer else ext.mutedSurface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { dateFilter = opt }
                                .testTag("search_date_${opt.name.lowercase()}")
                        ) {
                            Text(
                                text = opt.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected) ext.onLavenderContainer else ext.textSecondary,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                            )
                        }
                    }
                    SearchCompletionFilter.entries.filter { it != SearchCompletionFilter.ALL }.forEach { comp ->
                        val selected = comp == completionFilter
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) ext.mintContainer else ext.mutedSurface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable {
                                    completionFilter = if (selected) SearchCompletionFilter.ALL else comp
                                }
                                .testTag("search_completion_${comp.name.lowercase()}")
                        ) {
                            Text(
                                text = comp.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected) ext.onMintContainer else ext.textSecondary,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Event Type & Task Priority Filter Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SearchEventTypeFilter.entries.filter { it != SearchEventTypeFilter.ALL }.forEach { typeOpt ->
                        val selected = typeOpt == eventTypeFilter
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) ext.skyBlueContainer else ext.mutedSurface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable {
                                    eventTypeFilter = if (selected) SearchEventTypeFilter.ALL else typeOpt
                                    if (eventTypeFilter != SearchEventTypeFilter.ALL) {
                                        priorityFilter = PriorityFilterOption.ALL
                                    }
                                }
                                .testTag("search_event_type_${typeOpt.name.lowercase()}")
                        ) {
                            Text(
                                text = typeOpt.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected) ext.onSkyBlueContainer else ext.textSecondary,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                            )
                        }
                    }
                    PriorityFilterOption.entries.filter { it != PriorityFilterOption.ALL }.forEach { pOpt ->
                        val selected = pOpt == priorityFilter
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) ext.coralContainer else ext.mutedSurface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable {
                                    priorityFilter = if (selected) PriorityFilterOption.ALL else pOpt
                                    if (priorityFilter != PriorityFilterOption.ALL) {
                                        eventTypeFilter = SearchEventTypeFilter.ALL
                                    }
                                }
                                .testTag("search_priority_${pOpt.name.lowercase()}")
                        ) {
                            Text(
                                text = "${pOpt.label} Priority",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected) ext.onCoralContainer else ext.textSecondary,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            if (matchingEvents.isEmpty() && matchingTasks.isEmpty()) {
                item {
                    KronozaEmptyStateCard(
                        icon = KronozaIcons.Search,
                        title = "No matching items",
                        subtitle = "Try another keyword or clear active search filters.",
                        actionLabel = if (hasActiveFilters) "Reset all filters" else null,
                        onAction = if (hasActiveFilters) {
                            {
                                query = ""
                                activeFilter = SearchFilter.ALL
                                dateFilter = DateFilterOption.ALL
                                priorityFilter = PriorityFilterOption.ALL
                                completionFilter = SearchCompletionFilter.ALL
                                eventTypeFilter = SearchEventTypeFilter.ALL
                            }
                        } else null
                    )
                }
            }

            if (matchingEvents.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Events (${matchingEvents.size})",
                            style = MaterialTheme.typography.titleLarge,
                            color = ext.textPrimary
                        )
                        Text(
                            text = "Calendar",
                            style = MaterialTheme.typography.labelMedium,
                            color = ext.textSecondary
                        )
                    }
                }
                items(matchingEvents, key = { "search_ev_${it.id}" }) { event ->
                    val bg = ext.eventContainer(event)
                    val fg = ext.onEventContainer(event)
                    val accent = ext.eventAccent(event)
                    val dateLabel = if (event.dateEpochDay == today.epochDay) "Today"
                    else KronozaDate.fromEpochDay(event.dateEpochDay).formatWithPreference(preferences.dateFormat)
                    val timeLabel = if (event.allDay) "All-day"
                    else KronozaDate.formatTimeRange(event.startMinutes, event.endMinutes, preferences.timeFormat24h)
                    val subtitleText = event.location.ifBlank { event.notes.ifBlank { event.category.displayName } }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = bg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onOpenEventDetails(event.id, event.dateEpochDay) }
                            .testTag("search_event_${event.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$dateLabel • $timeLabel",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                                    color = fg
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = highlightAllOccurrences(
                                        event.title,
                                        query,
                                        ext.lavenderContainer,
                                        ext.onLavenderContainer
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ext.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    KronozaIcon(
                                        imageVector = if (event.meetingUrl.isNotBlank()) KronozaIcons.VideoCall else KronozaIcons.LocationPin,
                                        contentDescription = null,
                                        tint = fg,
                                        size = 14.dp
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = highlightAllOccurrences(
                                            subtitleText,
                                            query,
                                            ext.lavenderContainer,
                                            ext.onLavenderContainer
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = fg,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(ext.card),
                                    contentAlignment = Alignment.Center
                                ) {
                                    KronozaIcon(
                                        imageVector = if (event.category == EventCategory.DEEP_WORK) KronozaIcons.Bolt else KronozaIcons.CalendarOutline,
                                        contentDescription = null,
                                        tint = accent,
                                        size = 18.dp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = ext.card
                                ) {
                                    Text(
                                        text = event.category.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.textPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (matchingTasks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tasks (${matchingTasks.size})",
                            style = MaterialTheme.typography.titleLarge,
                            color = ext.textPrimary
                        )
                        Text(
                            text = "To-Do",
                            style = MaterialTheme.typography.labelMedium,
                            color = ext.textSecondary
                        )
                    }
                }
                items(matchingTasks, key = { "search_task_${it.id}" }) { task ->
                    val badgeBg = when (task.priority) {
                        TaskPriority.HIGH -> ext.coralContainer
                        TaskPriority.MEDIUM -> ext.skyBlueContainer
                        TaskPriority.LOW -> ext.mintContainer
                    }
                    val badgeFg = when (task.priority) {
                        TaskPriority.HIGH -> ext.onCoralContainer
                        TaskPriority.MEDIUM -> ext.onSkyBlueContainer
                        TaskPriority.LOW -> ext.onMintContainer
                    }
                    val timeText = KronozaDate.formatTime(task.dueMinutes, preferences.timeFormat24h)
                    val dateText = when (task.dueEpochDay) {
                        today.epochDay -> "Today, $timeText"
                        today.plusDays(1).epochDay -> "Tomorrow, $timeText"
                        else -> "${KronozaDate.fromEpochDay(task.dueEpochDay).formatWithPreference(preferences.dateFormat)}, $timeText"
                    }
                    KronozaCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_task_${task.id}"),
                        onClick = { onEditTask(task.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KronozaAnimatedCheckbox(
                                checked = task.completed,
                                accentColor = ext.mintAccent,
                                borderColor = ext.textSecondary,
                                onToggle = { onToggleTask(task) },
                                boxSize = 22.dp
                            )
                            Spacer(Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = highlightAllOccurrences(
                                        task.title,
                                        query,
                                        ext.lavenderContainer,
                                        ext.onLavenderContainer
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (task.completed) ext.textSecondary else ext.textPrimary,
                                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (task.description.isNotBlank() && query.isNotBlank() &&
                                    task.description.contains(query, ignoreCase = true)
                                ) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = highlightAllOccurrences(
                                            task.description,
                                            query,
                                            ext.lavenderContainer,
                                            ext.onLavenderContainer
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ext.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    KronozaIcon(
                                        imageVector = KronozaIcons.CalendarOutline,
                                        contentDescription = null,
                                        tint = ext.textSecondary,
                                        size = 13.dp
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = dateText,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                        color = ext.textSecondary
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = badgeBg
                            ) {
                                Text(
                                    text = task.priority.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = badgeFg,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun highlightAllOccurrences(
    fullText: String,
    query: String,
    highlightBg: Color,
    highlightFg: Color
): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(fullText)
    var index = fullText.indexOf(query, ignoreCase = true)
    if (index < 0) return AnnotatedString(fullText)
    return buildAnnotatedString {
        append(fullText)
        val style = SpanStyle(
            background = highlightBg,
            color = highlightFg,
            fontWeight = FontWeight.Bold
        )
        while (index >= 0) {
            val end = (index + query.length).coerceAtMost(fullText.length)
            addStyle(style = style, start = index, end = end)
            index = fullText.indexOf(query, startIndex = end, ignoreCase = true)
        }
    }
}
