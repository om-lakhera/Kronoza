package com.omlakhera.kronoza.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omlakhera.kronoza.data.KronozaRepository
import com.omlakhera.kronoza.data.backup.BackupResult
import com.omlakhera.kronoza.data.local.EventEntity
import com.omlakhera.kronoza.data.local.NotificationEntity
import com.omlakhera.kronoza.data.local.ReminderConfigEntity
import com.omlakhera.kronoza.data.local.TaskEntity
import com.omlakhera.kronoza.model.AccentPreset
import com.omlakhera.kronoza.model.BackupRestoreMode
import com.omlakhera.kronoza.model.CalendarViewMode
import com.omlakhera.kronoza.model.CategoryShare
import com.omlakhera.kronoza.model.DailyHoursPoint
import com.omlakhera.kronoza.model.DateFormatOption
import com.omlakhera.kronoza.model.EventCategory
import com.omlakhera.kronoza.model.InsightsPeriod
import com.omlakhera.kronoza.model.InsightsSummary
import com.omlakhera.kronoza.model.RecurrenceEditScope
import com.omlakhera.kronoza.model.ReminderOption
import com.omlakhera.kronoza.model.StorageStats
import com.omlakhera.kronoza.model.ThemeMode
import com.omlakhera.kronoza.model.UserPreferences
import com.omlakhera.kronoza.model.WeeklyTrendPoint
import com.omlakhera.kronoza.util.CalendarEngine
import com.omlakhera.kronoza.util.KronozaDate
import com.omlakhera.kronoza.util.TaskFilterSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class KronozaViewModel(
    private val repository: KronozaRepository
) : ViewModel() {

    val events: StateFlow<List<EventEntity>> = repository.eventsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.tasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.notificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderConfigEntity>> = repository.remindersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val preferences: StateFlow<UserPreferences> = repository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    private val _prefsLoaded = MutableStateFlow(false)
    val prefsLoaded: StateFlow<Boolean> = _prefsLoaded.asStateFlow()

    private val _storageStats = MutableStateFlow(StorageStats())
    val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

    val unreadCount: StateFlow<Int> = notifications
        .map { list -> list.count { it.unread } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val initialToday = KronozaDate.today()

    private val _selectedDate = MutableStateFlow(initialToday)
    val selectedDate: StateFlow<KronozaDate> = _selectedDate.asStateFlow()

    private val _displayedMonth = MutableStateFlow(initialToday.year to initialToday.month)
    val displayedMonth: StateFlow<Pair<Int, Int>> = _displayedMonth.asStateFlow()

    private val _calendarViewMode = MutableStateFlow(CalendarViewMode.MONTH)
    val calendarViewMode: StateFlow<CalendarViewMode> = _calendarViewMode.asStateFlow()

    private val _insightsPeriod = MutableStateFlow(InsightsPeriod.WEEK)
    val insightsPeriod: StateFlow<InsightsPeriod> = _insightsPeriod.asStateFlow()

    val insightsSummary: StateFlow<InsightsSummary> = combine(
        events,
        tasks,
        _insightsPeriod,
        preferences
    ) { eventList, taskList, period, prefs ->
        computeGenuineInsights(eventList, taskList, period, prefs.dailyFocusGoal, prefs.startWeekMonday)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        computeGenuineInsights(emptyList(), emptyList(), InsightsPeriod.WEEK, 4, true)
    )

    init {
        viewModelScope.launch {
            val initialPrefs = repository.preferencesFlow.first()
            _calendarViewMode.value = initialPrefs.defaultCalendarView
            _prefsLoaded.value = true
            refreshStorageStats()
            repository.rescheduleAllReminders()
        }
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            _storageStats.value = repository.getStorageStats()
        }
    }

    fun observeEvent(eventId: Long): Flow<EventEntity?> = repository.observeEvent(eventId)

    suspend fun getEvent(eventId: Long): EventEntity? = repository.getEvent(eventId)

    suspend fun getTask(taskId: Long): TaskEntity? = repository.getTask(taskId)

    fun selectDate(date: KronozaDate) {
        _selectedDate.value = date
        _displayedMonth.value = date.year to date.month
    }

    fun jumpToToday() {
        val t = KronozaDate.today()
        _selectedDate.value = t
        _displayedMonth.value = t.year to t.month
    }

    fun shiftMonth(delta: Int) {
        val (y, m) = _displayedMonth.value
        val shifted = KronozaDate(y, m, 1).plusMonths(delta)
        _displayedMonth.value = shifted.year to shifted.month
    }

    fun setCalendarViewMode(mode: CalendarViewMode) {
        _calendarViewMode.value = mode
    }

    fun setInsightsPeriod(period: InsightsPeriod) {
        _insightsPeriod.value = period
    }

    fun saveEvent(event: EventEntity, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val savedId = repository.saveEvent(event)
            refreshStorageStats()
            onDone(savedId)
        }
    }

    fun saveEventWithScope(
        originalEvent: EventEntity,
        occurrenceEpochDay: Long,
        editedEvent: EventEntity,
        scope: RecurrenceEditScope,
        onDone: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val savedId = repository.saveEventWithScope(
                originalEvent = originalEvent,
                occurrenceEpochDay = occurrenceEpochDay,
                editedEvent = editedEvent,
                scope = scope
            )
            refreshStorageStats()
            onDone(savedId)
        }
    }

    fun duplicateEvent(eventId: Long, onDone: (Long?) -> Unit = {}) {
        viewModelScope.launch {
            val newId = repository.duplicateEvent(eventId)
            refreshStorageStats()
            onDone(newId)
        }
    }

    fun rescheduleEvent(
        eventId: Long,
        newDateEpochDay: Long,
        newStartMinutes: Int,
        newEndMinutes: Int,
        onDone: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val ok = repository.rescheduleEvent(eventId, newDateEpochDay, newStartMinutes, newEndMinutes)
            refreshStorageStats()
            onDone(ok)
        }
    }

    fun deleteEvent(eventId: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
            refreshStorageStats()
            onDone()
        }
    }

    fun deleteEventWithScope(
        event: EventEntity,
        occurrenceEpochDay: Long,
        scope: RecurrenceEditScope,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.deleteEventWithScope(event, occurrenceEpochDay, scope)
            refreshStorageStats()
            onDone()
        }
    }

    fun toggleEventChecklistItem(eventId: Long, itemId: String) {
        viewModelScope.launch {
            repository.toggleEventChecklistItem(eventId, itemId)
        }
    }

    fun saveTask(task: TaskEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveTask(task)
            refreshStorageStats()
            onDone()
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
        }
    }

    fun deleteTask(taskId: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            refreshStorageStats()
            onDone()
        }
    }

    fun markNotificationRead(notification: NotificationEntity) {
        viewModelScope.launch { repository.markNotificationRead(notification) }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch { repository.markAllNotificationsRead() }
    }

    fun respondToInvite(notification: NotificationEntity, accept: Boolean) {
        viewModelScope.launch {
            repository.respondToInvite(notification, accept)
            refreshStorageStats()
        }
    }

    fun setThemeMode(mode: ThemeMode) { viewModelScope.launch { repository.setThemeMode(mode) } }
    fun setAccentPreset(preset: AccentPreset) { viewModelScope.launch { repository.setAccentPreset(preset) } }
    fun setDefaultCalendarView(mode: CalendarViewMode) {
        viewModelScope.launch {
            repository.setDefaultCalendarView(mode)
            _calendarViewMode.value = mode
        }
    }
    fun setStartWeekMonday(startMonday: Boolean) { viewModelScope.launch { repository.setStartWeekMonday(startMonday) } }
    fun setDateFormat(format: DateFormatOption) { viewModelScope.launch { repository.setDateFormat(format) } }
    fun setTimeFormat24h(use24h: Boolean) { viewModelScope.launch { repository.setTimeFormat24h(use24h) } }
    fun setDefaultEventDurationMinutes(minutes: Int) { viewModelScope.launch { repository.setDefaultEventDurationMinutes(minutes) } }
    fun setDefaultReminder(option: ReminderOption) { viewModelScope.launch { repository.setDefaultReminder(option) } }
    fun setEventRemindersEnabled(enabled: Boolean) { viewModelScope.launch { repository.setEventRemindersEnabled(enabled) } }
    fun setTaskRemindersEnabled(enabled: Boolean) { viewModelScope.launch { repository.setTaskRemindersEnabled(enabled) } }
    fun setNotificationSounds(enabled: Boolean) { viewModelScope.launch { repository.setNotificationSounds(enabled) } }
    fun setDailyFocusGoal(goal: Int) { viewModelScope.launch { repository.setDailyFocusGoal(goal) } }
    fun setPreferredCategories(categories: Set<EventCategory>) { viewModelScope.launch { repository.setPreferredCategories(categories) } }
    fun updateProfile(name: String, email: String) { viewModelScope.launch { repository.updateProfile(name, email) } }

    fun saveProfilePhoto(uri: Uri, onDone: (String) -> Unit = {}) {
        viewModelScope.launch {
            val path = repository.saveProfilePhoto(uri)
            onDone(path)
        }
    }

    fun clearProfilePhoto() {
        viewModelScope.launch { repository.clearProfilePhoto() }
    }

    fun completeOnboarding() { viewModelScope.launch { repository.setOnboardingCompleted(true) } }
    fun replayOnboarding() { viewModelScope.launch { repository.setOnboardingCompleted(false) } }
    fun canScheduleExactAlarms(): Boolean = repository.canScheduleExactAlarms()
    fun rescheduleReminders() { viewModelScope.launch { repository.rescheduleAllReminders() } }

    fun clearAllLocalData(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearAllLocalData()
            refreshStorageStats()
            onDone()
        }
    }

    fun exportBackup(uri: Uri, onResult: (BackupResult) -> Unit) {
        viewModelScope.launch {
            onResult(repository.exportBackup(uri))
        }
    }

    fun restoreBackup(uri: Uri, mode: BackupRestoreMode, onResult: (BackupResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.restoreBackup(uri, mode)
            if (result is BackupResult.RestoreSuccess) {
                val restoredPrefs = repository.preferencesFlow.first()
                _calendarViewMode.value = restoredPrefs.defaultCalendarView
                refreshStorageStats()
            }
            onResult(result)
        }
    }

    /**
     * Computes 100% genuine productivity statistics exclusively from actual Room events and tasks.
     * Zero fabricated baselines or static mock offsets.
     */
    private fun computeGenuineInsights(
        events: List<EventEntity>,
        tasks: List<TaskEntity>,
        period: InsightsPeriod,
        dailyGoal: Int,
        startWeekMonday: Boolean
    ): InsightsSummary {
        val current = KronozaDate.today()
        val nowMinutes = KronozaDate.currentTimeMinutes()
        val weekStart = current.startOfWeek(startWeekMonday)

        val periodStartDay = if (period == InsightsPeriod.WEEK) {
            weekStart.epochDay
        } else {
            KronozaDate(current.year, current.month, 1).epochDay
        }
        val periodEndDay = if (period == InsightsPeriod.WEEK) {
            periodStartDay + 6
        } else {
            periodStartDay + KronozaDate.daysInMonth(current.year, current.month) - 1
        }

        val previousLength = (periodEndDay - periodStartDay + 1).coerceAtLeast(1)
        val prevStartDay = periodStartDay - previousLength
        val prevEndDay = periodStartDay - 1

        val periodOccurrences = CalendarEngine.expandEventsInRange(events, periodStartDay, periodEndDay)
        val prevOccurrences = CalendarEngine.expandEventsInRange(events, prevStartDay, prevEndDay)

        fun durationHoursFor(category: EventCategory, list: List<com.omlakhera.kronoza.util.EventOccurrence>): Double {
            val mins = list.filter { it.event.category == category }.sumOf { occ ->
                if (occ.event.allDay) 120 else (occ.spanEndMinutes - occ.spanStartMinutes).coerceAtLeast(15)
            }
            return (mins / 60.0).let { (it * 10.0).roundToInt() / 10.0 }
        }

        val focusHours = durationHoursFor(EventCategory.DEEP_WORK, periodOccurrences)
        val prevFocusHours = durationHoursFor(EventCategory.DEEP_WORK, prevOccurrences)
        val meetHours = durationHoursFor(EventCategory.WORK, periodOccurrences)
        val personalHours = durationHoursFor(EventCategory.PERSONAL, periodOccurrences)
        val deadlineHours = durationHoursFor(EventCategory.DEADLINE, periodOccurrences)

        val focusDeltaPercent = when {
            prevFocusHours <= 0.0 && focusHours > 0.0 -> 100
            prevFocusHours <= 0.0 -> 0
            else -> (((focusHours - prevFocusHours) / prevFocusHours) * 100.0).roundToInt()
        }

        val periodTasks = tasks.filter { it.dueEpochDay in periodStartDay..periodEndDay }
        val relevantTasks = if (periodTasks.isNotEmpty()) periodTasks else tasks
        val completedTasksCount = relevantTasks.count { it.completed }
        val pendingTasksCount = relevantTasks.count { !it.completed }
        val overdueTasksCount = relevantTasks.count { TaskFilterSort.isOverdue(it, current.epochDay, nowMinutes) }
        val completionRate = if (relevantTasks.isEmpty()) {
            0
        } else {
            ((completedTasksCount.toDouble() / relevantTasks.size) * 100.0).roundToInt().coerceIn(0, 100)
        }

        val totalHours = focusHours + meetHours + personalHours + deadlineHours
        val hasData = events.isNotEmpty() || tasks.isNotEmpty()

        // Daily breakdown across the current 7-day week
        val rawWeekDays = List(7) { offset -> weekStart.plusDays(offset) }
        val weekOccurrences = CalendarEngine.expandEventsInRange(
            events,
            weekStart.epochDay,
            weekStart.plusDays(6).epochDay
        )
        val rawPoints = rawWeekDays.map { date ->
            val dayOccs = weekOccurrences.filter { it.occurrenceEpochDay == date.epochDay }
            val fHours = dayOccs.filter { it.event.category == EventCategory.DEEP_WORK }.sumOf {
                if (it.event.allDay) 120 else (it.spanEndMinutes - it.spanStartMinutes).coerceAtLeast(15)
            } / 60f
            val mHours = dayOccs.filter { it.event.category == EventCategory.WORK }.sumOf {
                if (it.event.allDay) 60 else (it.spanEndMinutes - it.spanStartMinutes).coerceAtLeast(15)
            } / 60f
            Triple(date.shortWeekdayName(), fHours, mHours)
        }
        val maxFocus = rawPoints.maxOfOrNull { it.second } ?: 0f
        val dailyBars = rawPoints.map { (label, f, m) ->
            DailyHoursPoint(
                dayLabel = label,
                focusHours = f,
                meetingHours = m,
                isPeak = maxFocus > 0f && f >= maxFocus - 0.01f
            )
        }

        val shares = if (totalHours <= 0.0) {
            emptyList()
        } else {
            listOf(
                CategoryShare(
                    label = "Deep Work",
                    hours = focusHours,
                    percent = ((focusHours / totalHours) * 100.0).roundToInt(),
                    category = EventCategory.DEEP_WORK
                ),
                CategoryShare(
                    label = "Meetings",
                    hours = meetHours,
                    percent = ((meetHours / totalHours) * 100.0).roundToInt(),
                    category = EventCategory.WORK
                ),
                CategoryShare(
                    label = "Personal",
                    hours = personalHours,
                    percent = ((personalHours / totalHours) * 100.0).roundToInt(),
                    category = EventCategory.PERSONAL
                ),
                CategoryShare(
                    label = "Milestones",
                    hours = deadlineHours,
                    percent = ((deadlineHours / totalHours) * 100.0).roundToInt(),
                    category = EventCategory.DEADLINE
                )
            ).filter { it.hours > 0.0 }.sortedByDescending { it.hours }
        }

        // Real streak computation across last 30 days based on days with completed tasks or deep-work events
        val activeDays = HashSet<Long>()
        tasks.filter { it.completed }.forEach { activeDays.add(it.dueEpochDay) }
        CalendarEngine.expandEventsInRange(events, current.epochDay - 30, current.epochDay)
            .filter { it.event.category == EventCategory.DEEP_WORK || it.event.category == EventCategory.WORK }
            .forEach { activeDays.add(it.occurrenceEpochDay) }

        var currentStreak = 0
        var cursor = current.epochDay
        if (cursor !in activeDays && (cursor - 1) in activeDays) {
            cursor -= 1
        }
        while (cursor in activeDays && currentStreak < 60) {
            currentStreak++
            cursor--
        }

        var bestStreak = currentStreak
        var run = 0
        for (day in (current.epochDay - 30)..current.epochDay) {
            if (day in activeDays) {
                run++
                if (run > bestStreak) bestStreak = run
            } else {
                run = 0
            }
        }

        // Last 4 weeks genuine trend
        val weeklyTrend = (3 downTo 0).map { weeksAgo ->
            val wStart = weekStart.plusDays(-weeksAgo * 7)
            val wEnd = wStart.plusDays(6)
            val wOccs = CalendarEngine.expandEventsInRange(events, wStart.epochDay, wEnd.epochDay)
            val wHours = (wOccs.sumOf {
                if (it.event.allDay) 60 else (it.spanEndMinutes - it.spanStartMinutes).coerceAtLeast(15)
            } / 60f)
            val wDoneTasks = tasks.count { it.completed && it.dueEpochDay in wStart.epochDay..wEnd.epochDay }
            WeeklyTrendPoint(
                weekLabel = if (weeksAgo == 0) "This wk" else "${weeksAgo}w ago",
                scheduledHours = (wHours * 10f).roundToInt() / 10f,
                completedTasks = wDoneTasks
            )
        }

        val peakDay = dailyBars.firstOrNull { it.isPeak }?.dayLabel
        val highlightTitle = when {
            !hasData -> "Ready when you are"
            overdueTasksCount > 0 -> "$overdueTasksCount overdue ${if (overdueTasksCount == 1) "task" else "tasks"}"
            peakDay != null -> "$peakDay led your week"
            else -> "Balanced schedule"
        }
        val highlightBody = when {
            !hasData -> "Add events or tasks to unlock real-time insights, category balance, and streak tracking."
            overdueTasksCount > 0 -> "Rescheduling or completing overdue tasks will keep your ${dailyGoal}-block daily goal on track."
            peakDay != null -> "You scheduled ${focusHours}h of deep work and completed $completedTasksCount of ${relevantTasks.size} tasks ($completionRate%)."
            else -> "${periodOccurrences.size} scheduled events and $pendingTasksCount pending tasks tracked in this period."
        }

        return InsightsSummary(
            focusHours = focusHours,
            focusDeltaPercent = focusDeltaPercent,
            tasksDone = completedTasksCount,
            pendingTasks = pendingTasksCount,
            overdueTasks = overdueTasksCount,
            completionRatePercent = completionRate,
            eventsCount = periodOccurrences.size,
            meetingHours = meetHours,
            streakDays = currentStreak,
            bestStreakDays = bestStreak,
            dailyBars = dailyBars,
            categoryBreakdown = shares,
            weeklyTrendPoints = weeklyTrend,
            hasActivityData = hasData,
            highlightTitle = highlightTitle,
            highlightBody = highlightBody
        )
    }

    class Factory(private val repository: KronozaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return KronozaViewModel(repository) as T
        }
    }
}
