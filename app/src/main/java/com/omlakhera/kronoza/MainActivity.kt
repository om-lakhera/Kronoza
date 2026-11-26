package com.omlakhera.kronoza

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.omlakhera.kronoza.data.KronozaRepository
import com.omlakhera.kronoza.data.backup.BackupResult
import com.omlakhera.kronoza.data.local.EventEntity
import com.omlakhera.kronoza.data.local.TaskEntity
import com.omlakhera.kronoza.model.BackupRestoreMode
import com.omlakhera.kronoza.model.CalendarViewMode
import com.omlakhera.kronoza.reminders.KronozaNotificationHelper
import com.omlakhera.kronoza.ui.components.KronozaBottomBar
import com.omlakhera.kronoza.ui.components.KronozaFab
import com.omlakhera.kronoza.ui.components.KronozaMotion
import com.omlakhera.kronoza.ui.components.KronozaNavRail
import com.omlakhera.kronoza.ui.components.KronozaTopAppBar
import com.omlakhera.kronoza.ui.components.LocalReducedMotion
import com.omlakhera.kronoza.ui.components.ProvideKronozaMotion
import com.omlakhera.kronoza.ui.components.QuickCreateSheet
import com.omlakhera.kronoza.ui.navigation.KronozaRoutes
import com.omlakhera.kronoza.ui.screens.AboutCreditsHubScreen
import com.omlakhera.kronoza.ui.screens.AboutKronozaScreen
import com.omlakhera.kronoza.ui.screens.AgendaCalendarScreen
import com.omlakhera.kronoza.ui.screens.DailyAgendaScreen
import com.omlakhera.kronoza.ui.screens.DeveloperProfileScreen
import com.omlakhera.kronoza.ui.screens.EventDetailsScreen
import com.omlakhera.kronoza.ui.screens.EventEditorScreen
import com.omlakhera.kronoza.ui.screens.HomeDashboardScreen
import com.omlakhera.kronoza.ui.screens.MonthlyCalendarScreen
import com.omlakhera.kronoza.ui.screens.NotificationsScreen
import com.omlakhera.kronoza.ui.screens.OnboardingFlowRoot
import com.omlakhera.kronoza.ui.screens.OpenSourceLicensesScreen
import com.omlakhera.kronoza.ui.screens.PrivacyInformationScreen
import com.omlakhera.kronoza.ui.screens.ProductivityInsightsScreen
import com.omlakhera.kronoza.ui.screens.SearchFilterScreen
import com.omlakhera.kronoza.ui.screens.SettingsScreen
import com.omlakhera.kronoza.ui.screens.SourceContributionsScreen
import com.omlakhera.kronoza.ui.screens.TaskEditorScreen
import com.omlakhera.kronoza.ui.screens.TaskManagementScreen
import com.omlakhera.kronoza.ui.screens.TechnologyStackScreen
import com.omlakhera.kronoza.ui.screens.WeeklyCalendarScreen
import com.omlakhera.kronoza.ui.theme.KronozaTheme
import com.omlakhera.kronoza.ui.viewmodel.KronozaViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KronozaAppRoot()
        }
    }
}

@Composable
fun KronozaAppRoot() {
    val context = LocalContext.current
    val viewModel: KronozaViewModel = viewModel(
        factory = remember(context) {
            KronozaViewModel.Factory(KronozaRepository(context.applicationContext))
        }
    )

    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val prefsLoaded by viewModel.prefsLoaded.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val displayedMonth by viewModel.displayedMonth.collectAsStateWithLifecycle()
    val calendarViewMode by viewModel.calendarViewMode.collectAsStateWithLifecycle()
    val insightsPeriod by viewModel.insightsPeriod.collectAsStateWithLifecycle()
    val insightsSummary by viewModel.insightsSummary.collectAsStateWithLifecycle()
    val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()

    KronozaTheme(
        themeMode = preferences.themeMode,
        accentPreset = preferences.accentPreset
    ) {
        ProvideKronozaMotion {
            if (!prefsLoaded) {
                Box(modifier = Modifier.fillMaxSize())
                return@ProvideKronozaMotion
            }

            if (!preferences.onboardingCompleted) {
                OnboardingFlowRoot(
                    initialPreferences = preferences,
                    canScheduleExactAlarms = viewModel::canScheduleExactAlarms,
                    onUpdateProfile = viewModel::updateProfile,
                    onSaveProfilePhoto = { uri -> viewModel.saveProfilePhoto(uri) },
                    onSetPreferredCategories = viewModel::setPreferredCategories,
                    onSetStartWeekMonday = viewModel::setStartWeekMonday,
                    onSetDefaultCalendarView = viewModel::setDefaultCalendarView,
                    onSetThemeMode = viewModel::setThemeMode,
                    onCompleteOnboarding = {
                        viewModel.completeOnboarding()
                        viewModel.rescheduleReminders()
                    }
                )
                return@ProvideKronozaMotion
            }

            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination?.route ?: KronozaRoutes.HOME
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            val reducedMotion = LocalReducedMotion.current
            var showQuickCreate by remember { mutableStateOf(false) }
            var pendingRestoreMode by remember { mutableStateOf(BackupRestoreMode.MERGE_SKIP_DUPLICATES) }

            var permissionRefreshTick by remember { mutableIntStateOf(0) }
            val notifGranted = remember(permissionRefreshTick) {
                KronozaNotificationHelper.hasNotificationPermission(context)
            }
            val exactAlarmsGranted = remember(permissionRefreshTick) {
                viewModel.canScheduleExactAlarms()
            }

            val notifPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                permissionRefreshTick++
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (granted) "Notifications enabled" else "Notifications denied — reminders will appear in-app"
                    )
                }
            }

            val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) {
                permissionRefreshTick++
                viewModel.rescheduleReminders()
            }

            val exportBackupLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                if (uri != null) {
                    viewModel.exportBackup(uri) { result ->
                        scope.launch {
                            val msg = when (result) {
                                is BackupResult.ExportSuccess ->
                                    "Backed up ${result.eventCount} events, ${result.taskCount} tasks, ${result.reminderCount} reminders"
                                is BackupResult.Failure -> result.message
                                else -> "Backup exported"
                            }
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                }
            }

            val restoreBackupLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    viewModel.restoreBackup(uri, pendingRestoreMode) { result ->
                        scope.launch {
                            val msg = when (result) {
                                is BackupResult.RestoreSuccess -> {
                                    val skippedSuffix = if (result.skippedDuplicates > 0) {
                                        " (${result.skippedDuplicates} duplicates skipped)"
                                    } else {
                                        ""
                                    }
                                    "Restored ${result.eventCount} events, ${result.taskCount} tasks & ${result.reminderCount} reminders$skippedSuffix"
                                }
                                is BackupResult.Failure -> result.message
                                else -> "Backup restored"
                            }
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                }
            }

            val topLevelRoutes = remember {
                setOf(
                    KronozaRoutes.HOME,
                    KronozaRoutes.CALENDAR,
                    KronozaRoutes.TASKS,
                    KronozaRoutes.INSIGHTS,
                    KronozaRoutes.SETTINGS
                )
            }
            val isTopLevel = currentDestination in topLevelRoutes
            val isEditorScreen = currentDestination.startsWith("event_editor") ||
                currentDestination.startsWith("task_editor")

            val topBarTitle = when {
                currentDestination == KronozaRoutes.HOME -> "Home"
                currentDestination == KronozaRoutes.CALENDAR -> when (calendarViewMode) {
                    CalendarViewMode.MONTH -> "Monthly Calendar"
                    CalendarViewMode.WEEK -> "Weekly Calendar"
                    CalendarViewMode.DAY -> "Daily Agenda"
                    CalendarViewMode.AGENDA -> "Agenda"
                }
                currentDestination == KronozaRoutes.TASKS -> "Tasks"
                currentDestination == KronozaRoutes.INSIGHTS -> "Insights"
                currentDestination == KronozaRoutes.SETTINGS -> "Settings"
                currentDestination.startsWith("event_details") -> "Event Details"
                currentDestination.startsWith("event_editor") -> "Event Editor"
                currentDestination.startsWith("task_editor") -> "Task Editor"
                currentDestination == KronozaRoutes.SEARCH -> "Search & Filter"
                currentDestination == KronozaRoutes.NOTIFICATIONS -> "Notifications"
                currentDestination == KronozaRoutes.ABOUT_HUB -> "About & Credits"
                currentDestination == KronozaRoutes.ABOUT_APP -> "About Kronoza"
                currentDestination == KronozaRoutes.DEVELOPER_PROFILE -> "Developer Profile"
                currentDestination == KronozaRoutes.TECH_STACK -> "Technology Stack"
                currentDestination == KronozaRoutes.OPEN_SOURCE_LICENSES -> "Open Source Licenses"
                currentDestination == KronozaRoutes.SOURCE_CONTRIBUTIONS -> "Source & Contributions"
                currentDestination == KronozaRoutes.PRIVACY_INFO -> "Privacy"
                else -> "Kronoza"
            }

            val navigateTopLevel: (String) -> Unit = { route ->
                navController.navigate(route) {
                    popUpTo(KronozaRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            if (showQuickCreate) {
                QuickCreateSheet(
                    onDismiss = { showQuickCreate = false },
                    onCreateEvent = {
                        navController.navigate(KronozaRoutes.eventEditor(dateEpochDay = selectedDate.epochDay))
                    },
                    onCreateTask = {
                        navController.navigate(KronozaRoutes.taskEditor(dateEpochDay = selectedDate.epochDay))
                    }
                )
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavRail = maxWidth >= 600.dp

                Row(modifier = Modifier.fillMaxSize()) {
                    if (useNavRail && isTopLevel) {
                        KronozaNavRail(
                            currentRoute = currentDestination,
                            onNavigate = navigateTopLevel
                        )
                    }

                    Scaffold(
                        modifier = Modifier.weight(1f),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        topBar = {
                            KronozaTopAppBar(
                                title = topBarTitle,
                                profilePhotoPath = preferences.profilePhotoPath,
                                userName = preferences.userName,
                                showBack = !isTopLevel,
                                showActions = isTopLevel,
                                unreadNotifications = unreadCount,
                                onBackClick = { navController.popBackStack() },
                                onSearchClick = { navController.navigate(KronozaRoutes.SEARCH) },
                                onNotificationsClick = { navController.navigate(KronozaRoutes.NOTIFICATIONS) },
                                onAvatarClick = {
                                    if (currentDestination != KronozaRoutes.SETTINGS) {
                                        navigateTopLevel(KronozaRoutes.SETTINGS)
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            if (!useNavRail && isTopLevel) {
                                KronozaBottomBar(
                                    currentRoute = currentDestination,
                                    onNavigate = navigateTopLevel
                                )
                            }
                        },
                        floatingActionButton = {
                            if (isTopLevel && !isEditorScreen) {
                                KronozaFab(
                                    onClick = {
                                        if (currentDestination == KronozaRoutes.TASKS) {
                                            navController.navigate(
                                                KronozaRoutes.taskEditor(dateEpochDay = selectedDate.epochDay)
                                            )
                                        } else {
                                            showQuickCreate = true
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = KronozaRoutes.HOME,
                                enterTransition = { KronozaMotion.navEnter(reducedMotion) },
                                exitTransition = { KronozaMotion.navExit(reducedMotion) },
                                popEnterTransition = { KronozaMotion.navPopEnter(reducedMotion) },
                                popExitTransition = { KronozaMotion.navPopExit(reducedMotion) }
                            ) {
                                composable(KronozaRoutes.HOME) {
                                    HomeDashboardScreen(
                                        events = events,
                                        tasks = tasks,
                                        selectedDate = selectedDate,
                                        displayedMonth = displayedMonth,
                                        preferences = preferences,
                                        onSelectDate = viewModel::selectDate,
                                        onShiftMonth = viewModel::shiftMonth,
                                        onOpenFullDay = {
                                            viewModel.setCalendarViewMode(CalendarViewMode.DAY)
                                            navigateTopLevel(KronozaRoutes.CALENDAR)
                                        },
                                        onOpenEventDetails = { id, occ ->
                                            navController.navigate(KronozaRoutes.eventDetails(id, occ))
                                        },
                                        onCreateEventForDate = { date ->
                                            navController.navigate(
                                                KronozaRoutes.eventEditor(dateEpochDay = date.epochDay)
                                            )
                                        },
                                        onToggleTask = viewModel::toggleTaskCompleted,
                                        onEditTask = { id ->
                                            navController.navigate(KronozaRoutes.taskEditor(taskId = id))
                                        },
                                        onOpenTasksTab = {
                                            navigateTopLevel(KronozaRoutes.TASKS)
                                        }
                                    )
                                }

                                composable(KronozaRoutes.CALENDAR) {
                                    when (calendarViewMode) {
                                        CalendarViewMode.MONTH -> MonthlyCalendarScreen(
                                            events = events,
                                            tasks = tasks,
                                            selectedDate = selectedDate,
                                            displayedMonth = displayedMonth,
                                            preferences = preferences,
                                            onSelectDate = viewModel::selectDate,
                                            onJumpToToday = viewModel::jumpToToday,
                                            onShiftMonth = viewModel::shiftMonth,
                                            onChangeViewMode = viewModel::setCalendarViewMode,
                                            onOpenEventDetails = { id, occ ->
                                                navController.navigate(KronozaRoutes.eventDetails(id, occ))
                                            },
                                            onToggleTask = viewModel::toggleTaskCompleted,
                                            onEditTask = { id ->
                                                navController.navigate(KronozaRoutes.taskEditor(taskId = id))
                                            },
                                            onCreateEventForDate = { date ->
                                                navController.navigate(
                                                    KronozaRoutes.eventEditor(dateEpochDay = date.epochDay)
                                                )
                                            }
                                        )
                                        CalendarViewMode.WEEK -> WeeklyCalendarScreen(
                                            events = events,
                                            selectedDate = selectedDate,
                                            preferences = preferences,
                                            onSelectDate = viewModel::selectDate,
                                            onChangeViewMode = viewModel::setCalendarViewMode,
                                            onOpenEventDetails = { id, occ ->
                                                navController.navigate(KronozaRoutes.eventDetails(id, occ))
                                            },
                                            onCreateEventAtSlot = { date, hour ->
                                                navController.navigate(
                                                    KronozaRoutes.eventEditor(
                                                        dateEpochDay = date.epochDay,
                                                        hour = hour
                                                    )
                                                )
                                            }
                                        )
                                        CalendarViewMode.DAY -> DailyAgendaScreen(
                                            events = events,
                                            tasks = tasks,
                                            selectedDate = selectedDate,
                                            preferences = preferences,
                                            onSelectDate = viewModel::selectDate,
                                            onChangeViewMode = viewModel::setCalendarViewMode,
                                            onOpenEventDetails = { id, occ ->
                                                navController.navigate(KronozaRoutes.eventDetails(id, occ))
                                            },
                                            onToggleTask = viewModel::toggleTaskCompleted,
                                            onEditTask = { id ->
                                                navController.navigate(KronozaRoutes.taskEditor(taskId = id))
                                            },
                                            onCreateEventAtHour = { date, hour ->
                                                navController.navigate(
                                                    KronozaRoutes.eventEditor(
                                                        dateEpochDay = date.epochDay,
                                                        hour = hour
                                                    )
                                                )
                                            }
                                        )
                                        CalendarViewMode.AGENDA -> AgendaCalendarScreen(
                                            events = events,
                                            tasks = tasks,
                                            selectedDate = selectedDate,
                                            preferences = preferences,
                                            onJumpToToday = viewModel::jumpToToday,
                                            onChangeViewMode = viewModel::setCalendarViewMode,
                                            onOpenEventDetails = { id, occ ->
                                                navController.navigate(KronozaRoutes.eventDetails(id, occ))
                                            },
                                            onToggleTask = viewModel::toggleTaskCompleted,
                                            onEditTask = { id ->
                                                navController.navigate(KronozaRoutes.taskEditor(taskId = id))
                                            },
                                            onCreateEventForDate = { date ->
                                                navController.navigate(
                                                    KronozaRoutes.eventEditor(dateEpochDay = date.epochDay)
                                                )
                                            }
                                        )
                                    }
                                }

                                composable(KronozaRoutes.TASKS) {
                                    TaskManagementScreen(
                                        tasks = tasks,
                                        preferences = preferences,
                                        onToggleCompleted = viewModel::toggleTaskCompleted,
                                        onEditTask = { taskId ->
                                            navController.navigate(KronozaRoutes.taskEditor(taskId = taskId))
                                        }
                                    )
                                }

                                composable(KronozaRoutes.INSIGHTS) {
                                    ProductivityInsightsScreen(
                                        summary = insightsSummary,
                                        selectedPeriod = insightsPeriod,
                                        onSelectPeriod = viewModel::setInsightsPeriod
                                    )
                                }

                                composable(KronozaRoutes.SETTINGS) {
                                    SettingsScreen(
                                        preferences = preferences,
                                        storageStats = storageStats,
                                        notificationPermissionGranted = notifGranted,
                                        exactAlarmsPermitted = exactAlarmsGranted,
                                        activeReminderCount = reminders.count { it.enabled && !it.fired },
                                        onSetThemeMode = viewModel::setThemeMode,
                                        onSetAccentPreset = viewModel::setAccentPreset,
                                        onSetDefaultCalendarView = viewModel::setDefaultCalendarView,
                                        onSetStartWeekMonday = viewModel::setStartWeekMonday,
                                        onSetDateFormat = viewModel::setDateFormat,
                                        onSetTimeFormat24h = viewModel::setTimeFormat24h,
                                        onSetDefaultEventDurationMinutes = viewModel::setDefaultEventDurationMinutes,
                                        onSetDefaultReminder = viewModel::setDefaultReminder,
                                        onSetEventRemindersEnabled = viewModel::setEventRemindersEnabled,
                                        onSetTaskRemindersEnabled = viewModel::setTaskRemindersEnabled,
                                        onSetNotificationSounds = viewModel::setNotificationSounds,
                                        onSetDailyFocusGoal = viewModel::setDailyFocusGoal,
                                        onUpdateProfile = viewModel::updateProfile,
                                        onPickProfilePhoto = { uri ->
                                            viewModel.saveProfilePhoto(uri) { path ->
                                                if (path.isNotBlank()) {
                                                    scope.launch { snackbarHostState.showSnackbar("Profile photo updated") }
                                                }
                                            }
                                        },
                                        onClearProfilePhoto = {
                                            viewModel.clearProfilePhoto()
                                            scope.launch { snackbarHostState.showSnackbar("Profile photo removed") }
                                        },
                                        onRequestNotificationPermission = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            } else {
                                                permissionRefreshTick++
                                            }
                                        },
                                        onRequestExactAlarmPermission = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                    data = Uri.parse("package:${context.packageName}")
                                                }
                                                exactAlarmSettingsLauncher.launch(intent)
                                            }
                                        },
                                        onExportBackup = {
                                            exportBackupLauncher.launch("kronoza_backup.json")
                                        },
                                        onRestoreBackup = { mode ->
                                            pendingRestoreMode = mode
                                            restoreBackupLauncher.launch(arrayOf("application/json", "*/*"))
                                        },
                                        onClearAllLocalData = {
                                            viewModel.clearAllLocalData {
                                                scope.launch { snackbarHostState.showSnackbar("Local data cleared") }
                                            }
                                        },
                                        onReplayOnboarding = viewModel::replayOnboarding,
                                        onOpenAboutHub = { navController.navigate(KronozaRoutes.ABOUT_HUB) },
                                        onOpenAboutApp = { navController.navigate(KronozaRoutes.ABOUT_APP) },
                                        onOpenDeveloperProfile = { navController.navigate(KronozaRoutes.DEVELOPER_PROFILE) },
                                        onOpenTechStack = { navController.navigate(KronozaRoutes.TECH_STACK) },
                                        onOpenLicenses = { navController.navigate(KronozaRoutes.OPEN_SOURCE_LICENSES) },
                                        onOpenSourceContributions = { navController.navigate(KronozaRoutes.SOURCE_CONTRIBUTIONS) },
                                        onOpenPrivacyInfo = { navController.navigate(KronozaRoutes.PRIVACY_INFO) }
                                    )
                                }

                                composable(
                                    route = KronozaRoutes.EVENT_DETAILS,
                                    arguments = listOf(
                                        navArgument("eventId") { type = NavType.LongType },
                                        navArgument("occ") { type = NavType.LongType; defaultValue = -1L }
                                    )
                                ) { entry ->
                                    val eventId = entry.arguments?.getLong("eventId") ?: -1L
                                    val occArg = entry.arguments?.getLong("occ") ?: -1L
                                    val occurrenceEpochDay = if (occArg > 0L) occArg else null
                                    val event by viewModel.observeEvent(eventId)
                                        .collectAsStateWithLifecycle(initialValue = null)
                                    EventDetailsScreen(
                                        event = event,
                                        occurrenceEpochDay = occurrenceEpochDay,
                                        preferences = preferences,
                                        snackbarHostState = snackbarHostState,
                                        onEditEvent = { id, occ ->
                                            navController.navigate(
                                                KronozaRoutes.eventEditor(
                                                    eventId = id,
                                                    occurrenceEpochDay = occ
                                                )
                                            )
                                        },
                                        onDuplicateEvent = { id ->
                                            viewModel.duplicateEvent(id) { newId ->
                                                if (newId != null) {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Event duplicated")
                                                    }
                                                }
                                            }
                                        },
                                        onRescheduleEvent = { id, dateDay, startMins, endMins ->
                                            viewModel.rescheduleEvent(id, dateDay, startMins, endMins) { ok ->
                                                if (ok) {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Event rescheduled")
                                                    }
                                                }
                                            }
                                        },
                                        onDeleteEventWithScope = { ev, occ, scopeChoice ->
                                            viewModel.deleteEventWithScope(ev, occ, scopeChoice) {
                                                navController.popBackStack()
                                                scope.launch { snackbarHostState.showSnackbar("Event deleted") }
                                            }
                                        },
                                        onToggleChecklistItem = viewModel::toggleEventChecklistItem
                                    )
                                }

                                composable(
                                    route = KronozaRoutes.EVENT_EDITOR,
                                    arguments = listOf(
                                        navArgument("eventId") { type = NavType.LongType; defaultValue = -1L },
                                        navArgument("date") { type = NavType.LongType; defaultValue = -1L },
                                        navArgument("hour") { type = NavType.IntType; defaultValue = -1 },
                                        navArgument("occ") { type = NavType.LongType; defaultValue = -1L }
                                    )
                                ) { entry ->
                                    val eventId = entry.arguments?.getLong("eventId") ?: -1L
                                    val dateArg = entry.arguments?.getLong("date") ?: -1L
                                    val hourArg = entry.arguments?.getInt("hour") ?: -1
                                    val occArg = entry.arguments?.getLong("occ") ?: -1L
                                    var existingEvent by remember { mutableStateOf<EventEntity?>(null) }

                                    LaunchedEffect(eventId) {
                                        if (eventId > 0L) {
                                            existingEvent = viewModel.getEvent(eventId)
                                        }
                                    }

                                    EventEditorScreen(
                                        existingEvent = existingEvent,
                                        existingEvents = events,
                                        initialDateEpochDay = if (dateArg > 0L) dateArg else null,
                                        initialHour = if (hourArg >= 0) hourArg else null,
                                        occurrenceEpochDay = if (occArg > 0L) occArg else null,
                                        preferences = preferences,
                                        onCancel = { navController.popBackStack() },
                                        onSave = { entity ->
                                            viewModel.saveEvent(entity) {
                                                navController.popBackStack()
                                                scope.launch { snackbarHostState.showSnackbar("Event saved") }
                                            }
                                        },
                                        onSaveWithScope = { orig, occ, edited, scopeChoice ->
                                            viewModel.saveEventWithScope(orig, occ, edited, scopeChoice) {
                                                navController.popBackStack()
                                                scope.launch { snackbarHostState.showSnackbar("Event updated") }
                                            }
                                        }
                                    )
                                }

                                composable(
                                    route = KronozaRoutes.TASK_EDITOR,
                                    arguments = listOf(
                                        navArgument("taskId") { type = NavType.LongType; defaultValue = -1L },
                                        navArgument("date") { type = NavType.LongType; defaultValue = -1L }
                                    )
                                ) { entry ->
                                    val taskId = entry.arguments?.getLong("taskId") ?: -1L
                                    val dateArg = entry.arguments?.getLong("date") ?: -1L
                                    var existingTask by remember { mutableStateOf<TaskEntity?>(null) }

                                    LaunchedEffect(taskId) {
                                        if (taskId > 0L) {
                                            existingTask = viewModel.getTask(taskId)
                                        }
                                    }

                                    TaskEditorScreen(
                                        existingTask = existingTask,
                                        initialDateEpochDay = if (dateArg > 0L) dateArg else null,
                                        preferences = preferences,
                                        onCancel = { navController.popBackStack() },
                                        onSave = { entity ->
                                            viewModel.saveTask(entity) {
                                                navController.popBackStack()
                                                scope.launch { snackbarHostState.showSnackbar("Task saved") }
                                            }
                                        },
                                        onDelete = { id ->
                                            viewModel.deleteTask(id) {
                                                navController.popBackStack()
                                                scope.launch { snackbarHostState.showSnackbar("Task deleted") }
                                            }
                                        }
                                    )
                                }

                                composable(KronozaRoutes.SEARCH) {
                                    SearchFilterScreen(
                                        events = events,
                                        tasks = tasks,
                                        preferences = preferences,
                                        onOpenEventDetails = { id, occ ->
                                            navController.navigate(KronozaRoutes.eventDetails(id, occ))
                                        },
                                        onToggleTask = viewModel::toggleTaskCompleted,
                                        onEditTask = { id ->
                                            navController.navigate(KronozaRoutes.taskEditor(taskId = id))
                                        }
                                    )
                                }

                                composable(KronozaRoutes.NOTIFICATIONS) {
                                    NotificationsScreen(
                                        notifications = notifications,
                                        notificationPermissionGranted = notifGranted,
                                        eventRemindersEnabled = preferences.eventRemindersEnabled,
                                        taskRemindersEnabled = preferences.taskRemindersEnabled,
                                        onMarkRead = viewModel::markNotificationRead,
                                        onMarkAllRead = viewModel::markAllNotificationsRead,
                                        onRespondInvite = { notif, accept ->
                                            viewModel.respondToInvite(notif, accept)
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (accept) "Invite accepted & added to calendar" else "Invite declined"
                                                )
                                            }
                                        },
                                        onOpenLinkedEvent = { id ->
                                            navController.navigate(KronozaRoutes.eventDetails(id))
                                        },
                                        onOpenSettings = {
                                            navigateTopLevel(KronozaRoutes.SETTINGS)
                                        }
                                    )
                                }

                                composable(KronozaRoutes.ABOUT_HUB) {
                                    AboutCreditsHubScreen(
                                        onOpenAboutApp = { navController.navigate(KronozaRoutes.ABOUT_APP) },
                                        onOpenDeveloperProfile = { navController.navigate(KronozaRoutes.DEVELOPER_PROFILE) },
                                        onOpenTechStack = { navController.navigate(KronozaRoutes.TECH_STACK) },
                                        onOpenLicenses = { navController.navigate(KronozaRoutes.OPEN_SOURCE_LICENSES) },
                                        onOpenSourceContributions = { navController.navigate(KronozaRoutes.SOURCE_CONTRIBUTIONS) },
                                        onOpenPrivacy = { navController.navigate(KronozaRoutes.PRIVACY_INFO) },
                                        onShowMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                                    )
                                }

                                composable(KronozaRoutes.ABOUT_APP) {
                                    AboutKronozaScreen(
                                        onOpenDeveloperProfile = { navController.navigate(KronozaRoutes.DEVELOPER_PROFILE) },
                                        onOpenLicenses = { navController.navigate(KronozaRoutes.OPEN_SOURCE_LICENSES) },
                                        onOpenPrivacy = { navController.navigate(KronozaRoutes.PRIVACY_INFO) }
                                    )
                                }

                                composable(KronozaRoutes.DEVELOPER_PROFILE) {
                                    DeveloperProfileScreen(
                                        onOpenTechStack = { navController.navigate(KronozaRoutes.TECH_STACK) },
                                        onOpenLicenses = { navController.navigate(KronozaRoutes.OPEN_SOURCE_LICENSES) },
                                        onShowMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                                    )
                                }

                                composable(KronozaRoutes.TECH_STACK) {
                                    TechnologyStackScreen()
                                }

                                composable(KronozaRoutes.OPEN_SOURCE_LICENSES) {
                                    OpenSourceLicensesScreen()
                                }

                                composable(KronozaRoutes.SOURCE_CONTRIBUTIONS) {
                                    SourceContributionsScreen(
                                        onShowMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                                    )
                                }

                                composable(KronozaRoutes.PRIVACY_INFO) {
                                    PrivacyInformationScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
