package com.omlakhera.kronoza.ui.navigation

object KronozaRoutes {
    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val TASKS = "tasks"
    const val INSIGHTS = "insights"
    const val SETTINGS = "settings"

    const val EVENT_DETAILS = "event_details/{eventId}?occ={occ}"
    const val EVENT_EDITOR = "event_editor?eventId={eventId}&date={date}&hour={hour}&occ={occ}"
    const val TASK_EDITOR = "task_editor?taskId={taskId}&date={date}"
    const val SEARCH = "search"
    const val NOTIFICATIONS = "notifications"

    const val ABOUT_HUB = "about_hub"
    const val ABOUT_APP = "about_app"
    const val DEVELOPER_PROFILE = "developer_profile"
    const val TECH_STACK = "tech_stack"
    const val OPEN_SOURCE_LICENSES = "open_source_licenses"
    const val SOURCE_CONTRIBUTIONS = "source_contributions"
    const val PRIVACY_INFO = "privacy_info"

    fun eventDetails(eventId: Long, occurrenceEpochDay: Long? = null): String {
        val occArg = occurrenceEpochDay ?: -1L
        return "event_details/$eventId?occ=$occArg"
    }

    fun eventEditor(
        eventId: Long? = null,
        dateEpochDay: Long? = null,
        hour: Int? = null,
        occurrenceEpochDay: Long? = null
    ): String {
        val idArg = eventId ?: -1L
        val dateArg = dateEpochDay ?: -1L
        val hourArg = hour ?: -1
        val occArg = occurrenceEpochDay ?: -1L
        return "event_editor?eventId=$idArg&date=$dateArg&hour=$hourArg&occ=$occArg"
    }

    fun taskEditor(taskId: Long? = null, dateEpochDay: Long? = null): String {
        val idArg = taskId ?: -1L
        val dateArg = dateEpochDay ?: -1L
        return "task_editor?taskId=$idArg&date=$dateArg"
    }
}
