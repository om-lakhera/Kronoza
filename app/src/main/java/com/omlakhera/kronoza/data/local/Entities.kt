package com.omlakhera.kronoza.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.omlakhera.kronoza.model.AgendaCheckItem
import com.omlakhera.kronoza.model.AttendeeItem
import com.omlakhera.kronoza.model.EventCategory
import com.omlakhera.kronoza.model.NotificationType
import com.omlakhera.kronoza.model.ReminderOption
import com.omlakhera.kronoza.model.ReminderTargetType
import com.omlakhera.kronoza.model.RepeatFrequency
import com.omlakhera.kronoza.model.SubtaskItem
import com.omlakhera.kronoza.model.TaskCategory
import com.omlakhera.kronoza.model.TaskPriority

@Entity(
    tableName = "events",
    indices = [
        Index(value = ["dateEpochDay"]),
        Index(value = ["seriesId"])
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val dateEpochDay: Long,
    val endDateEpochDay: Long = 0L, // 0L or <= dateEpochDay means single-day event
    val startMinutes: Int, // minutes from midnight (0..1439)
    val endMinutes: Int,
    val allDay: Boolean = false,
    val repeatFrequency: RepeatFrequency = RepeatFrequency.NONE,
    val seriesId: String = "",
    val excludedEpochDays: List<Long> = emptyList(),
    val location: String = "",
    val meetingUrl: String = "",
    val category: EventCategory = EventCategory.WORK,
    val customColorHex: String = "",
    val reminder: ReminderOption = ReminderOption.FIFTEEN_MINS,
    val notes: String = "",
    val badgeLabel: String = "",
    val isMilestone: Boolean = false,
    val attendees: List<AttendeeItem> = emptyList(),
    val checklist: List<AgendaCheckItem> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val effectiveEndDateEpochDay: Long
        get() = if (endDateEpochDay > dateEpochDay) endDateEpochDay else dateEpochDay

    val isMultiDay: Boolean
        get() = effectiveEndDateEpochDay > dateEpochDay
}

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["dueEpochDay"]),
        Index(value = ["completed"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String = "",
    val dueEpochDay: Long,
    val dueMinutes: Int = 14 * 60, // 2:00 PM default
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: TaskCategory = TaskCategory.DESIGN,
    val completed: Boolean = false,
    val reminderEnabled: Boolean = true,
    val subtasks: List<SubtaskItem> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val message: String,
    val type: NotificationType,
    val isToday: Boolean = true,
    val timeAgoLabel: String = "Just now",
    val metaChipLabel: String = "",
    val unread: Boolean = true,
    val linkedEventId: Long? = null,
    val inviteStatus: String = "", // "", "PENDING", "ACCEPTED", "DECLINED"
    val inviteDateEpochDay: Long? = null,
    val inviteStartMinutes: Int? = null,
    val inviteEndMinutes: Int? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["targetType", "targetId"])
    ]
)
data class ReminderConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val targetType: ReminderTargetType,
    val targetId: Long,
    val title: String,
    val dueEpochDay: Long,
    val dueMinutes: Int,
    val minutesBefore: Int,
    val triggerAtMillis: Long,
    val timeZoneId: String,
    val enabled: Boolean = true,
    val fired: Boolean = false,
    val useExactAlarm: Boolean = true,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
