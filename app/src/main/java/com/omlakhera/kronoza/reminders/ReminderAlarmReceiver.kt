package com.omlakhera.kronoza.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.omlakhera.kronoza.model.ReminderTargetType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != KronozaReminderScheduler.ACTION_FIRE_REMINDER) return
        val pendingResult = goAsync()
        val rawType = intent.getStringExtra(ReminderWorker.KEY_TARGET_TYPE) ?: ReminderTargetType.EVENT.name
        val targetType = enumValues<ReminderTargetType>().firstOrNull { it.name == rawType }
            ?: ReminderTargetType.EVENT
        val targetId = intent.getLongExtra(ReminderWorker.KEY_TARGET_ID, -1L)
        val title = intent.getStringExtra(ReminderWorker.KEY_TITLE) ?: ""
        val minutesBefore = intent.getIntExtra(ReminderWorker.KEY_MINUTES_BEFORE, 15)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (targetId > 0L) {
                    ReminderDispatch.fireIfPending(
                        context = context.applicationContext,
                        targetType = targetType,
                        targetId = targetId,
                        fallbackTitle = title,
                        fallbackMinutesBefore = minutesBefore
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
