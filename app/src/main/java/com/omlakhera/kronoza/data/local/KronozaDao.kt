package com.omlakhera.kronoza.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.omlakhera.kronoza.model.ReminderTargetType
import kotlinx.coroutines.flow.Flow

@Dao
interface KronozaDao {

    @Query("SELECT * FROM events ORDER BY dateEpochDay ASC, startMinutes ASC")
    fun observeAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY dateEpochDay ASC, startMinutes ASC")
    suspend fun getAllEventsOnce(): List<EventEntity>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    fun observeEventById(id: Long): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Long): EventEntity?

    @Query("SELECT * FROM events WHERE seriesId = :seriesId")
    suspend fun getEventsBySeriesId(seriesId: String): List<EventEntity>

    @Query("SELECT COUNT(*) FROM events")
    suspend fun countEvents(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("DELETE FROM events WHERE seriesId = :seriesId")
    suspend fun deleteEventsBySeriesId(seriesId: String)

    @Query("DELETE FROM events")
    suspend fun clearEvents()

    @Query("SELECT * FROM tasks ORDER BY completed ASC, dueEpochDay ASC, dueMinutes ASC")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY completed ASC, dueEpochDay ASC, dueMinutes ASC")
    suspend fun getAllTasksOnce(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countTasks(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("SELECT * FROM notifications ORDER BY isToday DESC, id DESC")
    fun observeAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY id ASC")
    suspend fun getAllNotificationsOnce(): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun countNotifications(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET unread = 0")
    suspend fun markAllNotificationsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()

    // Reminder configurations
    @Query("SELECT * FROM reminders ORDER BY triggerAtMillis ASC")
    fun observeAllReminders(): Flow<List<ReminderConfigEntity>>

    @Query("SELECT * FROM reminders ORDER BY triggerAtMillis ASC")
    suspend fun getAllRemindersOnce(): List<ReminderConfigEntity>

    @Query("SELECT * FROM reminders WHERE enabled = 1 AND fired = 0 ORDER BY triggerAtMillis ASC")
    suspend fun getActiveReminders(): List<ReminderConfigEntity>

    @Query("SELECT COUNT(*) FROM reminders")
    suspend fun countReminders(): Int

    @Query("SELECT * FROM reminders WHERE targetType = :type AND targetId = :targetId LIMIT 1")
    suspend fun getReminderForTarget(type: ReminderTargetType, targetId: Long): ReminderConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderConfigEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderConfigEntity>)

    @Update
    suspend fun updateReminder(reminder: ReminderConfigEntity)

    @Query("DELETE FROM reminders WHERE targetType = :type AND targetId = :targetId")
    suspend fun deleteReminderForTarget(type: ReminderTargetType, targetId: Long)

    @Query("UPDATE reminders SET fired = 1 WHERE targetType = :type AND targetId = :targetId")
    suspend fun markReminderFired(type: ReminderTargetType, targetId: Long)

    @Query("DELETE FROM reminders")
    suspend fun clearReminders()

    @Transaction
    suspend fun replaceAllData(
        events: List<EventEntity>,
        tasks: List<TaskEntity>,
        notifications: List<NotificationEntity>,
        reminders: List<ReminderConfigEntity>
    ) {
        clearReminders()
        clearNotifications()
        clearTasks()
        clearEvents()
        if (events.isNotEmpty()) insertEvents(events)
        if (tasks.isNotEmpty()) insertTasks(tasks)
        if (notifications.isNotEmpty()) insertNotifications(notifications)
        if (reminders.isNotEmpty()) insertReminders(reminders)
    }

    @Transaction
    suspend fun deleteEventAndReminder(eventId: Long) {
        deleteReminderForTarget(ReminderTargetType.EVENT, eventId)
        deleteEventById(eventId)
    }

    @Transaction
    suspend fun deleteTaskAndReminder(taskId: Long) {
        deleteReminderForTarget(ReminderTargetType.TASK, taskId)
        deleteTaskById(taskId)
    }

    @Transaction
    suspend fun clearAllUserData() {
        clearReminders()
        clearNotifications()
        clearTasks()
        clearEvents()
    }
}
