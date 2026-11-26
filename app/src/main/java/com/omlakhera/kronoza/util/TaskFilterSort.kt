package com.omlakhera.kronoza.util

import com.omlakhera.kronoza.data.local.EventEntity
import com.omlakhera.kronoza.data.local.TaskEntity
import com.omlakhera.kronoza.model.DateFilterOption
import com.omlakhera.kronoza.model.EventCategory
import com.omlakhera.kronoza.model.PriorityFilterOption
import com.omlakhera.kronoza.model.RepeatFrequency
import com.omlakhera.kronoza.model.SearchCompletionFilter
import com.omlakhera.kronoza.model.SearchEventTypeFilter
import com.omlakhera.kronoza.model.SearchFilter
import com.omlakhera.kronoza.model.SearchSortOption
import com.omlakhera.kronoza.model.TaskCategory
import com.omlakhera.kronoza.model.TaskFilter
import com.omlakhera.kronoza.model.TaskPriority
import com.omlakhera.kronoza.model.TaskSortOption

/**
 * Pure domain helpers for task overdue detection, filtering, sorting, and global search matching.
 */
object TaskFilterSort {

    fun isOverdue(
        task: TaskEntity,
        todayEpochDay: Long,
        nowMinutes: Int
    ): Boolean {
        if (task.completed) return false
        return task.dueEpochDay < todayEpochDay ||
            (task.dueEpochDay == todayEpochDay && task.dueMinutes < nowMinutes)
    }

    fun filterTasks(
        tasks: List<TaskEntity>,
        filter: TaskFilter,
        categoryFilter: TaskCategory?,
        todayEpochDay: Long,
        nowMinutes: Int
    ): List<TaskEntity> {
        return tasks.filter { task ->
            val primaryMatch = when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.TODAY -> task.dueEpochDay == todayEpochDay
                TaskFilter.OVERDUE -> isOverdue(task, todayEpochDay, nowMinutes)
                TaskFilter.HIGH_PRIORITY -> task.priority == TaskPriority.HIGH && !task.completed
                TaskFilter.COMPLETED -> task.completed
            }
            val categoryMatch = categoryFilter == null || task.category == categoryFilter
            primaryMatch && categoryMatch
        }
    }

    fun sortTasks(
        tasks: List<TaskEntity>,
        sortOption: TaskSortOption
    ): List<TaskEntity> {
        val comparator: Comparator<TaskEntity> = when (sortOption) {
            TaskSortOption.DUE_DATE -> compareBy<TaskEntity>(
                { it.dueEpochDay },
                { it.dueMinutes },
                { it.priority.sortWeight }
            )
            TaskSortOption.PRIORITY -> compareBy<TaskEntity>(
                { it.priority.sortWeight },
                { it.dueEpochDay },
                { it.dueMinutes }
            )
            TaskSortOption.TITLE -> compareBy<TaskEntity> { it.title.lowercase() }
            TaskSortOption.CREATED -> compareByDescending<TaskEntity> { it.updatedAtMillis }
        }
        return tasks.sortedWith(comparator)
    }

    fun filterSearchEvents(
        events: List<EventEntity>,
        query: String,
        scopeFilter: SearchFilter,
        dateFilter: DateFilterOption,
        eventTypeFilter: SearchEventTypeFilter = SearchEventTypeFilter.ALL,
        completionFilter: SearchCompletionFilter = SearchCompletionFilter.ALL,
        sortOption: SearchSortOption = SearchSortOption.DATE,
        todayEpochDay: Long,
        weekStartEpochDay: Long,
        weekEndEpochDay: Long
    ): List<EventEntity> {
        if (scopeFilter == SearchFilter.TASKS) return emptyList()
        if (completionFilter == SearchCompletionFilter.COMPLETED) return emptyList()
        val filtered = events.filter { event ->
            val catMatch = when (scopeFilter) {
                SearchFilter.WORK -> event.category == EventCategory.WORK || event.category == EventCategory.DEEP_WORK
                SearchFilter.PERSONAL -> event.category == EventCategory.PERSONAL
                else -> true
            }
            val endDay = event.effectiveEndDateEpochDay
            val dateMatch = when (dateFilter) {
                DateFilterOption.ALL -> true
                DateFilterOption.TODAY -> todayEpochDay in event.dateEpochDay..endDay
                DateFilterOption.THIS_WEEK -> endDay >= weekStartEpochDay && event.dateEpochDay <= weekEndEpochDay
                DateFilterOption.UPCOMING -> endDay >= todayEpochDay
                DateFilterOption.OVERDUE -> endDay < todayEpochDay
            }
            val typeMatch = when (eventTypeFilter) {
                SearchEventTypeFilter.ALL -> true
                SearchEventTypeFilter.TIMED -> !event.allDay
                SearchEventTypeFilter.ALL_DAY -> event.allDay
                SearchEventTypeFilter.RECURRING -> event.repeatFrequency != RepeatFrequency.NONE
                SearchEventTypeFilter.VIDEO_CALL -> event.meetingUrl.isNotBlank() ||
                    event.location.contains("meet", ignoreCase = true) ||
                    event.location.contains("zoom", ignoreCase = true)
            }
            val textMatch = query.isBlank() ||
                event.title.contains(query, ignoreCase = true) ||
                event.location.contains(query, ignoreCase = true) ||
                event.notes.contains(query, ignoreCase = true) ||
                event.category.displayName.contains(query, ignoreCase = true) ||
                event.badgeLabel.contains(query, ignoreCase = true)
            catMatch && dateMatch && typeMatch && textMatch
        }

        val comparator: Comparator<EventEntity> = when (sortOption) {
            SearchSortOption.DATE -> compareBy({ it.dateEpochDay }, { it.startMinutes })
            SearchSortOption.RECENTLY_MODIFIED -> compareByDescending { it.updatedAtMillis }
            SearchSortOption.PRIORITY -> compareBy<EventEntity>({ !it.isMilestone }, { it.dateEpochDay }, { it.startMinutes })
            SearchSortOption.ALPHABETICAL -> compareBy { it.title.lowercase() }
        }
        return filtered.sortedWith(comparator)
    }

    fun filterSearchTasks(
        tasks: List<TaskEntity>,
        query: String,
        scopeFilter: SearchFilter,
        dateFilter: DateFilterOption,
        priorityFilter: PriorityFilterOption,
        completionFilter: SearchCompletionFilter = SearchCompletionFilter.ALL,
        sortOption: SearchSortOption = SearchSortOption.DATE,
        todayEpochDay: Long,
        nowMinutes: Int,
        weekStartEpochDay: Long,
        weekEndEpochDay: Long
    ): List<TaskEntity> {
        if (scopeFilter == SearchFilter.EVENTS) return emptyList()
        val filtered = tasks.filter { task ->
            val catMatch = when (scopeFilter) {
                SearchFilter.WORK -> task.category != TaskCategory.PERSONAL
                SearchFilter.PERSONAL -> task.category == TaskCategory.PERSONAL
                else -> true
            }
            val dateMatch = when (dateFilter) {
                DateFilterOption.ALL -> true
                DateFilterOption.TODAY -> task.dueEpochDay == todayEpochDay
                DateFilterOption.THIS_WEEK -> task.dueEpochDay in weekStartEpochDay..weekEndEpochDay
                DateFilterOption.UPCOMING -> task.dueEpochDay > todayEpochDay
                DateFilterOption.OVERDUE -> isOverdue(task, todayEpochDay, nowMinutes)
            }
            val priorityMatch = when (priorityFilter) {
                PriorityFilterOption.ALL -> true
                PriorityFilterOption.HIGH -> task.priority == TaskPriority.HIGH
                PriorityFilterOption.MEDIUM -> task.priority == TaskPriority.MEDIUM
                PriorityFilterOption.LOW -> task.priority == TaskPriority.LOW
            }
            val completionMatch = when (completionFilter) {
                SearchCompletionFilter.ALL -> true
                SearchCompletionFilter.ACTIVE -> !task.completed
                SearchCompletionFilter.COMPLETED -> task.completed
            }
            val textMatch = query.isBlank() ||
                task.title.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true) ||
                task.category.label.contains(query, ignoreCase = true)
            catMatch && dateMatch && priorityMatch && completionMatch && textMatch
        }

        val comparator: Comparator<TaskEntity> = when (sortOption) {
            SearchSortOption.DATE -> compareBy({ it.completed }, { it.dueEpochDay }, { it.dueMinutes })
            SearchSortOption.RECENTLY_MODIFIED -> compareByDescending { it.updatedAtMillis }
            SearchSortOption.PRIORITY -> compareBy({ it.priority.sortWeight }, { it.dueEpochDay }, { it.dueMinutes })
            SearchSortOption.ALPHABETICAL -> compareBy { it.title.lowercase() }
        }
        return filtered.sortedWith(comparator)
    }
}
