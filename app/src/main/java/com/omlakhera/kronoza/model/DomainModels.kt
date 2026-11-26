package com.omlakhera.kronoza.model

enum class EventCategory(
    val displayName: String,
    val colorName: String,
    val insightLabel: String
) {
    WORK("Work", "Lavender", "Meetings"),
    DEEP_WORK("Deep Work", "Sky Blue", "Deep Work"),
    PERSONAL("Personal", "Mint", "Wellness"),
    DEADLINE("Health", "Coral", "Deadlines")
}

enum class EventColorOption(
    val label: String,
    val hex: String?
) {
    AUTO("Category", null),
    LAVENDER("Lavender", "#5368E8"),
    SKY_BLUE("Sky Blue", "#1D6FD6"),
    MINT("Mint", "#117A58"),
    CORAL("Coral", "#D14343"),
    AMBER("Amber", "#D97706"),
    PLUM("Plum", "#7C3AED")
}

enum class RepeatFrequency(val label: String) {
    NONE("Does not repeat"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

enum class ReminderOption(val label: String, val minutesBefore: Int) {
    NONE("No reminder", -1),
    FIVE_MINS("5 mins before", 5),
    FIFTEEN_MINS("15 mins before", 15),
    THIRTY_MINS("30 mins before", 30),
    ONE_HOUR("1 hour before", 60)
}

enum class ReminderTargetType {
    EVENT,
    TASK
}

enum class TaskPriority(val label: String, val sortWeight: Int) {
    HIGH("High", 0),
    MEDIUM("Medium", 1),
    LOW("Low", 2)
}

enum class TaskCategory(val label: String) {
    DESIGN("Design"),
    WORK("Work"),
    PERSONAL("Personal"),
    PRODUCT("Product"),
    ENGINEERING("Engineering")
}

enum class CalendarViewMode(val label: String) {
    MONTH("Month"),
    WEEK("Week"),
    DAY("Day"),
    AGENDA("Agenda")
}

enum class ThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}

enum class AccentPreset(val label: String) {
    LAVENDER("Lavender"),
    SKY_BLUE("Sky Blue"),
    MINT("Mint"),
    CORAL("Coral")
}

enum class DateFormatOption(val label: String, val example: String) {
    MMM_D_YYYY("MMM d, yyyy", "Oct 14, 2026"),
    DD_MM_YYYY("dd/MM/yyyy", "14/10/2026"),
    MM_DD_YYYY("MM/dd/yyyy", "10/14/2026")
}

enum class NotificationType(val label: String) {
    EVENT("Events"),
    TASK("Tasks"),
    INVITE("Invites"),
    INSIGHT("Insights")
}

enum class TaskFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    OVERDUE("Overdue"),
    HIGH_PRIORITY("High Priority"),
    COMPLETED("Completed")
}

enum class TaskSortOption(val label: String) {
    DUE_DATE("Due Date"),
    PRIORITY("Priority"),
    TITLE("Title"),
    CREATED("Recently Modified")
}

enum class SearchFilter(val label: String) {
    ALL("All"),
    EVENTS("Events"),
    TASKS("Tasks"),
    WORK("Work"),
    PERSONAL("Personal")
}

enum class DateFilterOption(val label: String) {
    ALL("Any Date"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    UPCOMING("Upcoming"),
    OVERDUE("Overdue")
}

enum class PriorityFilterOption(val label: String) {
    ALL("All Priorities"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

enum class SearchCompletionFilter(val label: String) {
    ALL("All Status"),
    ACTIVE("Active"),
    COMPLETED("Completed")
}

enum class SearchEventTypeFilter(val label: String) {
    ALL("All Types"),
    TIMED("Timed"),
    ALL_DAY("All-day"),
    RECURRING("Recurring"),
    VIDEO_CALL("Video Call")
}

enum class SearchSortOption(val label: String) {
    DATE("Date"),
    RECENTLY_MODIFIED("Recently Modified"),
    PRIORITY("Priority"),
    ALPHABETICAL("Alphabetical")
}

enum class RecurrenceEditScope {
    THIS_OCCURRENCE,
    ENTIRE_SERIES
}

enum class BackupRestoreMode(val label: String, val description: String) {
    MERGE_SKIP_DUPLICATES(
        "Merge (Keep Existing)",
        "Add imported items while skipping duplicates already in Kronoza."
    ),
    REPLACE_ALL(
        "Replace Everything",
        "Replace current events, tasks, reminders, and settings with the backup."
    )
}

enum class InsightsPeriod(val label: String) {
    WEEK("This Week"),
    MONTH("This Month")
}

data class SubtaskItem(
    val id: String,
    val title: String,
    val done: Boolean = false
)

data class AgendaCheckItem(
    val id: String,
    val text: String,
    val done: Boolean = false
)

data class AttendeeItem(
    val name: String,
    val role: String,
    val accepted: Boolean = true,
    val avatarKey: String = ""
)

data class UserPreferences(
    val userName: String = "",
    val userEmail: String = "",
    val profilePhotoPath: String = "",
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val accentPreset: AccentPreset = AccentPreset.LAVENDER,
    val defaultCalendarView: CalendarViewMode = CalendarViewMode.MONTH,
    val startWeekMonday: Boolean = true,
    val dateFormat: DateFormatOption = DateFormatOption.MMM_D_YYYY,
    val timeFormat24h: Boolean = false,
    val defaultEventDurationMinutes: Int = 60,
    val defaultReminder: ReminderOption = ReminderOption.FIFTEEN_MINS,
    val eventRemindersEnabled: Boolean = true,
    val taskRemindersEnabled: Boolean = true,
    val notificationSounds: Boolean = true,
    val dailyFocusGoal: Int = 4,
    val preferredCategories: Set<EventCategory> = EventCategory.entries.toSet(),
    val onboardingCompleted: Boolean = false
)

data class StorageStats(
    val eventCount: Int = 0,
    val taskCount: Int = 0,
    val notificationCount: Int = 0,
    val reminderCount: Int = 0,
    val databaseBytes: Long = 0L
)

data class InsightsSummary(
    val focusHours: Double,
    val focusDeltaPercent: Int,
    val tasksDone: Int,
    val pendingTasks: Int,
    val overdueTasks: Int,
    val completionRatePercent: Int,
    val eventsCount: Int,
    val meetingHours: Double,
    val streakDays: Int,
    val bestStreakDays: Int,
    val dailyBars: List<DailyHoursPoint>,
    val categoryBreakdown: List<CategoryShare>,
    val weeklyTrendPoints: List<WeeklyTrendPoint>,
    val hasActivityData: Boolean,
    val highlightTitle: String,
    val highlightBody: String
)

data class DailyHoursPoint(
    val dayLabel: String,
    val focusHours: Float,
    val meetingHours: Float,
    val isPeak: Boolean
)

data class CategoryShare(
    val label: String,
    val hours: Double,
    val percent: Int,
    val category: EventCategory
)

data class WeeklyTrendPoint(
    val weekLabel: String,
    val scheduledHours: Float,
    val completedTasks: Int
)
