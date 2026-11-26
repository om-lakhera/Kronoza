package com.omlakhera.kronoza.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.omlakhera.kronoza.data.local.KronozaDao
import com.omlakhera.kronoza.data.local.KronozaDatabase
import com.omlakhera.kronoza.data.local.ReminderConfigEntity
import com.omlakhera.kronoza.model.ReminderTargetType
import com.omlakhera.kronoza.util.KronozaDate
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class KronozaReminderScheduler(
    private val context: Context,
    private val dao: KronozaDao = KronozaDatabase.getInstance(context).kronozaDao()
) {

    fun canScheduleExactAlarms(): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    suspend fun scheduleOrUpdate(reminder: ReminderConfigEntity) {
        if (!reminder.enabled || reminder.fired) {
            cancelReminder(reminder.targetType, reminder.targetId)
            return
        }

        val tz = runCatching { TimeZone.getTimeZone(reminder.timeZoneId) }
            .getOrDefault(TimeZone.getDefault())
        val computedTrigger = computeTriggerAtMillis(
            dueEpochDay = reminder.dueEpochDay,
            dueMinutes = reminder.dueMinutes,
            minutesBefore = reminder.minutesBefore,
            timeZone = tz
        )
        val now = System.currentTimeMillis()
        if (computedTrigger <= now - GRACE_WINDOW_MS) {
            // Reminder is already well in the past; cancel pending alarms without firing stale alerts
            cancelReminder(reminder.targetType, reminder.targetId)
            return
        }

        val triggerAt = computedTrigger.coerceAtLeast(now + 5_000L)
        val exactAllowed = canScheduleExactAlarms()
        val updated = reminder.copy(
            triggerAtMillis = triggerAt,
            useExactAlarm = exactAllowed,
            updatedAtMillis = now
        )
        dao.insertReminder(updated)

        scheduleAlarmAndFallbackWork(updated, exactAllowed)
    }

    suspend fun cancelReminder(targetType: ReminderTargetType, targetId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val pendingIntent = buildAlarmPendingIntent(targetType, targetId, "", 0)
        alarmManager?.cancel(pendingIntent)
        pendingIntent.cancel()

        WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName(targetType, targetId))
    }

    suspend fun rescheduleAll(timeZone: TimeZone = TimeZone.getDefault()) {
        val active = dao.getActiveReminders()
        val now = System.currentTimeMillis()
        val exactAllowed = canScheduleExactAlarms()
        active.forEach { item ->
            val recomputedTrigger = computeTriggerAtMillis(
                dueEpochDay = item.dueEpochDay,
                dueMinutes = item.dueMinutes,
                minutesBefore = item.minutesBefore,
                timeZone = timeZone
            )
            if (recomputedTrigger <= now - GRACE_WINDOW_MS) {
                cancelReminder(item.targetType, item.targetId)
            } else {
                val safeTrigger = recomputedTrigger.coerceAtLeast(now + 5_000L)
                val updated = item.copy(
                    triggerAtMillis = safeTrigger,
                    timeZoneId = timeZone.id,
                    useExactAlarm = exactAllowed,
                    updatedAtMillis = now
                )
                dao.updateReminder(updated)
                scheduleAlarmAndFallbackWork(updated, exactAllowed)
            }
        }
        ensurePeriodicResyncWork()
    }

    fun ensurePeriodicResyncWork() {
        val request = PeriodicWorkRequestBuilder<ReminderResyncWorker>(12, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_RESYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun scheduleAlarmAndFallbackWork(
        reminder: ReminderConfigEntity,
        exactAllowed: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val pendingIntent = buildAlarmPendingIntent(
            targetType = reminder.targetType,
            targetId = reminder.targetId,
            title = reminder.title,
            minutesBefore = reminder.minutesBefore
        )

        if (alarmManager != null) {
            if (exactAllowed) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.triggerAtMillis,
                        pendingIntent
                    )
                } catch (_: SecurityException) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.triggerAtMillis,
                    pendingIntent
                )
            }
        }

        // Always pair with deferrable WorkManager fallback so reminders still fire even if exact alarm is denied/deferred
        val delayMs = (reminder.triggerAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val inputData = Data.Builder()
            .putString(ReminderWorker.KEY_TARGET_TYPE, reminder.targetType.name)
            .putLong(ReminderWorker.KEY_TARGET_ID, reminder.targetId)
            .putString(ReminderWorker.KEY_TITLE, reminder.title)
            .putInt(ReminderWorker.KEY_MINUTES_BEFORE, reminder.minutesBefore)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName(reminder.targetType, reminder.targetId),
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun buildAlarmPendingIntent(
        targetType: ReminderTargetType,
        targetId: Long,
        title: String,
        minutesBefore: Int
    ): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_FIRE_REMINDER
            putExtra(ReminderWorker.KEY_TARGET_TYPE, targetType.name)
            putExtra(ReminderWorker.KEY_TARGET_ID, targetId)
            putExtra(ReminderWorker.KEY_TITLE, title)
            putExtra(ReminderWorker.KEY_MINUTES_BEFORE, minutesBefore)
        }
        val requestCode = ((targetType.ordinal + 1) * 100_000) + (targetId.toInt() and 0xFFFF)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_FIRE_REMINDER = "com.omlakhera.kronoza.kronoza.ACTION_FIRE_REMINDER"
        private const val PERIODIC_RESYNC_WORK_NAME = "kronoza_reminder_resync"
        private const val GRACE_WINDOW_MS = 120_000L

        fun uniqueWorkName(type: ReminderTargetType, targetId: Long): String =
            "kronoza_reminder_${type.name.lowercase()}_$targetId"

        fun computeTriggerAtMillis(
            dueEpochDay: Long,
            dueMinutes: Int,
            minutesBefore: Int,
            timeZone: TimeZone = TimeZone.getDefault()
        ): Long {
            val date = KronozaDate.fromEpochDay(dueEpochDay)
            val targetMillis = date.epochMillisAt(dueMinutes, timeZone)
            val leadMillis = minutesBefore.coerceAtLeast(0) * 60_000L
            return targetMillis - leadMillis
        }
    }
}
