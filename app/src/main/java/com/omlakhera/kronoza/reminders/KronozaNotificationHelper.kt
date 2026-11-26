package com.omlakhera.kronoza.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.omlakhera.kronoza.MainActivity
import com.omlakhera.kronoza.R
import com.omlakhera.kronoza.model.ReminderTargetType

object KronozaNotificationHelper {

    const val EVENT_CHANNEL_ID = "kronoza_event_reminders"
    const val TASK_CHANNEL_ID = "kronoza_task_reminders"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return
            if (manager.getNotificationChannel(EVENT_CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        EVENT_CHANNEL_ID,
                        "Event Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Alerts for upcoming calendar events and recurring sessions"
                        enableVibration(true)
                    }
                )
            }
            if (manager.getNotificationChannel(TASK_CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        TASK_CHANNEL_ID,
                        "Task Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Alerts for upcoming and due tasks"
                        enableVibration(true)
                    }
                )
            }
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun showReminderNotification(
        context: Context,
        targetType: ReminderTargetType,
        targetId: Long,
        title: String,
        body: String,
        playSound: Boolean = true
    ) {
        ensureChannels(context)
        if (!hasNotificationPermission(context)) return

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("extra_target_type", targetType.name)
            putExtra("extra_target_id", targetId)
        }
        val requestCode = ((targetType.ordinal + 1) * 100_000) + (targetId.toInt() and 0xFFFF)
        val contentIntent = PendingIntent.getActivity(
            context,
            requestCode,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (targetType == ReminderTargetType.EVENT) EVENT_CHANNEL_ID else TASK_CHANNEL_ID
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_kronoza_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)

        if (!playSound) {
            builder.setSilent(true)
        }

        runCatching {
            NotificationManagerCompat.from(context).notify(requestCode, builder.build())
        }
    }
}
