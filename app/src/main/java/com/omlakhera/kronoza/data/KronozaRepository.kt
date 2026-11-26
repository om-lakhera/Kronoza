package com.omlakhera.kronoza.data

import android.content.Context
import android.net.Uri
import com.omlakhera.kronoza.data.backup.BackupResult
import com.omlakhera.kronoza.data.backup.KronozaBackupManager
import com.omlakhera.kronoza.data.local.EventEntity
import com.omlakhera.kronoza.data.local.KronozaDao
import com.omlakhera.kronoza.data.local.KronozaDatabase
import com.omlakhera.kronoza.data.local.NotificationEntity
import com.omlakhera.kronoza.data.local.PreferencesRepository
import com.omlakhera.kronoza.data.local.ReminderConfigEntity
import com.omlakhera.kronoza.data.local.TaskEntity
import com.omlakhera.kronoza.model.AccentPreset
import com.omlakhera.kronoza.model.AgendaCheckItem
import com.omlakhera.kronoza.model.AttendeeItem
import com.omlakhera.kronoza.model.BackupRestoreMode
import com.omlakhera.kronoza.model.CalendarViewMode
import com.omlakhera.kronoza.model.DateFormatOption
import com.omlakhera.kronoza.model.EventCategory
import com.omlakhera.kronoza.model.NotificationType
import com.omlakhera.kronoza.model.RecurrenceEditScope
import com.omlakhera.kronoza.model.ReminderOption
import com.omlakhera.kronoza.model.ReminderTargetType
import com.omlakhera.kronoza.model.RepeatFrequency
import com.omlakhera.kronoza.model.StorageStats
import com.omlakhera.kronoza.model.SubtaskItem
import com.omlakhera.kronoza.model.TaskCategory
import com.omlakhera.kronoza.model.TaskPriority
import com.omlakhera.kronoza.model.ThemeMode
import com.omlakhera.kronoza.model.UserPreferences
import com.omlakhera.kronoza.reminders.KronozaReminderScheduler
import com.omlakhera.kronoza.util.KronozaDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.TimeZone
import java.util.UUID

class KronozaRepository(
    private val dao: KronozaDao,
    private val prefs: PreferencesRepository,
    private val reminderScheduler: KronozaReminderScheduler? = null,
    private val backupManager: KronozaBackupManager? = null,
    private val appContext: Context? = null
) {
    constructor(context: Context) : this(
        dao = KronozaDatabase.getInstance(context).kronozaDao(),
        prefs = PreferencesRepository(context),
        reminderScheduler = KronozaReminderScheduler(context.applicationContext),
        backupManager = KronozaBackupManager(context.applicationContext),
        appContext = context.applicationContext
    )

    val eventsFlow: Flow<List<EventEntity>> = dao.observeAllEvents()
    val tasksFlow: Flow<List<TaskEntity>> = dao.observeAllTasks()
    val notificationsFlow: Flow<List<NotificationEntity>> = dao.observeAllNotifications()
    val remindersFlow: Flow<List<ReminderConfigEntity>> = dao.observeAllReminders()
    val preferencesFlow: Flow<UserPreferences> = prefs.preferencesFlow

    fun observeEvent(eventId: Long): Flow<EventEntity?> = dao.observeEventById(eventId)

    suspend fun getEvent(eventId: Long): EventEntity? = dao.getEventById(eventId)

    suspend fun saveEvent(event: EventEntity): Long {
        val now = System.currentTimeMillis()
        val seriesId = if (event.repeatFrequency != RepeatFrequency.NONE && event.seriesId.isBlank()) {
            UUID.randomUUID().toString()
        } else {
            event.seriesId
        }
        val normalized = event.copy(
            seriesId = seriesId,
            endDateEpochDay = if (event.endDateEpochDay > event.dateEpochDay) event.endDateEpochDay else 0L,
            updatedAtMillis = now
        )
        val id = if (normalized.id == 0L) {
            dao.insertEvent(normalized)
        } else {
            dao.updateEvent(normalized)
            normalized.id
        }
        syncEventReminder(normalized.copy(id = id))
        return id
    }

    suspend fun saveEventWithScope(
        originalEvent: EventEntity,
        occurrenceEpochDay: Long,
        editedEvent: EventEntity,
        scope: RecurrenceEditScope
    ): Long {
        return if (scope == RecurrenceEditScope.THIS_OCCURRENCE &&
            originalEvent.repeatFrequency != RepeatFrequency.NONE
        ) {
            val updatedExceptions = (originalEvent.excludedEpochDays + occurrenceEpochDay).distinct()
            dao.updateEvent(
                originalEvent.copy(
                    excludedEpochDays = updatedExceptions,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            val detached = editedEvent.copy(
                id = 0L,
                dateEpochDay = occurrenceEpochDay,
                repeatFrequency = RepeatFrequency.NONE,
                seriesId = "",
                excludedEpochDays = emptyList(),
                createdAtMillis = System.currentTimeMillis(),
                updatedAtMillis = System.currentTimeMillis()
            )
            saveEvent(detached)
        } else {
            saveEvent(editedEvent.copy(id = originalEvent.id))
        }
    }

    suspend fun duplicateEvent(eventId: Long): Long? {
        val existing = dao.getEventById(eventId) ?: return null
        val now = System.currentTimeMillis()
        val copy = existing.copy(
            id = 0L,
            title = "${existing.title} (Copy)",
            seriesId = if (existing.repeatFrequency != RepeatFrequency.NONE) UUID.randomUUID().toString() else "",
            excludedEpochDays = emptyList(),
            createdAtMillis = now,
            updatedAtMillis = now
        )
        return saveEvent(copy)
    }

    suspend fun rescheduleEvent(
        eventId: Long,
        newDateEpochDay: Long,
        newStartMinutes: Int,
        newEndMinutes: Int
    ): Boolean {
        val existing = dao.getEventById(eventId) ?: return false
        val dayDelta = newDateEpochDay - existing.dateEpochDay
        val newEndDay = if (existing.isMultiDay) existing.effectiveEndDateEpochDay + dayDelta else 0L
        val updated = existing.copy(
            dateEpochDay = newDateEpochDay,
            endDateEpochDay = newEndDay,
            startMinutes = newStartMinutes,
            endMinutes = newEndMinutes,
            updatedAtMillis = System.currentTimeMillis()
        )
        saveEvent(updated)
        return true
    }

    suspend fun deleteEvent(eventId: Long) {
        dao.deleteEventById(eventId)
        dao.deleteReminderForTarget(ReminderTargetType.EVENT, eventId)
        reminderScheduler?.cancelReminder(ReminderTargetType.EVENT, eventId)
    }

    suspend fun deleteEventWithScope(
        event: EventEntity,
        occurrenceEpochDay: Long,
        scope: RecurrenceEditScope
    ) {
        if (scope == RecurrenceEditScope.THIS_OCCURRENCE &&
            event.repeatFrequency != RepeatFrequency.NONE
        ) {
            val updatedExceptions = (event.excludedEpochDays + occurrenceEpochDay).distinct()
            dao.updateEvent(
                event.copy(
                    excludedEpochDays = updatedExceptions,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            syncEventReminder(event.copy(excludedEpochDays = updatedExceptions))
        } else {
            if (event.seriesId.isNotBlank()) {
                val seriesEvents = dao.getEventsBySeriesId(event.seriesId)
                seriesEvents.forEach { member ->
                    dao.deleteReminderForTarget(ReminderTargetType.EVENT, member.id)
                    reminderScheduler?.cancelReminder(ReminderTargetType.EVENT, member.id)
                }
                dao.deleteEventsBySeriesId(event.seriesId)
            } else {
                deleteEvent(event.id)
            }
        }
    }

    suspend fun toggleEventChecklistItem(eventId: Long, itemId: String) {
        val current = dao.getEventById(eventId) ?: return
        val updated = current.checklist.map {
            if (it.id == itemId) it.copy(done = !it.done) else it
        }
        dao.updateEvent(current.copy(checklist = updated, updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun getTask(taskId: Long): TaskEntity? = dao.getTaskById(taskId)

    suspend fun saveTask(task: TaskEntity): Long {
        val normalized = task.copy(updatedAtMillis = System.currentTimeMillis())
        val id = if (normalized.id == 0L) {
            dao.insertTask(normalized)
        } else {
            dao.updateTask(normalized)
            normalized.id
        }
        syncTaskReminder(normalized.copy(id = id))
        return id
    }

    suspend fun toggleTaskCompleted(task: TaskEntity) {
        val updated = task.copy(
            completed = !task.completed,
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.updateTask(updated)
        syncTaskReminder(updated)
    }

    suspend fun deleteTask(taskId: Long) {
        dao.deleteTaskById(taskId)
        dao.deleteReminderForTarget(ReminderTargetType.TASK, taskId)
        reminderScheduler?.cancelReminder(ReminderTargetType.TASK, taskId)
    }

    suspend fun markNotificationRead(notification: NotificationEntity) {
        if (notification.unread) {
            dao.updateNotification(notification.copy(unread = false))
        }
    }

    suspend fun markAllNotificationsRead() = dao.markAllNotificationsRead()

    suspend fun respondToInvite(notification: NotificationEntity, accept: Boolean) {
        val status = if (accept) "ACCEPTED" else "DECLINED"
        dao.updateNotification(notification.copy(unread = false, inviteStatus = status))
        if (accept) {
            val today = KronozaDate.today()
            val eventEpoch = notification.inviteDateEpochDay ?: today.plusDays(1).epochDay
            val start = notification.inviteStartMinutes ?: (14 * 60)
            val end = notification.inviteEndMinutes ?: (15 * 60)
            val newEvent = EventEntity(
                title = notification.title.ifBlank { "Scheduled Invitation" },
                dateEpochDay = eventEpoch,
                startMinutes = start,
                endMinutes = end,
                category = EventCategory.WORK,
                reminder = ReminderOption.FIFTEEN_MINS,
                notes = notification.message,
                badgeLabel = "Invite"
            )
            saveEvent(newEvent)
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) = prefs.setThemeMode(mode)
    suspend fun setAccentPreset(preset: AccentPreset) = prefs.setAccentPreset(preset)
    suspend fun setDefaultCalendarView(mode: CalendarViewMode) = prefs.setDefaultCalendarView(mode)
    suspend fun setStartWeekMonday(startMonday: Boolean) = prefs.setStartWeekMonday(startMonday)
    suspend fun setDateFormat(format: DateFormatOption) = prefs.setDateFormat(format)
    suspend fun setTimeFormat24h(use24h: Boolean) = prefs.setTimeFormat24h(use24h)
    suspend fun setDefaultEventDurationMinutes(minutes: Int) = prefs.setDefaultEventDurationMinutes(minutes)
    suspend fun setDefaultReminder(option: ReminderOption) = prefs.setDefaultReminder(option)
    suspend fun setEventRemindersEnabled(enabled: Boolean) {
        prefs.setEventRemindersEnabled(enabled)
        dao.getAllEventsOnce().forEach { syncEventReminder(it) }
    }
    suspend fun setTaskRemindersEnabled(enabled: Boolean) {
        prefs.setTaskRemindersEnabled(enabled)
        dao.getAllTasksOnce().forEach { syncTaskReminder(it) }
    }
    suspend fun setNotificationSounds(enabled: Boolean) = prefs.setNotificationSounds(enabled)
    suspend fun setDailyFocusGoal(goal: Int) = prefs.setDailyFocusGoal(goal)
    suspend fun setPreferredCategories(categories: Set<EventCategory>) = prefs.setPreferredCategories(categories)
    suspend fun setOnboardingCompleted(completed: Boolean) = prefs.setOnboardingCompleted(completed)
    suspend fun updateProfile(name: String, email: String) = prefs.updateProfile(name, email)
    suspend fun saveProfilePhoto(uri: Uri): String = prefs.saveProfilePhotoFromUri(uri)
    suspend fun clearProfilePhoto() = prefs.clearProfilePhoto()

    suspend fun getStorageStats(): StorageStats = withContext(Dispatchers.IO) {
        val dbFileSize = appContext?.getDatabasePath(KronozaDatabase.DB_NAME)?.length() ?: 0L
        StorageStats(
            eventCount = dao.countEvents(),
            taskCount = dao.countTasks(),
            notificationCount = dao.countNotifications(),
            reminderCount = dao.countReminders(),
            databaseBytes = dbFileSize
        )
    }

    suspend fun clearAllLocalData() {
        val reminders = dao.getAllRemindersOnce()
        reminders.forEach {
            reminderScheduler?.cancelReminder(it.targetType, it.targetId)
        }
        dao.clearAllUserData()
    }

    suspend fun rescheduleAllReminders() {
        reminderScheduler?.rescheduleAll(TimeZone.getDefault())
    }

    fun canScheduleExactAlarms(): Boolean = reminderScheduler?.canScheduleExactAlarms() ?: false

    suspend fun exportBackup(uri: Uri): BackupResult {
        return backupManager?.exportToUri(uri) ?: BackupResult.Failure("Backup manager unavailable.")
    }

    suspend fun restoreBackup(uri: Uri, mode: BackupRestoreMode): BackupResult {
        return backupManager?.restoreFromUri(uri, mode) ?: BackupResult.Failure("Backup manager unavailable.")
    }

    private suspend fun syncEventReminder(event: EventEntity) {
        val userPrefs = prefs.getPreferencesOnce()
        if (event.reminder == ReminderOption.NONE || !userPrefs.eventRemindersEnabled) {
            dao.deleteReminderForTarget(ReminderTargetType.EVENT, event.id)
            reminderScheduler?.cancelReminder(ReminderTargetType.EVENT, event.id)
            return
        }
        val tz = TimeZone.getDefault()
        val minutes = if (event.allDay) 9 * 60 else event.startMinutes
        var triggerDay = event.dateEpochDay
        val todayEpoch = KronozaDate.today(tz).epochDay
        if (event.repeatFrequency != RepeatFrequency.NONE && triggerDay < todayEpoch) {
            triggerDay = com.omlakhera.kronoza.util.CalendarEngine.nextOccurrenceEpochDay(event, todayEpoch - 1) ?: triggerDay
        }
        val triggerAt = KronozaReminderScheduler.computeTriggerAtMillis(
            dueEpochDay = triggerDay,
            dueMinutes = minutes,
            minutesBefore = event.reminder.minutesBefore,
            timeZone = tz
        )
        val existing = dao.getReminderForTarget(ReminderTargetType.EVENT, event.id)
        val config = ReminderConfigEntity(
            id = existing?.id ?: 0L,
            targetType = ReminderTargetType.EVENT,
            targetId = event.id,
            title = event.title,
            dueEpochDay = triggerDay,
            dueMinutes = minutes,
            minutesBefore = event.reminder.minutesBefore,
            triggerAtMillis = triggerAt,
            timeZoneId = tz.id,
            enabled = true,
            fired = false,
            useExactAlarm = reminderScheduler?.canScheduleExactAlarms() ?: true
        )
        dao.insertReminder(config)
        reminderScheduler?.scheduleOrUpdate(config)
    }

    private suspend fun syncTaskReminder(task: TaskEntity) {
        val userPrefs = prefs.getPreferencesOnce()
        if (!task.reminderEnabled || task.completed || !userPrefs.taskRemindersEnabled) {
            dao.deleteReminderForTarget(ReminderTargetType.TASK, task.id)
            reminderScheduler?.cancelReminder(ReminderTargetType.TASK, task.id)
            return
        }
        val tz = TimeZone.getDefault()
        val triggerAt = KronozaReminderScheduler.computeTriggerAtMillis(
            dueEpochDay = task.dueEpochDay,
            dueMinutes = task.dueMinutes,
            minutesBefore = 15,
            timeZone = tz
        )
        val existing = dao.getReminderForTarget(ReminderTargetType.TASK, task.id)
        val config = ReminderConfigEntity(
            id = existing?.id ?: 0L,
            targetType = ReminderTargetType.TASK,
            targetId = task.id,
            title = task.title,
            dueEpochDay = task.dueEpochDay,
            dueMinutes = task.dueMinutes,
            minutesBefore = 15,
            triggerAtMillis = triggerAt,
            timeZoneId = tz.id,
            enabled = true,
            fired = false,
            useExactAlarm = reminderScheduler?.canScheduleExactAlarms() ?: true
        )
        dao.insertReminder(config)
        reminderScheduler?.scheduleOrUpdate(config)
    }
}
