package com.omlakhera.kronoza.data.local

import androidx.room.TypeConverter
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
import org.json.JSONArray
import org.json.JSONObject

class KronozaConverters {

    @TypeConverter
    fun fromEventCategory(value: EventCategory): String = value.name

    @TypeConverter
    fun toEventCategory(raw: String): EventCategory =
        enumValues<EventCategory>().firstOrNull { it.name == raw } ?: EventCategory.WORK

    @TypeConverter
    fun fromRepeatFrequency(value: RepeatFrequency): String = value.name

    @TypeConverter
    fun toRepeatFrequency(raw: String): RepeatFrequency =
        enumValues<RepeatFrequency>().firstOrNull { it.name == raw } ?: RepeatFrequency.NONE

    @TypeConverter
    fun fromReminderOption(value: ReminderOption): String = value.name

    @TypeConverter
    fun toReminderOption(raw: String): ReminderOption =
        enumValues<ReminderOption>().firstOrNull { it.name == raw } ?: ReminderOption.FIFTEEN_MINS

    @TypeConverter
    fun fromReminderTargetType(value: ReminderTargetType): String = value.name

    @TypeConverter
    fun toReminderTargetType(raw: String): ReminderTargetType =
        enumValues<ReminderTargetType>().firstOrNull { it.name == raw } ?: ReminderTargetType.EVENT

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(raw: String): TaskPriority =
        enumValues<TaskPriority>().firstOrNull { it.name == raw } ?: TaskPriority.MEDIUM

    @TypeConverter
    fun fromTaskCategory(value: TaskCategory): String = value.name

    @TypeConverter
    fun toTaskCategory(raw: String): TaskCategory =
        enumValues<TaskCategory>().firstOrNull { it.name == raw } ?: TaskCategory.WORK

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name

    @TypeConverter
    fun toNotificationType(raw: String): NotificationType =
        enumValues<NotificationType>().firstOrNull { it.name == raw } ?: NotificationType.EVENT

    @TypeConverter
    fun fromLongList(list: List<Long>): String {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    @TypeConverter
    fun toLongList(raw: String): List<Long> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { idx -> arr.optLong(idx, 0L) }
        }.getOrDefault(emptyList())
    }

    @TypeConverter
    fun fromSubtaskList(list: List<SubtaskItem>): String {
        val arr = JSONArray()
        list.forEach { item ->
            arr.put(JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("done", item.done)
            })
        }
        return arr.toString()
    }

    @TypeConverter
    fun toSubtaskList(raw: String): List<SubtaskItem> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { idx ->
                val obj = arr.getJSONObject(idx)
                SubtaskItem(
                    id = obj.optString("id", idx.toString()),
                    title = obj.optString("title", ""),
                    done = obj.optBoolean("done", false)
                )
            }
        }.getOrDefault(emptyList())
    }

    @TypeConverter
    fun fromAgendaList(list: List<AgendaCheckItem>): String {
        val arr = JSONArray()
        list.forEach { item ->
            arr.put(JSONObject().apply {
                put("id", item.id)
                put("text", item.text)
                put("done", item.done)
            })
        }
        return arr.toString()
    }

    @TypeConverter
    fun toAgendaList(raw: String): List<AgendaCheckItem> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { idx ->
                val obj = arr.getJSONObject(idx)
                AgendaCheckItem(
                    id = obj.optString("id", idx.toString()),
                    text = obj.optString("text", ""),
                    done = obj.optBoolean("done", false)
                )
            }
        }.getOrDefault(emptyList())
    }

    @TypeConverter
    fun fromAttendeeList(list: List<AttendeeItem>): String {
        val arr = JSONArray()
        list.forEach { item ->
            arr.put(JSONObject().apply {
                put("name", item.name)
                put("role", item.role)
                put("accepted", item.accepted)
                put("avatarKey", item.avatarKey)
            })
        }
        return arr.toString()
    }

    @TypeConverter
    fun toAttendeeList(raw: String): List<AttendeeItem> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { idx ->
                val obj = arr.getJSONObject(idx)
                AttendeeItem(
                    name = obj.optString("name", ""),
                    role = obj.optString("role", ""),
                    accepted = obj.optBoolean("accepted", true),
                    avatarKey = obj.optString("avatarKey", "")
                )
            }
        }.getOrDefault(emptyList())
    }
}
