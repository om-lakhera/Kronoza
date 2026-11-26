package com.omlakhera.kronoza.data.local

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.omlakhera.kronoza.model.AccentPreset
import com.omlakhera.kronoza.model.CalendarViewMode
import com.omlakhera.kronoza.model.DateFormatOption
import com.omlakhera.kronoza.model.EventCategory
import com.omlakhera.kronoza.model.ReminderOption
import com.omlakhera.kronoza.model.ThemeMode
import com.omlakhera.kronoza.model.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kronoza_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val PROFILE_PHOTO_PATH = stringPreferencesKey("profile_photo_path")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_PRESET = stringPreferencesKey("accent_preset")
        val DEFAULT_CALENDAR_VIEW = stringPreferencesKey("default_calendar_view")
        val START_WEEK_MONDAY = booleanPreferencesKey("start_week_monday")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val TIME_FORMAT_24H = booleanPreferencesKey("time_format_24h")
        val DEFAULT_EVENT_DURATION = intPreferencesKey("default_event_duration")
        val DEFAULT_REMINDER = stringPreferencesKey("default_reminder")
        val EVENT_REMINDERS_ENABLED = booleanPreferencesKey("event_reminders_enabled")
        val TASK_REMINDERS_ENABLED = booleanPreferencesKey("task_reminders_enabled")
        val NOTIFICATION_SOUNDS = booleanPreferencesKey("notification_sounds")
        val DAILY_FOCUS_GOAL = intPreferencesKey("daily_focus_goal")
        val PREFERRED_CATEGORIES = stringPreferencesKey("preferred_categories")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val rawCategories = prefs[Keys.PREFERRED_CATEGORIES]
        val categories = if (rawCategories.isNullOrBlank()) {
            EventCategory.entries.toSet()
        } else {
            rawCategories.split(",")
                .mapNotNull { name -> enumValues<EventCategory>().firstOrNull { it.name == name } }
                .toSet()
                .ifEmpty { EventCategory.entries.toSet() }
        }
        UserPreferences(
            userName = prefs[Keys.USER_NAME] ?: "",
            userEmail = prefs[Keys.USER_EMAIL] ?: "",
            profilePhotoPath = prefs[Keys.PROFILE_PHOTO_PATH] ?: "",
            themeMode = enumValues<ThemeMode>().firstOrNull { it.name == prefs[Keys.THEME_MODE] }
                ?: ThemeMode.LIGHT,
            accentPreset = enumValues<AccentPreset>().firstOrNull { it.name == prefs[Keys.ACCENT_PRESET] }
                ?: AccentPreset.LAVENDER,
            defaultCalendarView = enumValues<CalendarViewMode>()
                .firstOrNull { it.name == prefs[Keys.DEFAULT_CALENDAR_VIEW] }
                ?: CalendarViewMode.MONTH,
            startWeekMonday = prefs[Keys.START_WEEK_MONDAY] ?: true,
            dateFormat = enumValues<DateFormatOption>()
                .firstOrNull { it.name == prefs[Keys.DATE_FORMAT] }
                ?: DateFormatOption.MMM_D_YYYY,
            timeFormat24h = prefs[Keys.TIME_FORMAT_24H] ?: false,
            defaultEventDurationMinutes = prefs[Keys.DEFAULT_EVENT_DURATION] ?: 60,
            defaultReminder = enumValues<ReminderOption>()
                .firstOrNull { it.name == prefs[Keys.DEFAULT_REMINDER] }
                ?: ReminderOption.FIFTEEN_MINS,
            eventRemindersEnabled = prefs[Keys.EVENT_REMINDERS_ENABLED] ?: true,
            taskRemindersEnabled = prefs[Keys.TASK_REMINDERS_ENABLED] ?: true,
            notificationSounds = prefs[Keys.NOTIFICATION_SOUNDS] ?: true,
            dailyFocusGoal = prefs[Keys.DAILY_FOCUS_GOAL] ?: 4,
            preferredCategories = categories,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun getPreferencesOnce(): UserPreferences = preferencesFlow.first()

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAccentPreset(preset: AccentPreset) {
        context.dataStore.edit { it[Keys.ACCENT_PRESET] = preset.name }
    }

    suspend fun setDefaultCalendarView(viewMode: CalendarViewMode) {
        context.dataStore.edit { it[Keys.DEFAULT_CALENDAR_VIEW] = viewMode.name }
    }

    suspend fun setStartWeekMonday(startMonday: Boolean) {
        context.dataStore.edit { it[Keys.START_WEEK_MONDAY] = startMonday }
    }

    suspend fun setDateFormat(format: DateFormatOption) {
        context.dataStore.edit { it[Keys.DATE_FORMAT] = format.name }
    }

    suspend fun setTimeFormat24h(use24h: Boolean) {
        context.dataStore.edit { it[Keys.TIME_FORMAT_24H] = use24h }
    }

    suspend fun setDefaultEventDurationMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_EVENT_DURATION] = minutes.coerceIn(15, 240) }
    }

    suspend fun setDefaultReminder(option: ReminderOption) {
        context.dataStore.edit { it[Keys.DEFAULT_REMINDER] = option.name }
    }

    suspend fun setEventRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.EVENT_REMINDERS_ENABLED] = enabled }
    }

    suspend fun setTaskRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TASK_REMINDERS_ENABLED] = enabled }
    }

    suspend fun setNotificationSounds(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATION_SOUNDS] = enabled }
    }

    suspend fun setDailyFocusGoal(goal: Int) {
        context.dataStore.edit { it[Keys.DAILY_FOCUS_GOAL] = goal.coerceIn(1, 8) }
    }

    suspend fun setPreferredCategories(categories: Set<EventCategory>) {
        val safe = categories.ifEmpty { EventCategory.entries.toSet() }
        context.dataStore.edit {
            it[Keys.PREFERRED_CATEGORIES] = safe.joinToString(",") { cat -> cat.name }
        }
    }

    suspend fun updateProfile(name: String, email: String) {
        context.dataStore.edit {
            it[Keys.USER_NAME] = name.trim()
            it[Keys.USER_EMAIL] = email.trim()
        }
    }

    suspend fun saveProfilePhotoFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        val destFile = File(context.filesDir, "kronoza_profile_avatar.jpg")
        val maxBytes = 10L * 1024L * 1024L
        var totalCopied = 0L
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                val buffer = ByteArray(8192)
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    totalCopied += read
                    if (totalCopied > maxBytes) {
                        throw IllegalArgumentException("Selected image exceeds maximum supported size.")
                    }
                    output.write(buffer, 0, read)
                }
            }
        }
        val path = if (destFile.exists() && destFile.length() > 0L) destFile.absolutePath else ""
        context.dataStore.edit { it[Keys.PROFILE_PHOTO_PATH] = path }
        path
    }

    suspend fun clearProfilePhoto() = withContext(Dispatchers.IO) {
        val destFile = File(context.filesDir, "kronoza_profile_avatar.jpg")
        if (destFile.exists()) destFile.delete()
        context.dataStore.edit { it[Keys.PROFILE_PHOTO_PATH] = "" }
    }

    suspend fun restorePreferences(prefs: UserPreferences) {
        context.dataStore.edit {
            it[Keys.USER_NAME] = prefs.userName
            it[Keys.USER_EMAIL] = prefs.userEmail
            it[Keys.THEME_MODE] = prefs.themeMode.name
            it[Keys.ACCENT_PRESET] = prefs.accentPreset.name
            it[Keys.DEFAULT_CALENDAR_VIEW] = prefs.defaultCalendarView.name
            it[Keys.START_WEEK_MONDAY] = prefs.startWeekMonday
            it[Keys.DATE_FORMAT] = prefs.dateFormat.name
            it[Keys.TIME_FORMAT_24H] = prefs.timeFormat24h
            it[Keys.DEFAULT_EVENT_DURATION] = prefs.defaultEventDurationMinutes.coerceIn(15, 240)
            it[Keys.DEFAULT_REMINDER] = prefs.defaultReminder.name
            it[Keys.EVENT_REMINDERS_ENABLED] = prefs.eventRemindersEnabled
            it[Keys.TASK_REMINDERS_ENABLED] = prefs.taskRemindersEnabled
            it[Keys.NOTIFICATION_SOUNDS] = prefs.notificationSounds
            it[Keys.DAILY_FOCUS_GOAL] = prefs.dailyFocusGoal.coerceIn(1, 8)
            it[Keys.PREFERRED_CATEGORIES] = prefs.preferredCategories
                .ifEmpty { EventCategory.entries.toSet() }
                .joinToString(",") { cat -> cat.name }
            it[Keys.ONBOARDING_COMPLETED] = prefs.onboardingCompleted
        }
    }
}
