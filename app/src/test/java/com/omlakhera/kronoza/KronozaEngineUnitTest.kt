package com.omlakhera.kronoza

import com.omlakhera.kronoza.data.backup.KronozaBackupCodec
import com.omlakhera.kronoza.data.backup.KronozaBackupSnapshot
import com.omlakhera.kronoza.data.local.EventEntity
import com.omlakhera.kronoza.data.local.NotificationEntity
import com.omlakhera.kronoza.data.local.ReminderConfigEntity
import com.omlakhera.kronoza.data.local.TaskEntity
import com.omlakhera.kronoza.model.AccentPreset
import com.omlakhera.kronoza.model.AgendaCheckItem
import com.omlakhera.kronoza.model.AttendeeItem
import com.omlakhera.kronoza.model.CalendarViewMode
import com.omlakhera.kronoza.model.DateFilterOption
import com.omlakhera.kronoza.model.EventCategory
import com.omlakhera.kronoza.model.NotificationType
import com.omlakhera.kronoza.model.PriorityFilterOption
import com.omlakhera.kronoza.model.ReminderOption
import com.omlakhera.kronoza.model.ReminderTargetType
import com.omlakhera.kronoza.model.RepeatFrequency
import com.omlakhera.kronoza.model.SearchFilter
import com.omlakhera.kronoza.model.SubtaskItem
import com.omlakhera.kronoza.model.TaskCategory
import com.omlakhera.kronoza.model.TaskFilter
import com.omlakhera.kronoza.model.TaskPriority
import com.omlakhera.kronoza.model.TaskSortOption
import com.omlakhera.kronoza.model.ThemeMode
import com.omlakhera.kronoza.model.UserPreferences
import com.omlakhera.kronoza.reminders.KronozaReminderScheduler
import com.omlakhera.kronoza.util.KronozaDate
import com.omlakhera.kronoza.util.TaskFilterSort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.TimeZone

class KronozaEngineUnitTest {

    @Test
    fun leapYear_and_daysInMonth_followGregorianRules() {
        assertTrue(KronozaDate.isLeapYear(2024))
        assertTrue(KronozaDate.isLeapYear(2000))
        assertFalse(KronozaDate.isLeapYear(1900))
        assertFalse(KronozaDate.isLeapYear(2100))
        assertFalse(KronozaDate.isLeapYear(2026))

        assertEquals(29, KronozaDate.daysInMonth(2024, 2))
        assertEquals(28, KronozaDate.daysInMonth(2025, 2))
        assertEquals(29, KronozaDate.daysInMonth(2000, 2))
        assertEquals(28, KronozaDate.daysInMonth(1900, 2))
        assertEquals(30, KronozaDate.daysInMonth(2026, 4))
        assertEquals(31, KronozaDate.daysInMonth(2026, 12))
    }

    @Test
    fun monthBoundaries_and_plusMonths_clampingAreAccurate() {
        val dec31 = KronozaDate(2025, 12, 31)
        assertEquals(KronozaDate(2026, 1, 1), dec31.plusDays(1))
        assertEquals(dec31, KronozaDate(2026, 1, 1).plusDays(-1))

        val feb28Leap = KronozaDate(2024, 2, 28)
        assertEquals(KronozaDate(2024, 2, 29), feb28Leap.plusDays(1))
        assertEquals(KronozaDate(2024, 3, 1), feb28Leap.plusDays(2))

        val feb28NonLeap = KronozaDate(2025, 2, 28)
        assertEquals(KronozaDate(2025, 3, 1), feb28NonLeap.plusDays(1))

        // Clamping on month navigation
        assertEquals(KronozaDate(2024, 2, 29), KronozaDate(2024, 1, 31).plusMonths(1))
        assertEquals(KronozaDate(2025, 2, 28), KronozaDate(2025, 1, 31).plusMonths(1))
        assertEquals(KronozaDate(2025, 2, 28), KronozaDate(2024, 2, 29).plusMonths(12))
        assertEquals(KronozaDate(2026, 2, 28), KronozaDate(2026, 3, 31).plusMonths(-1))
        assertEquals(KronozaDate(2025, 12, 15), KronozaDate(2026, 1, 15).plusMonths(-1))
    }

    @Test
    fun monthGrid_alignsMondayAndSundayStartsAcrossBoundaries() {
        // Feb 2024 starts on Thursday (4) and has 29 days
        val mondayGrid = KronozaDate.buildMonthGrid(2024, 2, startMonday = true)
        assertEquals(42, mondayGrid.size)
        assertEquals(1, mondayGrid.first().dayOfWeek)
        assertEquals(KronozaDate(2024, 1, 29), mondayGrid.first())
        assertTrue(mondayGrid.contains(KronozaDate(2024, 2, 29)))

        val sundayGrid = KronozaDate.buildMonthGrid(2024, 2, startMonday = false)
        assertEquals(42, sundayGrid.size)
        assertEquals(7, sundayGrid.first().dayOfWeek)
        assertEquals(KronozaDate(2024, 1, 28), sundayGrid.first())
    }

    @Test
    fun timezoneConversion_and_reminderTriggerComputation_areTimezoneAware() {
        val date = KronozaDate(2026, 11, 23)
        val zones = listOf(
            TimeZone.getTimeZone("UTC"),
            TimeZone.getTimeZone("America/Los_Angeles"),
            TimeZone.getTimeZone("Asia/Tokyo"),
            TimeZone.getTimeZone("Asia/Kolkata")
        )

        zones.forEach { tz ->
            val millis = date.epochMillisAt(minutesFromMidnight = 9 * 60 + 45, timeZone = tz)
            val (decodedDate, decodedMinutes) = KronozaDate.fromEpochMillis(millis, tz)
            assertEquals("Date mismatch for ${tz.id}", date, decodedDate)
            assertEquals("Minutes mismatch for ${tz.id}", 9 * 60 + 45, decodedMinutes)
        }

        val utcTrigger = KronozaReminderScheduler.computeTriggerAtMillis(
            dueEpochDay = date.epochDay,
            dueMinutes = 10 * 60,
            minutesBefore = 15,
            timeZone = TimeZone.getTimeZone("UTC")
        )
        val tokyoTrigger = KronozaReminderScheduler.computeTriggerAtMillis(
            dueEpochDay = date.epochDay,
            dueMinutes = 10 * 60,
            minutesBefore = 15,
            timeZone = TimeZone.getTimeZone("Asia/Tokyo")
        )
        // Tokyo is UTC+9, so 09:45 in Tokyo happens 9 hours earlier in UTC epoch millis
        assertEquals(9L * 60L * 60L * 1000L, utcTrigger - tokyoTrigger)
    }

    @Test
    fun taskOverdue_filtering_and_sorting_workAccurately() {
        val today = KronozaDate(2026, 11, 23)
        val todayDay = today.epochDay
        val nowMins = 12 * 60

        val pastOverdue = TaskEntity(
            id = 1L,
            title = "Past due task",
            dueEpochDay = todayDay - 1,
            dueMinutes = 17 * 60,
            priority = TaskPriority.HIGH,
            category = TaskCategory.WORK,
            completed = false,
            createdAtMillis = 100L
        )
        val earlierTodayOverdue = TaskEntity(
            id = 2L,
            title = "Morning overdue task",
            dueEpochDay = todayDay,
            dueMinutes = 9 * 60,
            priority = TaskPriority.LOW,
            category = TaskCategory.DESIGN,
            completed = false,
            createdAtMillis = 200L
        )
        val upcomingToday = TaskEntity(
            id = 3L,
            title = "Afternoon focus task",
            dueEpochDay = todayDay,
            dueMinutes = 15 * 60,
            priority = TaskPriority.HIGH,
            category = TaskCategory.DESIGN,
            completed = false,
            createdAtMillis = 300L
        )
        val completedPast = TaskEntity(
            id = 4L,
            title = "Already finished",
            dueEpochDay = todayDay - 2,
            dueMinutes = 10 * 60,
            priority = TaskPriority.HIGH,
            category = TaskCategory.PERSONAL,
            completed = true,
            createdAtMillis = 400L
        )
        val allTasks = listOf(pastOverdue, earlierTodayOverdue, upcomingToday, completedPast)

        assertTrue(TaskFilterSort.isOverdue(pastOverdue, todayDay, nowMins))
        assertTrue(TaskFilterSort.isOverdue(earlierTodayOverdue, todayDay, nowMins))
        assertFalse(TaskFilterSort.isOverdue(upcomingToday, todayDay, nowMins))
        assertFalse(TaskFilterSort.isOverdue(completedPast, todayDay, nowMins))

        assertEquals(
            listOf(1L, 2L),
            TaskFilterSort.filterTasks(allTasks, TaskFilter.OVERDUE, null, todayDay, nowMins).map { it.id }
        )
        assertEquals(
            listOf(2L, 3L),
            TaskFilterSort.filterTasks(allTasks, TaskFilter.TODAY, null, todayDay, nowMins).map { it.id }
        )
        assertEquals(
            listOf(3L),
            TaskFilterSort.filterTasks(allTasks, TaskFilter.TODAY, TaskCategory.DESIGN, todayDay, nowMins)
                .filterNot { TaskFilterSort.isOverdue(it, todayDay, nowMins) }
                .map { it.id }
        )
        assertEquals(
            listOf(1L, 3L),
            TaskFilterSort.filterTasks(allTasks, TaskFilter.HIGH_PRIORITY, null, todayDay, nowMins).map { it.id }
        )
        assertEquals(
            listOf(4L),
            TaskFilterSort.filterTasks(allTasks, TaskFilter.COMPLETED, null, todayDay, nowMins).map { it.id }
        )

        // Sorting checks
        assertEquals(
            listOf(4L, 1L, 2L, 3L),
            TaskFilterSort.sortTasks(allTasks, TaskSortOption.DUE_DATE).map { it.id }
        )
        assertEquals(
            listOf(4L, 1L, 3L, 2L),
            TaskFilterSort.sortTasks(allTasks, TaskSortOption.PRIORITY).map { it.id }
        )
        assertEquals(
            listOf(3L, 4L, 2L, 1L),
            TaskFilterSort.sortTasks(allTasks, TaskSortOption.TITLE).map { it.id }
        )
        assertEquals(
            listOf(4L, 3L, 2L, 1L),
            TaskFilterSort.sortTasks(allTasks, TaskSortOption.CREATED).map { it.id }
        )
    }

    @Test
    fun searchFilters_applyQueryDateScopeAndPriority() {
        val today = KronozaDate(2026, 11, 23)
        val todayDay = today.epochDay
        val weekStart = today.startOfWeek(true).epochDay
        val weekEnd = weekStart + 6

        val events = listOf(
            EventEntity(
                id = 10L,
                title = "Roadmap Sync",
                dateEpochDay = todayDay,
                startMinutes = 600,
                endMinutes = 660,
                category = EventCategory.WORK,
                location = "Studio B"
            ),
            EventEntity(
                id = 11L,
                title = "Morning Run",
                dateEpochDay = todayDay + 2,
                startMinutes = 420,
                endMinutes = 480,
                category = EventCategory.PERSONAL,
                location = "Park"
            )
        )
        val tasks = listOf(
            TaskEntity(
                id = 20L,
                title = "Finalize Roadmap Brief",
                dueEpochDay = todayDay - 1,
                dueMinutes = 900,
                priority = TaskPriority.HIGH,
                category = TaskCategory.PRODUCT
            ),
            TaskEntity(
                id = 21L,
                title = "Book Dentist",
                dueEpochDay = todayDay + 1,
                dueMinutes = 700,
                priority = TaskPriority.LOW,
                category = TaskCategory.PERSONAL
            )
        )

        val matchedEvents = TaskFilterSort.filterSearchEvents(
            events = events,
            query = "roadmap",
            scopeFilter = SearchFilter.WORK,
            dateFilter = DateFilterOption.TODAY,
            todayEpochDay = todayDay,
            weekStartEpochDay = weekStart,
            weekEndEpochDay = weekEnd
        )
        assertEquals(listOf(10L), matchedEvents.map { it.id })

        val matchedOverdueTasks = TaskFilterSort.filterSearchTasks(
            tasks = tasks,
            query = "roadmap",
            scopeFilter = SearchFilter.ALL,
            dateFilter = DateFilterOption.OVERDUE,
            priorityFilter = PriorityFilterOption.HIGH,
            todayEpochDay = todayDay,
            nowMinutes = 12 * 60,
            weekStartEpochDay = weekStart,
            weekEndEpochDay = weekEnd
        )
        assertEquals(listOf(20L), matchedOverdueTasks.map { it.id })
    }

    @Test
    fun backupCodec_roundTripPreservesData_and_rejectsTampering() {
        val snapshot = KronozaBackupSnapshot(
            exportedAtMillis = 1_763_900_000_000L,
            preferences = UserPreferences(
                userName = "Elena \"E.\" Rostova",
                userEmail = "elena@kronoza.app",
                themeMode = ThemeMode.DARK,
                accentPreset = AccentPreset.SKY_BLUE,
                defaultCalendarView = CalendarViewMode.AGENDA,
                startWeekMonday = false,
                notificationSounds = false,
                dailyFocusGoal = 6,
                onboardingCompleted = true
            ),
            events = listOf(
                EventEntity(
                    id = 42L,
                    title = "Q4 Planning & Review",
                    dateEpochDay = 20780L,
                    startMinutes = 570,
                    endMinutes = 630,
                    allDay = false,
                    repeatFrequency = RepeatFrequency.WEEKLY,
                    location = "Room \"A\"\nFloor 3",
                    meetingUrl = "https://meet.example.com/abc",
                    category = EventCategory.DEEP_WORK,
                    reminder = ReminderOption.THIRTY_MINS,
                    notes = "Line 1\nLine 2\tTabbed • Unicode ✓",
                    badgeLabel = "Milestone",
                    isMilestone = true,
                    attendees = listOf(AttendeeItem("Maya Lin", "Lead", true, "maya")),
                    checklist = listOf(AgendaCheckItem("c1", "Confirm launch date", true)),
                    createdAtMillis = 123456789L
                )
            ),
            tasks = listOf(
                TaskEntity(
                    id = 99L,
                    title = "Verify backup integrity",
                    description = "Ensure SHA-256 passes",
                    dueEpochDay = 20781L,
                    dueMinutes = 840,
                    priority = TaskPriority.HIGH,
                    category = TaskCategory.ENGINEERING,
                    completed = false,
                    reminderEnabled = true,
                    subtasks = listOf(SubtaskItem("s1", "Run unit tests", true)),
                    createdAtMillis = 987654321L
                )
            ),
            notifications = listOf(
                NotificationEntity(
                    id = 7L,
                    title = "Reminder active",
                    message = "Test alert",
                    type = NotificationType.EVENT,
                    isToday = true,
                    timeAgoLabel = "Just now",
                    metaChipLabel = "15m",
                    unread = true,
                    linkedEventId = 42L,
                    inviteStatus = "",
                    inviteDateEpochDay = null,
                    inviteStartMinutes = null,
                    inviteEndMinutes = null,
                    createdAtMillis = 111222333L
                )
            ),
            reminders = listOf(
                ReminderConfigEntity(
                    id = 3L,
                    targetType = ReminderTargetType.EVENT,
                    targetId = 42L,
                    title = "Q4 Planning & Review",
                    dueEpochDay = 20780L,
                    dueMinutes = 570,
                    minutesBefore = 30,
                    triggerAtMillis = 1_763_950_000_000L,
                    timeZoneId = "America/Los_Angeles",
                    enabled = true,
                    fired = false,
                    useExactAlarm = true,
                    updatedAtMillis = 555666777L
                )
            )
        )

        val encoded = KronozaBackupCodec.encode(snapshot)
        val restored = KronozaBackupCodec.decodeAndVerify(encoded)

        assertEquals(snapshot.schemaVersion, restored.schemaVersion)
        assertEquals(snapshot.exportedAtMillis, restored.exportedAtMillis)
        assertEquals(snapshot.preferences, restored.preferences)
        assertEquals(snapshot.events, restored.events)
        assertEquals(snapshot.tasks, restored.tasks)
        assertEquals(snapshot.notifications, restored.notifications)
        assertEquals(snapshot.reminders, restored.reminders)

        // Tamper with payload and verify checksum catches it
        val tampered = encoded.replace("Verify backup integrity", "Tampered task title")
        try {
            KronozaBackupCodec.decodeAndVerify(tampered)
            fail("Expected checksum mismatch exception for tampered payload")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message?.contains("checksum", ignoreCase = true) == true)
        }

        // Unsupported schema version check
        val wrongVersion = encoded.replace("\"schemaVersion\":1", "\"schemaVersion\":99")
        try {
            KronozaBackupCodec.decodeAndVerify(wrongVersion)
            fail("Expected schema version rejection")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message?.contains("version", ignoreCase = true) == true)
        }
    }
}
