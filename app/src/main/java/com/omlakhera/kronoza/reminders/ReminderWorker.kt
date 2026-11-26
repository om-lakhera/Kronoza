package com.omlakhera.kronoza.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.omlakhera.kronoza.data.local.KronozaDatabase
import com.omlakhera.kronoza.data.local.NotificationEntity
import com.omlakhera.kronoza.data.local.PreferencesRepository
import com.omlakhera.kronoza.model.NotificationType
import com.omlakhera.kronoza.model.ReminderTargetType
import com.omlakhera.kronoza.model.RepeatFrequency
import com.omlakhera.kronoza.util.CalendarEngine
import java.util.TimeZone

internal object ReminderDispatch {
    suspend fun fireIfPending(
        context: Context,
        targetType: ReminderTargetType,
        targetId: Long,
        fallbackTitle: String,
        fallbackMinutesBefore: Int
    ) {
        val db = KronozaDatabase.getInstance(context)
        val dao = db.kronozaDao()
        val prefs = PreferencesRepository(context).getPreferencesOnce()
        val config = dao.getReminderForTarget(targetType, targetId)
        if (config != null && (!config.enabled || config.fired)) {
            return
        }

        val typeEnabled = when (targetType) {
            ReminderTargetType.EVENT -> prefs.eventRemindersEnabled
            ReminderTargetType.TASK -> prefs.taskRemindersEnabled
        }

        val title = config?.title ?: fallbackTitle.ifBlank { "Upcoming Kronoza item" }
        val minsBefore = config?.minutesBefore ?: fallbackMinutesBefore
        val prefix = if (targetType == ReminderTargetType.EVENT) "Event Reminder" else "Task Due Soon"
        val body = if (minsBefore > 0) {
            "$title starts in $minsBefore mins"
        } else {
            "$title is starting now"
        }

        if (typeEnabled) {
            KronozaNotificationHelper.showReminderNotification(
                context = context,
                targetType = targetType,
                targetId = targetId,
                title = prefix,
                body = body,
                playSound = prefs.notificationSounds
            )

            dao.insertNotification(
                NotificationEntity(
                    title = body,
                    message = if (targetType == ReminderTargetType.EVENT) {
                        "Calendar reminder delivered"
                    } else {
                        "Scheduled task reminder"
                    },
                    type = if (targetType == ReminderTargetType.EVENT) NotificationType.EVENT else NotificationType.TASK,
                    isToday = true,
                    timeAgoLabel = "Just now",
                    metaChipLabel = if (minsBefore > 0) "In ${minsBefore}m" else "Due now",
                    unread = true,
                    linkedEventId = if (targetType == ReminderTargetType.EVENT) targetId else null
                )
            )
        }

        // If this is a recurring event, automatically advance & schedule the next occurrence
        if (targetType == ReminderTargetType.EVENT && config != null) {
            val event = dao.getEventById(targetId)
            if (event != null && event.repeatFrequency != RepeatFrequency.NONE) {
                val nextDay = CalendarEngine.nextOccurrenceEpochDay(event, config.dueEpochDay)
                if (nextDay != null) {
                    val tz = TimeZone.getDefault()
                    val dueMins = if (event.allDay) 9 * 60 else event.startMinutes
                    val nextTrigger = KronozaReminderScheduler.computeTriggerAtMillis(
                        dueEpochDay = nextDay,
                        dueMinutes = dueMins,
                        minutesBefore = minsBefore,
                        timeZone = tz
                    )
                    val scheduler = KronozaReminderScheduler(context, dao)
                    val nextConfig = config.copy(
                        dueEpochDay = nextDay,
                        dueMinutes = dueMins,
                        triggerAtMillis = nextTrigger,
                        timeZoneId = tz.id,
                        fired = false,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                    dao.insertReminder(nextConfig)
                    scheduler.scheduleOrUpdate(nextConfig)
                    return
                }
            }
        }

        dao.markReminderFired(targetType, targetId)
    }
}

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val rawType = inputData.getString(KEY_TARGET_TYPE) ?: ReminderTargetType.EVENT.name
        val targetType = enumValues<ReminderTargetType>().firstOrNull { it.name == rawType }
            ?: ReminderTargetType.EVENT
        val targetId = inputData.getLong(KEY_TARGET_ID, -1L)
        if (targetId <= 0L) return Result.success()

        val title = inputData.getString(KEY_TITLE) ?: ""
        val minutesBefore = inputData.getInt(KEY_MINUTES_BEFORE, 15)

        ReminderDispatch.fireIfPending(
            context = applicationContext,
            targetType = targetType,
            targetId = targetId,
            fallbackTitle = title,
            fallbackMinutesBefore = minutesBefore
        )
        return Result.success()
    }

    companion object {
        const val KEY_TARGET_TYPE = "target_type"
        const val KEY_TARGET_ID = "target_id"
        const val KEY_TITLE = "title"
        const val KEY_MINUTES_BEFORE = "minutes_before"
    }
}

class ReminderResyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        KronozaReminderScheduler(applicationContext).rescheduleAll()
        return Result.success()
    }
}
