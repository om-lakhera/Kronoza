package com.omlakhera.kronoza.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.omlakhera.kronoza.data.local.TaskEntity
import com.omlakhera.kronoza.model.TaskCategory
import com.omlakhera.kronoza.model.TaskFilter
import com.omlakhera.kronoza.model.TaskPriority
import com.omlakhera.kronoza.model.TaskSortOption
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
import com.omlakhera.kronoza.util.KronozaDate
import com.omlakhera.kronoza.util.TaskFilterSort
import kotlin.math.roundToInt

@Composable
fun TaskManagementScreen(
    tasks: List<TaskEntity>,
    preferences: UserPreferences,
    onToggleCompleted: (TaskEntity) -> Unit,
    onEditTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val reduced = LocalReducedMotion.current
    val today = remember { KronozaDate.today() }
    val nowMinutes = remember { KronozaDate.currentTimeMinutes() }
    var selectedFilter by remember { mutableStateOf(TaskFilter.ALL) }
    var selectedCategory by remember { mutableStateOf<TaskCategory?>(null) }
    var selectedSort by remember { mutableStateOf(TaskSortOption.DUE_DATE) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var completedExpanded by remember { mutableStateOf(true) }

    val overdueCount = remember(tasks, today, nowMinutes) {
        tasks.count { TaskFilterSort.isOverdue(it, today.epochDay, nowMinutes) }
    }

    val filteredSorted = remember(tasks, selectedFilter, selectedCategory, selectedSort, today, nowMinutes) {
        val filtered = TaskFilterSort.filterTasks(
            tasks = tasks,
            filter = selectedFilter,
            categoryFilter = selectedCategory,
            todayEpochDay = today.epochDay,
            nowMinutes = nowMinutes
        )
        TaskFilterSort.sortTasks(filtered, selectedSort)
    }

    val overdueTasks = remember(filteredSorted, today, nowMinutes) {
        filteredSorted.filter { TaskFilterSort.isOverdue(it, today.epochDay, nowMinutes) }
    }
    val dueTodayTasks = remember(filteredSorted, today, nowMinutes) {
        filteredSorted.filter {
            !it.completed &&
                !TaskFilterSort.isOverdue(it, today.epochDay, nowMinutes) &&
                it.dueEpochDay <= today.epochDay
        }
    }
    val upcomingTasks = remember(filteredSorted, today) {
        filteredSorted.filter { !it.completed && it.dueEpochDay > today.epochDay }
    }
    val completedTasks = remember(filteredSorted) {
        filteredSorted.filter { it.completed }
    }

    val totalCount = tasks.size
    val doneCount = tasks.count { it.completed }
    val rawProgress = if (totalCount == 0) 0f else doneCount.toFloat() / totalCount.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = KronozaMotion.chartTween(reduced),
        label = "task_progress"
    )
    val progressPercent = (rawProgress * 100f).roundToInt()

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Primary Filter Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskFilter.entries.forEach { filter ->
                        val isSelected = filter == selectedFilter
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else ext.card,
                            border = if (isSelected) null else BorderStroke(1.dp, ext.cardBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { selectedFilter = filter }
                                .testTag("task_filter_${filter.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (filter == TaskFilter.HIGH_PRIORITY || filter == TaskFilter.OVERDUE) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else ext.coralAccent)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                }
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else ext.textPrimary
                                )
                                val badgeNum = when (filter) {
                                    TaskFilter.ALL -> totalCount
                                    TaskFilter.OVERDUE -> overdueCount
                                    else -> null
                                }
                                if (badgeNum != null && (filter == TaskFilter.ALL || badgeNum > 0)) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (isSelected) Color.White.copy(alpha = 0.22f)
                                        else if (filter == TaskFilter.OVERDUE) ext.coralContainer
                                        else ext.mutedSurface
                                    ) {
                                        Text(
                                            text = badgeNum.toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else if (filter == TaskFilter.OVERDUE) ext.onCoralContainer
                                            else ext.textSecondary,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Category Filter + Sort Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CategoryChip(
                            label = "All Categories",
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            testTag = "task_cat_filter_all"
                        )
                        TaskCategory.entries.forEach { cat ->
                            CategoryChip(
                                label = cat.label,
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                                testTag = "task_cat_filter_${cat.name.lowercase()}"
                            )
                        }
                    }

                    Box {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = ext.lavenderContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { sortMenuExpanded = true }
                                .testTag("task_sort_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Sort,
                                    contentDescription = "Sort tasks",
                                    tint = ext.onLavenderContainer,
                                    size = 15.dp
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = selectedSort.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ext.onLavenderContainer
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            TaskSortOption.entries.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text("Sort by ${opt.label}") },
                                    onClick = {
                                        selectedSort = opt
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Daily Progress Card
            item {
                KronozaStaggeredReveal(index = 0) {
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "DAILY PROGRESS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = when {
                                            totalCount == 0 -> ext.lavenderContainer
                                            overdueCount > 0 -> ext.coralContainer
                                            else -> ext.mintContainer
                                        }
                                    ) {
                                        Text(
                                            text = when {
                                                totalCount == 0 -> "Ready"
                                                overdueCount > 0 -> "$overdueCount Overdue"
                                                progressPercent >= 50 -> "On Track"
                                                else -> "In Progress"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = when {
                                                totalCount == 0 -> ext.onLavenderContainer
                                                overdueCount > 0 -> ext.onCoralContainer
                                                else -> ext.onMintContainer
                                            },
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = if (totalCount == 0) "No tasks created yet" else "$doneCount of $totalCount tasks completed",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"),
                                    color = ext.textPrimary
                                )
                                Spacer(Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(999.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = ext.mutedSurface,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                            Spacer(Modifier.width(18.dp))
                            Box(
                                modifier = Modifier.size(64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier.size(64.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = ext.mutedSurface,
                                    strokeWidth = 6.dp,
                                    strokeCap = StrokeCap.Round
                                )
                                Text(
                                    text = "$progressPercent%",
                                    style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.Bold,
                                    color = ext.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            if (filteredSorted.isEmpty()) {
                item {
                    val hasAnyTasks = tasks.isNotEmpty()
                    KronozaEmptyStateCard(
                        icon = KronozaIcons.TasksOutline,
                        title = if (hasAnyTasks) "No tasks match these filters" else "No tasks yet",
                        subtitle = if (hasAnyTasks) {
                            "Tap + to add a task or reset your active filters."
                        } else {
                            "Tap + to create your first task with priorities, due times, and subtasks."
                        },
                        actionLabel = if (hasAnyTasks && (selectedFilter != TaskFilter.ALL || selectedCategory != null)) "Reset filters" else null,
                        onAction = if (hasAnyTasks && (selectedFilter != TaskFilter.ALL || selectedCategory != null)) {
                            {
                                selectedFilter = TaskFilter.ALL
                                selectedCategory = null
                            }
                        } else null,
                        accentColor = ext.mintAccent,
                        accentContainer = ext.mintContainer
                    )
                }
            }

            // Overdue Section
            if (overdueTasks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overdue",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ext.coralAccent
                        )
                        Text(
                            text = "${overdueTasks.size} needs attention",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.coralAccent
                        )
                    }
                }
                items(overdueTasks, key = { "overdue_${it.id}" }) { task ->
                    TaskRowCard(
                        task = task,
                        today = today,
                        preferences = preferences,
                        isOverdue = true,
                        onToggle = { onToggleCompleted(task) },
                        onClick = { onEditTask(task.id) }
                    )
                }
            }

            // Due Today Section
            if (dueTodayTasks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Due Today",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ext.textPrimary
                        )
                        Text(
                            text = "${dueTodayTasks.size} remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.textSecondary
                        )
                    }
                }
                items(dueTodayTasks, key = { "today_${it.id}" }) { task ->
                    TaskRowCard(
                        task = task,
                        today = today,
                        preferences = preferences,
                        isOverdue = false,
                        onToggle = { onToggleCompleted(task) },
                        onClick = { onEditTask(task.id) }
                    )
                }
            }

            // Upcoming Section
            if (upcomingTasks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ext.textPrimary
                        )
                        Text(
                            text = "${upcomingTasks.size} scheduled",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.textSecondary
                        )
                    }
                }
                items(upcomingTasks, key = { "upcoming_${it.id}" }) { task ->
                    TaskRowCard(
                        task = task,
                        today = today,
                        preferences = preferences,
                        isOverdue = false,
                        onToggle = { onToggleCompleted(task) },
                        onClick = { onEditTask(task.id) }
                    )
                }
            }

            // Completed Collapsible Section
            if (completedTasks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { completedExpanded = !completedExpanded }
                            .padding(vertical = 4.dp)
                            .testTag("tasks_completed_toggle"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KronozaIcon(
                            imageVector = KronozaIcons.TasksFilled,
                            contentDescription = null,
                            tint = ext.mintAccent,
                            size = 20.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.titleMedium,
                            color = ext.textPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = ext.mutedSurface
                        ) {
                            Text(
                                text = completedTasks.size.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                color = ext.textSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        KronozaIcon(
                            imageVector = if (completedExpanded) KronozaIcons.ExpandLess else KronozaIcons.ExpandMore,
                            contentDescription = if (completedExpanded) "Collapse" else "Expand",
                            tint = ext.textSecondary,
                            size = 20.dp
                        )
                    }
                }

                items(completedTasks, key = { "done_${it.id}" }) { task ->
                    AnimatedVisibility(
                        visible = completedExpanded,
                        enter = fadeIn(KronozaMotion.fastTween(reduced)) + expandVertically(),
                        exit = fadeOut(KronozaMotion.fastTween(reduced)) + shrinkVertically()
                    ) {
                        TaskRowCard(
                            task = task,
                            today = today,
                            preferences = preferences,
                            isOverdue = false,
                            onToggle = { onToggleCompleted(task) },
                            onClick = { onEditTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val ext = LocalKronozaColors.current
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) ext.lavenderContainer else ext.mutedSurface,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) ext.onLavenderContainer else ext.textSecondary,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun TaskRowCard(
    task: TaskEntity,
    today: KronozaDate,
    preferences: UserPreferences,
    isOverdue: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val ext = LocalKronozaColors.current
    val priorityBg = when (task.priority) {
        TaskPriority.HIGH -> ext.coralContainer
        TaskPriority.MEDIUM -> ext.lavenderContainer
        TaskPriority.LOW -> ext.mintContainer
    }
    val priorityFg = when (task.priority) {
        TaskPriority.HIGH -> ext.onCoralContainer
        TaskPriority.MEDIUM -> ext.onLavenderContainer
        TaskPriority.LOW -> ext.onMintContainer
    }
    val dueDate = remember(task.dueEpochDay) { KronozaDate.fromEpochDay(task.dueEpochDay) }
    val dateTimeText = remember(task.dueEpochDay, task.dueMinutes, today, preferences.dateFormat, preferences.timeFormat24h) {
        val timePart = KronozaDate.formatTime(task.dueMinutes, preferences.timeFormat24h)
        when (task.dueEpochDay) {
            today.epochDay -> timePart
            today.plusDays(1).epochDay -> "Tomorrow, $timePart"
            else -> "${dueDate.formatWithPreference(preferences.dateFormat)}, $timePart"
        }
    }

    KronozaCard(
        borderColor = if (isOverdue) ext.coralAccent.copy(alpha = 0.45f) else ext.cardBorder,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KronozaAnimatedCheckbox(
                checked = task.completed,
                accentColor = ext.mintAccent,
                borderColor = if (isOverdue) ext.coralAccent else ext.cardBorder,
                onToggle = onToggle,
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (task.completed) ext.textSecondary else ext.textPrimary,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KronozaIcon(
                            imageVector = if (task.dueEpochDay == today.epochDay) KronozaIcons.Clock else KronozaIcons.CalendarOutline,
                            contentDescription = null,
                            tint = if (isOverdue) ext.coralAccent else ext.textSecondary,
                            size = 13.dp
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = dateTimeText,
                            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                            color = if (isOverdue) ext.coralAccent else ext.textSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ext.mutedSurface
                    ) {
                        Text(
                            text = task.category.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    if (task.subtasks.isNotEmpty()) {
                        val doneSubs = task.subtasks.count { it.done }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            KronozaIcon(
                                imageVector = KronozaIcons.Subtask,
                                contentDescription = null,
                                tint = ext.textSecondary,
                                size = 12.dp
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = "$doneSubs/${task.subtasks.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                color = ext.textSecondary
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = priorityBg
                ) {
                    Text(
                        text = task.priority.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = priorityFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                if (isOverdue) {
                    Text(
                        text = "Overdue",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ext.coralAccent
                    )
                }
            }
        }
    }
}
