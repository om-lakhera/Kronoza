package com.omlakhera.kronoza.data.backup

import android.content.Context
import android.net.Uri
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
import com.omlakhera.kronoza.model.ReminderOption
import com.omlakhera.kronoza.model.ReminderTargetType
import com.omlakhera.kronoza.model.RepeatFrequency
import com.omlakhera.kronoza.model.SubtaskItem
import com.omlakhera.kronoza.model.TaskCategory
import com.omlakhera.kronoza.model.TaskPriority
import com.omlakhera.kronoza.model.ThemeMode
import com.omlakhera.kronoza.model.UserPreferences
import com.omlakhera.kronoza.reminders.KronozaReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.TimeZone

data class KronozaBackupSnapshot(
    val schemaVersion: Int = KronozaBackupCodec.CURRENT_SCHEMA_VERSION,
    val exportedAtMillis: Long,
    val preferences: UserPreferences,
    val events: List<EventEntity>,
    val tasks: List<TaskEntity>,
    val notifications: List<NotificationEntity>,
    val reminders: List<ReminderConfigEntity>
)

sealed class BackupResult {
    data class ExportSuccess(val eventCount: Int, val taskCount: Int, val reminderCount: Int) : BackupResult()
    data class RestoreSuccess(
        val eventCount: Int,
        val taskCount: Int,
        val reminderCount: Int,
        val skippedDuplicates: Int = 0
    ) : BackupResult()
    data class Failure(val message: String) : BackupResult()
}

class KronozaBackupManager(
    private val context: Context,
    private val dao: KronozaDao = KronozaDatabase.getInstance(context).kronozaDao(),
    private val prefsRepo: PreferencesRepository = PreferencesRepository(context),
    private val scheduler: KronozaReminderScheduler = KronozaReminderScheduler(context, dao)
) {

    suspend fun exportToUri(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        runCatching {
            val snapshot = KronozaBackupSnapshot(
                schemaVersion = KronozaBackupCodec.CURRENT_SCHEMA_VERSION,
                exportedAtMillis = System.currentTimeMillis(),
                preferences = prefsRepo.getPreferencesOnce(),
                events = dao.getAllEventsOnce(),
                tasks = dao.getAllTasksOnce(),
                notifications = dao.getAllNotificationsOnce(),
                reminders = dao.getAllRemindersOnce()
            )
            val json = KronozaBackupCodec.encode(snapshot)
            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: error("Could not open destination file for writing.")
            outputStream.use { stream ->
                stream.write(json.toByteArray(Charsets.UTF_8))
                stream.flush()
            }
            BackupResult.ExportSuccess(
                eventCount = snapshot.events.size,
                taskCount = snapshot.tasks.size,
                reminderCount = snapshot.reminders.size
            )
        }.getOrElse { err ->
            BackupResult.Failure(err.message ?: "Backup export failed.")
        }
    }

    suspend fun restoreFromUri(
        uri: Uri,
        mode: BackupRestoreMode = BackupRestoreMode.MERGE_SKIP_DUPLICATES
    ): BackupResult = withContext(Dispatchers.IO) {
        runCatching {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: error("Could not open selected backup file.")
            val rawJson = inputStream.use { stream ->
                val maxBytes = 10 * 1024 * 1024
                val buffer = ByteArray(8192)
                val out = java.io.ByteArrayOutputStream()
                var total = 0
                while (true) {
                    val read = stream.read(buffer)
                    if (read <= 0) break
                    total += read
                    if (total > maxBytes) {
                        error("Backup file exceeds the 10 MB maximum supported size.")
                    }
                    out.write(buffer, 0, read)
                }
                out.toString(Charsets.UTF_8.name())
            }
            val snapshot = KronozaBackupCodec.decodeAndVerify(rawJson)

            when (mode) {
                BackupRestoreMode.REPLACE_ALL -> {
                    dao.replaceAllData(
                        events = snapshot.events,
                        tasks = snapshot.tasks,
                        notifications = snapshot.notifications,
                        reminders = snapshot.reminders
                    )
                    val currentPhoto = prefsRepo.getPreferencesOnce().profilePhotoPath
                    prefsRepo.restorePreferences(snapshot.preferences.copy(profilePhotoPath = currentPhoto))
                    scheduler.rescheduleAll(TimeZone.getDefault())

                    BackupResult.RestoreSuccess(
                        eventCount = snapshot.events.size,
                        taskCount = snapshot.tasks.size,
                        reminderCount = snapshot.reminders.size,
                        skippedDuplicates = 0
                    )
                }
                BackupRestoreMode.MERGE_SKIP_DUPLICATES -> {
                    val existingEvents = dao.getAllEventsOnce()
                    val existingTasks = dao.getAllTasksOnce()

                    val existingEventKeys = existingEvents.map {
                        Triple(it.title.trim().lowercase(), it.dateEpochDay, it.startMinutes)
                    }.toHashSet()
                    val existingTaskKeys = existingTasks.map {
                        Triple(it.title.trim().lowercase(), it.dueEpochDay, it.dueMinutes)
                    }.toHashSet()

                    val newEvents = snapshot.events.filter {
                        existingEventKeys.add(
                            Triple(it.title.trim().lowercase(), it.dateEpochDay, it.startMinutes)
                        )
                    }.map { it.copy(id = 0L) }

                    val newTasks = snapshot.tasks.filter {
                        existingTaskKeys.add(
                            Triple(it.title.trim().lowercase(), it.dueEpochDay, it.dueMinutes)
                        )
                    }.map { it.copy(id = 0L) }

                    val skipped = (snapshot.events.size - newEvents.size) + (snapshot.tasks.size - newTasks.size)

                    var remindersAdded = 0
                    val tz = TimeZone.getDefault()
                    newEvents.forEach { ev ->
                        val newId = dao.insertEvent(ev)
                        if (ev.reminder != ReminderOption.NONE) {
                            val mins = if (ev.allDay) 9 * 60 else ev.startMinutes
                            val trigger = KronozaReminderScheduler.computeTriggerAtMillis(
                                dueEpochDay = ev.dateEpochDay,
                                dueMinutes = mins,
                                minutesBefore = ev.reminder.minutesBefore,
                                timeZone = tz
                            )
                            dao.insertReminder(
                                ReminderConfigEntity(
                                    targetType = ReminderTargetType.EVENT,
                                    targetId = newId,
                                    title = ev.title,
                                    dueEpochDay = ev.dateEpochDay,
                                    dueMinutes = mins,
                                    minutesBefore = ev.reminder.minutesBefore,
                                    triggerAtMillis = trigger,
                                    timeZoneId = tz.id,
                                    enabled = true,
                                    fired = false
                                )
                            )
                            remindersAdded++
                        }
                    }
                    newTasks.forEach { task ->
                        val newId = dao.insertTask(task)
                        if (task.reminderEnabled && !task.completed) {
                            val trigger = KronozaReminderScheduler.computeTriggerAtMillis(
                                dueEpochDay = task.dueEpochDay,
                                dueMinutes = task.dueMinutes,
                                minutesBefore = 15,
                                timeZone = tz
                            )
                            dao.insertReminder(
                                ReminderConfigEntity(
                                    targetType = ReminderTargetType.TASK,
                                    targetId = newId,
                                    title = task.title,
                                    dueEpochDay = task.dueEpochDay,
                                    dueMinutes = task.dueMinutes,
                                    minutesBefore = 15,
                                    triggerAtMillis = trigger,
                                    timeZoneId = tz.id,
                                    enabled = true,
                                    fired = false
                                )
                            )
                            remindersAdded++
                        }
                    }

                    scheduler.rescheduleAll(tz)
                    BackupResult.RestoreSuccess(
                        eventCount = newEvents.size,
                        taskCount = newTasks.size,
                        reminderCount = remindersAdded,
                        skippedDuplicates = skipped
                    )
                }
            }
        }.getOrElse { err ->
            BackupResult.Failure(err.message ?: "Backup restore failed.")
        }
    }
}

/**
 * Pure Kotlin RFC-8259 JSON codec + SHA-256 integrity verifier for Kronoza backups.
 * Supports both schema v1 and v2 snapshots.
 */
object KronozaBackupCodec {

    const val CURRENT_SCHEMA_VERSION = 2
    private const val APP_IDENTIFIER = "Kronoza"

    fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun encode(snapshot: KronozaBackupSnapshot): String {
        val payloadJson = encodePayload(snapshot, snapshot.schemaVersion)
        val checksum = sha256Hex(payloadJson)
        return buildString {
            append('{')
            append("\"app\":\"").append(APP_IDENTIFIER).append("\",")
            append("\"schemaVersion\":").append(snapshot.schemaVersion).append(',')
            append("\"exportedAtMillis\":").append(snapshot.exportedAtMillis).append(',')
            append("\"checksum\":\"").append(checksum).append("\",")
            append("\"payload\":").append(payloadJson)
            append('}')
        }
    }

    fun decodeAndVerify(rawJson: String): KronozaBackupSnapshot {
        val root = JsonParser(rawJson).parseObject()
        val app = root["app"] as? String ?: throw IllegalArgumentException("Invalid backup: missing app identifier.")
        if (app != APP_IDENTIFIER) {
            throw IllegalArgumentException("Unsupported backup source: $app.")
        }
        val schemaVersion = (root["schemaVersion"] as? Number)?.toInt()
            ?: throw IllegalArgumentException("Invalid backup: missing schemaVersion.")
        if (schemaVersion !in 1..CURRENT_SCHEMA_VERSION) {
            throw IllegalArgumentException("Unsupported backup schema version: $schemaVersion.")
        }
        val exportedAtMillis = (root["exportedAtMillis"] as? Number)?.toLong() ?: 0L
        val expectedChecksum = root["checksum"] as? String
            ?: throw IllegalArgumentException("Invalid backup: missing integrity checksum.")
        @Suppress("UNCHECKED_CAST")
        val payloadMap = root["payload"] as? Map<String, Any?>
            ?: throw IllegalArgumentException("Invalid backup: missing payload.")

        val prefsMap = payloadMap["preferences"] as? Map<*, *>
            ?: throw IllegalArgumentException("Invalid backup: missing preferences.")
        val eventsList = payloadMap["events"] as? List<*>
            ?: throw IllegalArgumentException("Invalid backup: missing events.")
        val tasksList = payloadMap["tasks"] as? List<*>
            ?: throw IllegalArgumentException("Invalid backup: missing tasks.")
        val notificationsList = payloadMap["notifications"] as? List<*> ?: emptyList<Any?>()
        val remindersList = payloadMap["reminders"] as? List<*> ?: emptyList<Any?>()

        val snapshot = KronozaBackupSnapshot(
            schemaVersion = schemaVersion,
            exportedAtMillis = exportedAtMillis,
            preferences = decodePreferences(prefsMap),
            events = eventsList.map { decodeEvent(it as Map<*, *>) },
            tasks = tasksList.map { decodeTask(it as Map<*, *>) },
            notifications = notificationsList.map { decodeNotification(it as Map<*, *>) },
            reminders = remindersList.map { decodeReminder(it as Map<*, *>) }
        )

        val recomputedChecksum = sha256Hex(encodePayload(snapshot, schemaVersion))
        if (!recomputedChecksum.equals(expectedChecksum, ignoreCase = true)) {
            throw IllegalArgumentException("Backup integrity check failed: checksum mismatch.")
        }

        return snapshot
    }

    private fun encodePayload(snapshot: KronozaBackupSnapshot, version: Int): String = buildString {
        append('{')
        append("\"preferences\":").append(encodePreferences(snapshot.preferences, version)).append(',')
        append("\"events\":[").append(snapshot.events.joinToString(",") { encodeEvent(it, version) }).append("],")
        append("\"tasks\":[").append(snapshot.tasks.joinToString(",") { encodeTask(it, version) }).append("],")
        append("\"notifications\":[").append(snapshot.notifications.joinToString(",") { encodeNotification(it) }).append("],")
        append("\"reminders\":[").append(snapshot.reminders.joinToString(",") { encodeReminder(it) }).append(']')
        append('}')
    }

    private fun encodePreferences(p: UserPreferences, version: Int): String = buildString {
        append('{')
        append("\"userName\":").append(jsonStr(p.userName)).append(',')
        append("\"userEmail\":").append(jsonStr(p.userEmail)).append(',')
        append("\"themeMode\":").append(jsonStr(p.themeMode.name)).append(',')
        append("\"accentPreset\":").append(jsonStr(p.accentPreset.name)).append(',')
        append("\"defaultCalendarView\":").append(jsonStr(p.defaultCalendarView.name)).append(',')
        append("\"startWeekMonday\":").append(p.startWeekMonday).append(',')
        append("\"notificationSounds\":").append(p.notificationSounds).append(',')
        append("\"dailyFocusGoal\":").append(p.dailyFocusGoal).append(',')
        append("\"onboardingCompleted\":").append(p.onboardingCompleted)
        if (version >= 2) {
            append(",\"dateFormat\":").append(jsonStr(p.dateFormat.name))
            append(",\"timeFormat24h\":").append(p.timeFormat24h)
            append(",\"defaultEventDurationMinutes\":").append(p.defaultEventDurationMinutes)
            append(",\"defaultReminder\":").append(jsonStr(p.defaultReminder.name))
            append(",\"eventRemindersEnabled\":").append(p.eventRemindersEnabled)
            append(",\"taskRemindersEnabled\":").append(p.taskRemindersEnabled)
            append(",\"preferredCategories\":[").append(
                p.preferredCategories.joinToString(",") { jsonStr(it.name) }
            ).append(']')
        }
        append('}')
    }

    private fun decodePreferences(m: Map<*, *>): UserPreferences {
        val catsRaw = (m["preferredCategories"] as? List<*>)
            ?.mapNotNull { name -> enumValues<EventCategory>().firstOrNull { it.name == (name as? String) } }
            ?.toSet()
            ?.ifEmpty { EventCategory.entries.toSet() }
            ?: EventCategory.entries.toSet()

        return UserPreferences(
            userName = m.str("userName", ""),
            userEmail = m.str("userEmail", ""),
            themeMode = enumOrDefault(m.str("themeMode"), ThemeMode.LIGHT),
            accentPreset = enumOrDefault(m.str("accentPreset"), AccentPreset.LAVENDER),
            defaultCalendarView = enumOrDefault(m.str("defaultCalendarView"), CalendarViewMode.MONTH),
            startWeekMonday = m.bool("startWeekMonday", true),
            dateFormat = enumOrDefault(m.str("dateFormat"), DateFormatOption.MMM_D_YYYY),
            timeFormat24h = m.bool("timeFormat24h", false),
            defaultEventDurationMinutes = m.int("defaultEventDurationMinutes", 60).coerceIn(15, 240),
            defaultReminder = enumOrDefault(m.str("defaultReminder"), ReminderOption.FIFTEEN_MINS),
            eventRemindersEnabled = m.bool("eventRemindersEnabled", true),
            taskRemindersEnabled = m.bool("taskRemindersEnabled", true),
            notificationSounds = m.bool("notificationSounds", true),
            dailyFocusGoal = m.int("dailyFocusGoal", 4).coerceIn(1, 8),
            preferredCategories = catsRaw,
            onboardingCompleted = m.bool("onboardingCompleted", true)
        )
    }

    private fun encodeEvent(e: EventEntity, version: Int): String = buildString {
        append('{')
        append("\"id\":").append(e.id).append(',')
        append("\"title\":").append(jsonStr(e.title)).append(',')
        append("\"dateEpochDay\":").append(e.dateEpochDay).append(',')
        append("\"startMinutes\":").append(e.startMinutes).append(',')
        append("\"endMinutes\":").append(e.endMinutes).append(',')
        append("\"allDay\":").append(e.allDay).append(',')
        append("\"repeatFrequency\":").append(jsonStr(e.repeatFrequency.name)).append(',')
        append("\"location\":").append(jsonStr(e.location)).append(',')
        append("\"meetingUrl\":").append(jsonStr(e.meetingUrl)).append(',')
        append("\"category\":").append(jsonStr(e.category.name)).append(',')
        append("\"reminder\":").append(jsonStr(e.reminder.name)).append(',')
        append("\"notes\":").append(jsonStr(e.notes)).append(',')
        append("\"badgeLabel\":").append(jsonStr(e.badgeLabel)).append(',')
        append("\"isMilestone\":").append(e.isMilestone).append(',')
        append("\"attendees\":[").append(e.attendees.joinToString(",") {
            "{\"name\":${jsonStr(it.name)},\"role\":${jsonStr(it.role)},\"accepted\":${it.accepted},\"avatarKey\":${jsonStr(it.avatarKey)}}"
        }).append("],")
        append("\"checklist\":[").append(e.checklist.joinToString(",") {
            "{\"id\":${jsonStr(it.id)},\"text\":${jsonStr(it.text)},\"done\":${it.done}}"
        }).append("],")
        append("\"createdAtMillis\":").append(e.createdAtMillis)
        if (version >= 2) {
            append(",\"endDateEpochDay\":").append(e.endDateEpochDay)
            append(",\"seriesId\":").append(jsonStr(e.seriesId))
            append(",\"excludedEpochDays\":[").append(e.excludedEpochDays.joinToString(",")).append(']')
            append(",\"customColorHex\":").append(jsonStr(e.customColorHex))
            append(",\"updatedAtMillis\":").append(e.updatedAtMillis)
        }
        append('}')
    }

    private fun decodeEvent(m: Map<*, *>): EventEntity {
        val attendeesRaw = m["attendees"] as? List<*> ?: emptyList<Any?>()
        val checklistRaw = m["checklist"] as? List<*> ?: emptyList<Any?>()
        val excludedRaw = m["excludedEpochDays"] as? List<*> ?: emptyList<Any?>()
        val created = m.long("createdAtMillis", 0L)
        val safeStart = m.int("startMinutes", 540).coerceIn(0, 1439)
        val safeEnd = m.int("endMinutes", 600).coerceIn(safeStart.coerceAtMost(1439), 1440)
        return EventEntity(
            id = m.long("id", 0L),
            title = m.str("title").trim().ifBlank { "Untitled Event" },
            dateEpochDay = m.long("dateEpochDay", 0L),
            endDateEpochDay = m.long("endDateEpochDay", 0L),
            startMinutes = safeStart,
            endMinutes = safeEnd,
            allDay = m.bool("allDay", false),
            repeatFrequency = enumOrDefault(m.str("repeatFrequency"), RepeatFrequency.NONE),
            seriesId = m.str("seriesId"),
            excludedEpochDays = excludedRaw.mapNotNull { (it as? Number)?.toLong() },
            location = m.str("location"),
            meetingUrl = m.str("meetingUrl"),
            category = enumOrDefault(m.str("category"), EventCategory.WORK),
            customColorHex = m.str("customColorHex"),
            reminder = enumOrDefault(m.str("reminder"), ReminderOption.FIFTEEN_MINS),
            notes = m.str("notes"),
            badgeLabel = m.str("badgeLabel"),
            isMilestone = m.bool("isMilestone", false),
            attendees = attendeesRaw.mapNotNull { item ->
                val obj = item as? Map<*, *> ?: return@mapNotNull null
                AttendeeItem(
                    name = obj.str("name"),
                    role = obj.str("role"),
                    accepted = obj.bool("accepted", true),
                    avatarKey = obj.str("avatarKey", "")
                )
            },
            checklist = checklistRaw.mapNotNull { item ->
                val obj = item as? Map<*, *> ?: return@mapNotNull null
                AgendaCheckItem(
                    id = obj.str("id"),
                    text = obj.str("text"),
                    done = obj.bool("done", false)
                )
            },
            createdAtMillis = created,
            updatedAtMillis = m.long("updatedAtMillis", created)
        )
    }

    private fun encodeTask(t: TaskEntity, version: Int): String = buildString {
        append('{')
        append("\"id\":").append(t.id).append(',')
        append("\"title\":").append(jsonStr(t.title)).append(',')
        append("\"description\":").append(jsonStr(t.description)).append(',')
        append("\"dueEpochDay\":").append(t.dueEpochDay).append(',')
        append("\"dueMinutes\":").append(t.dueMinutes).append(',')
        append("\"priority\":").append(jsonStr(t.priority.name)).append(',')
        append("\"category\":").append(jsonStr(t.category.name)).append(',')
        append("\"completed\":").append(t.completed).append(',')
        append("\"reminderEnabled\":").append(t.reminderEnabled).append(',')
        append("\"subtasks\":[").append(t.subtasks.joinToString(",") {
            "{\"id\":${jsonStr(it.id)},\"title\":${jsonStr(it.title)},\"done\":${it.done}}"
        }).append("],")
        append("\"createdAtMillis\":").append(t.createdAtMillis)
        if (version >= 2) {
            append(",\"updatedAtMillis\":").append(t.updatedAtMillis)
        }
        append('}')
    }

    private fun decodeTask(m: Map<*, *>): TaskEntity {
        val subtasksRaw = m["subtasks"] as? List<*> ?: emptyList<Any?>()
        val created = m.long("createdAtMillis", 0L)
        return TaskEntity(
            id = m.long("id", 0L),
            title = m.str("title").trim().ifBlank { "Untitled Task" },
            description = m.str("description"),
            dueEpochDay = m.long("dueEpochDay", 0L),
            dueMinutes = m.int("dueMinutes", 840).coerceIn(0, 1439),
            priority = enumOrDefault(m.str("priority"), TaskPriority.MEDIUM),
            category = enumOrDefault(m.str("category"), TaskCategory.DESIGN),
            completed = m.bool("completed", false),
            reminderEnabled = m.bool("reminderEnabled", true),
            subtasks = subtasksRaw.mapNotNull { item ->
                val obj = item as? Map<*, *> ?: return@mapNotNull null
                SubtaskItem(
                    id = obj.str("id"),
                    title = obj.str("title"),
                    done = obj.bool("done", false)
                )
            },
            createdAtMillis = created,
            updatedAtMillis = m.long("updatedAtMillis", created)
        )
    }

    private fun encodeNotification(n: NotificationEntity): String = buildString {
        append('{')
        append("\"id\":").append(n.id).append(',')
        append("\"title\":").append(jsonStr(n.title)).append(',')
        append("\"message\":").append(jsonStr(n.message)).append(',')
        append("\"type\":").append(jsonStr(n.type.name)).append(',')
        append("\"isToday\":").append(n.isToday).append(',')
        append("\"timeAgoLabel\":").append(jsonStr(n.timeAgoLabel)).append(',')
        append("\"metaChipLabel\":").append(jsonStr(n.metaChipLabel)).append(',')
        append("\"unread\":").append(n.unread).append(',')
        append("\"linkedEventId\":").append(n.linkedEventId ?: "null").append(',')
        append("\"inviteStatus\":").append(jsonStr(n.inviteStatus)).append(',')
        append("\"inviteDateEpochDay\":").append(n.inviteDateEpochDay ?: "null").append(',')
        append("\"inviteStartMinutes\":").append(n.inviteStartMinutes ?: "null").append(',')
        append("\"inviteEndMinutes\":").append(n.inviteEndMinutes ?: "null").append(',')
        append("\"createdAtMillis\":").append(n.createdAtMillis)
        append('}')
    }

    private fun decodeNotification(m: Map<*, *>): NotificationEntity = NotificationEntity(
        id = m.long("id", 0L),
        title = m.str("title"),
        message = m.str("message"),
        type = enumOrDefault(m.str("type"), NotificationType.EVENT),
        isToday = m.bool("isToday", true),
        timeAgoLabel = m.str("timeAgoLabel", "Just now"),
        metaChipLabel = m.str("metaChipLabel"),
        unread = m.bool("unread", false),
        linkedEventId = m.nullableLong("linkedEventId"),
        inviteStatus = m.str("inviteStatus"),
        inviteDateEpochDay = m.nullableLong("inviteDateEpochDay"),
        inviteStartMinutes = m.nullableInt("inviteStartMinutes"),
        inviteEndMinutes = m.nullableInt("inviteEndMinutes"),
        createdAtMillis = m.long("createdAtMillis", 0L)
    )

    private fun encodeReminder(r: ReminderConfigEntity): String = buildString {
        append('{')
        append("\"id\":").append(r.id).append(',')
        append("\"targetType\":").append(jsonStr(r.targetType.name)).append(',')
        append("\"targetId\":").append(r.targetId).append(',')
        append("\"title\":").append(jsonStr(r.title)).append(',')
        append("\"dueEpochDay\":").append(r.dueEpochDay).append(',')
        append("\"dueMinutes\":").append(r.dueMinutes).append(',')
        append("\"minutesBefore\":").append(r.minutesBefore).append(',')
        append("\"triggerAtMillis\":").append(r.triggerAtMillis).append(',')
        append("\"timeZoneId\":").append(jsonStr(r.timeZoneId)).append(',')
        append("\"enabled\":").append(r.enabled).append(',')
        append("\"fired\":").append(r.fired).append(',')
        append("\"useExactAlarm\":").append(r.useExactAlarm).append(',')
        append("\"updatedAtMillis\":").append(r.updatedAtMillis)
        append('}')
    }

    private fun decodeReminder(m: Map<*, *>): ReminderConfigEntity = ReminderConfigEntity(
        id = m.long("id", 0L),
        targetType = enumOrDefault(m.str("targetType"), ReminderTargetType.EVENT),
        targetId = m.long("targetId", 0L),
        title = m.str("title"),
        dueEpochDay = m.long("dueEpochDay", 0L),
        dueMinutes = m.int("dueMinutes", 540),
        minutesBefore = m.int("minutesBefore", 15),
        triggerAtMillis = m.long("triggerAtMillis", 0L),
        timeZoneId = m.str("timeZoneId", "UTC"),
        enabled = m.bool("enabled", true),
        fired = m.bool("fired", false),
        useExactAlarm = m.bool("useExactAlarm", true),
        updatedAtMillis = m.long("updatedAtMillis", 0L)
    )

    private inline fun <reified T : Enum<T>> enumOrDefault(raw: String, default: T): T =
        enumValues<T>().firstOrNull { it.name == raw } ?: default

    private fun Map<*, *>.str(key: String, default: String = ""): String =
        (this[key] as? String) ?: default

    private fun Map<*, *>.bool(key: String, default: Boolean = false): Boolean =
        (this[key] as? Boolean) ?: default

    private fun Map<*, *>.int(key: String, default: Int = 0): Int =
        (this[key] as? Number)?.toInt() ?: default

    private fun Map<*, *>.long(key: String, default: Long = 0L): Long =
        (this[key] as? Number)?.toLong() ?: default

    private fun Map<*, *>.nullableLong(key: String): Long? =
        (this[key] as? Number)?.toLong()

    private fun Map<*, *>.nullableInt(key: String): Int? =
        (this[key] as? Number)?.toInt()

    private fun jsonStr(value: String): String = buildString(value.length + 2) {
        append('"')
        for (ch in value) {
            when (ch) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (ch.code < 0x20) {
                    append("\\u%04x".format(ch.code))
                } else {
                    append(ch)
                }
            }
        }
        append('"')
    }
}

private class JsonParser(private val text: String) {
    private var idx = 0
    private var depth = 0

    fun parseObject(): Map<String, Any?> {
        skipWhitespace()
        val value = readObject()
        skipWhitespace()
        if (idx < text.length) {
            throw IllegalArgumentException("Unexpected trailing characters in JSON backup.")
        }
        return value
    }

    private fun readValue(): Any? {
        skipWhitespace()
        if (idx >= text.length) throw IllegalArgumentException("Unexpected end of JSON input.")
        return when (val ch = text[idx]) {
            '{' -> readObject()
            '[' -> readArray()
            '"' -> readString()
            't' -> readLiteral("true", true)
            'f' -> readLiteral("false", false)
            'n' -> readLiteral("null", null)
            '-', in '0'..'9' -> readNumber()
            else -> throw IllegalArgumentException("Unexpected token '$ch' at index $idx.")
        }
    }

    private fun readObject(): Map<String, Any?> {
        if (++depth > 32) throw IllegalArgumentException("JSON backup exceeds maximum nesting depth.")
        expect('{')
        skipWhitespace()
        val map = LinkedHashMap<String, Any?>()
        if (peek() == '}') {
            idx++
            depth--
            return map
        }
        while (true) {
            skipWhitespace()
            val key = readString()
            skipWhitespace()
            expect(':')
            val value = readValue()
            map[key] = value
            skipWhitespace()
            when (peek()) {
                ',' -> idx++
                '}' -> {
                    idx++
                    depth--
                    return map
                }
                else -> throw IllegalArgumentException("Expected ',' or '}' in JSON object at $idx.")
            }
        }
    }

    private fun readArray(): List<Any?> {
        if (++depth > 32) throw IllegalArgumentException("JSON backup exceeds maximum nesting depth.")
        expect('[')
        skipWhitespace()
        val list = ArrayList<Any?>()
        if (peek() == ']') {
            idx++
            depth--
            return list
        }
        while (true) {
            list.add(readValue())
            skipWhitespace()
            when (peek()) {
                ',' -> idx++
                ']' -> {
                    idx++
                    depth--
                    return list
                }
                else -> throw IllegalArgumentException("Expected ',' or ']' in JSON array at $idx.")
            }
        }
    }

    private fun readString(): String {
        expect('"')
        val sb = StringBuilder()
        while (idx < text.length) {
            val ch = text[idx++]
            when (ch) {
                '"' -> return sb.toString()
                '\\' -> {
                    if (idx >= text.length) throw IllegalArgumentException("Unterminated escape sequence.")
                    when (val esc = text[idx++]) {
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        '/' -> sb.append('/')
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            if (idx + 4 > text.length) throw IllegalArgumentException("Invalid unicode escape.")
                            val code = text.substring(idx, idx + 4).toInt(16)
                            sb.append(code.toChar())
                            idx += 4
                        }
                        else -> throw IllegalArgumentException("Invalid escape '\\$esc'.")
                    }
                }
                else -> sb.append(ch)
            }
        }
        throw IllegalArgumentException("Unterminated JSON string.")
    }

    private fun readLiteral(literal: String, result: Any?): Any? {
        if (!text.startsWith(literal, idx)) {
            throw IllegalArgumentException("Invalid literal at index $idx.")
        }
        idx += literal.length
        return result
    }

    private fun readNumber(): Number {
        val start = idx
        if (text[idx] == '-') idx++
        while (idx < text.length && text[idx].isDigit()) idx++
        var isFloat = false
        if (idx < text.length && text[idx] == '.') {
            isFloat = true
            idx++
            while (idx < text.length && text[idx].isDigit()) idx++
        }
        if (idx < text.length && (text[idx] == 'e' || text[idx] == 'E')) {
            isFloat = true
            idx++
            if (idx < text.length && (text[idx] == '+' || text[idx] == '-')) idx++
            while (idx < text.length && text[idx].isDigit()) idx++
        }
        val slice = text.substring(start, idx)
        return if (isFloat) slice.toDouble() else slice.toLong()
    }

    private fun skipWhitespace() {
        while (idx < text.length && text[idx].isWhitespace()) idx++
    }

    private fun peek(): Char {
        if (idx >= text.length) throw IllegalArgumentException("Unexpected end of JSON input.")
        return text[idx]
    }

    private fun expect(expected: Char) {
        if (idx >= text.length || text[idx] != expected) {
            throw IllegalArgumentException("Expected '$expected' at index $idx.")
        }
        idx++
    }
}
