package com.omlakhera.kronoza.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.omlakhera.kronoza.model.AccentPreset
import com.omlakhera.kronoza.model.BackupRestoreMode
import com.omlakhera.kronoza.model.CalendarViewMode
import com.omlakhera.kronoza.model.DateFormatOption
import com.omlakhera.kronoza.model.KronozaAboutCatalog
import com.omlakhera.kronoza.model.ReminderOption
import com.omlakhera.kronoza.model.StorageStats
import com.omlakhera.kronoza.model.ThemeMode
import com.omlakhera.kronoza.model.UserPreferences
import com.omlakhera.kronoza.ui.components.KronozaCard
import com.omlakhera.kronoza.ui.components.KronozaIcon
import com.omlakhera.kronoza.ui.components.KronozaIcons
import com.omlakhera.kronoza.ui.components.KronozaLogoMark
import com.omlakhera.kronoza.ui.components.KronozaLogoVariationsSheetContent
import com.omlakhera.kronoza.ui.components.KronozaProfileAvatar
import com.omlakhera.kronoza.ui.components.LogoVariation
import com.omlakhera.kronoza.ui.theme.CoralVibrant
import com.omlakhera.kronoza.ui.theme.LavenderContainer
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors
import com.omlakhera.kronoza.ui.theme.MintContainerBright
import com.omlakhera.kronoza.ui.theme.SkyBlueContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    storageStats: StorageStats,
    notificationPermissionGranted: Boolean,
    exactAlarmsPermitted: Boolean,
    activeReminderCount: Int,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetAccentPreset: (AccentPreset) -> Unit,
    onSetDefaultCalendarView: (CalendarViewMode) -> Unit,
    onSetStartWeekMonday: (Boolean) -> Unit,
    onSetDateFormat: (DateFormatOption) -> Unit,
    onSetTimeFormat24h: (Boolean) -> Unit,
    onSetDefaultEventDurationMinutes: (Int) -> Unit,
    onSetDefaultReminder: (ReminderOption) -> Unit,
    onSetEventRemindersEnabled: (Boolean) -> Unit,
    onSetTaskRemindersEnabled: (Boolean) -> Unit,
    onSetNotificationSounds: (Boolean) -> Unit,
    onSetDailyFocusGoal: (Int) -> Unit,
    onUpdateProfile: (String, String) -> Unit,
    onPickProfilePhoto: (Uri) -> Unit,
    onClearProfilePhoto: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onRequestExactAlarmPermission: () -> Unit,
    onExportBackup: () -> Unit,
    onRestoreBackup: (BackupRestoreMode) -> Unit,
    onClearAllLocalData: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onOpenAboutHub: () -> Unit,
    onOpenAboutApp: () -> Unit,
    onOpenDeveloperProfile: () -> Unit,
    onOpenTechStack: () -> Unit,
    onOpenLicenses: () -> Unit,
    onOpenSourceContributions: () -> Unit,
    onOpenPrivacyInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val dev = KronozaAboutCatalog.developer
    var showProfileDialog by remember { mutableStateOf(false) }
    var showViewDialog by remember { mutableStateOf(false) }
    var showDateFormatDialog by remember { mutableStateOf(false) }
    var showDurationDialog by remember { mutableStateOf(false) }
    var showDefaultReminderDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showBrandSheet by remember { mutableStateOf(false) }
    var showRestoreModeDialog by remember { mutableStateOf(false) }
    var selectedRestoreMode by remember { mutableStateOf(BackupRestoreMode.MERGE_SKIP_DUPLICATES) }
    var showClearDataConfirm by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPickProfilePhoto(uri)
        }
    }

    if (showBrandSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBrandSheet = false },
            containerColor = ext.card,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            KronozaLogoVariationsSheetContent()
        }
    }

    if (showRestoreModeDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreModeDialog = false },
            title = { Text("Restore Backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Kronoza verifies the SHA-256 checksum before importing. Choose how to apply restored records:",
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.textSecondary
                    )
                    BackupRestoreMode.entries.forEach { mode ->
                        val selected = mode == selectedRestoreMode
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) ext.lavenderContainer.copy(alpha = 0.7f) else ext.mutedSurface,
                            border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedRestoreMode = mode }
                                .testTag("restore_mode_${mode.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = ext.textPrimary
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = mode.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ext.textSecondary
                                    )
                                }
                                if (selected) {
                                    Spacer(Modifier.width(8.dp))
                                    KronozaIcon(
                                        imageVector = KronozaIcons.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        size = 18.dp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreModeDialog = false
                        onRestoreBackup(selectedRestoreMode)
                    }
                ) { Text("Choose File") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreModeDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            title = { Text("Clear All Local Data?") },
            text = {
                Text("This permanently deletes all local events, tasks, reminders, and notifications on this device. Your profile settings remain intact. Export a backup first if needed.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataConfirm = false
                        onClearAllLocalData()
                    }
                ) {
                    Text("Delete All", color = ext.coralAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showProfileDialog) {
        var nameInput by remember(preferences.userName) { mutableStateOf(preferences.userName) }
        var emailInput by remember(preferences.userEmail) { mutableStateOf(preferences.userEmail) }
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KronozaProfileAvatar(
                            photoPath = preferences.profilePhotoPath,
                            contentDescription = preferences.userName.ifBlank { "Profile avatar" },
                            userName = preferences.userName,
                            size = 56.dp
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Camera,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 16.dp
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Change Photo")
                            }
                            if (preferences.profilePhotoPath.isNotBlank()) {
                                TextButton(onClick = onClearProfilePhoto) {
                                    Text("Remove Photo", color = ext.coralAccent)
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateProfile(nameInput, emailInput)
                        showProfileDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showViewDialog) {
        AlertDialog(
            onDismissRequest = { showViewDialog = false },
            title = { Text("Default Calendar View") },
            text = {
                Column {
                    CalendarViewMode.entries.forEach { mode ->
                        SelectionDialogRow(
                            title = mode.label,
                            selected = mode == preferences.defaultCalendarView,
                            onClick = {
                                onSetDefaultCalendarView(mode)
                                showViewDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showViewDialog = false }) { Text("Close") }
            }
        )
    }

    if (showDateFormatDialog) {
        AlertDialog(
            onDismissRequest = { showDateFormatDialog = false },
            title = { Text("Date Format") },
            text = {
                Column {
                    DateFormatOption.entries.forEach { option ->
                        SelectionDialogRow(
                            title = option.example,
                            subtitle = option.label,
                            selected = option == preferences.dateFormat,
                            onClick = {
                                onSetDateFormat(option)
                                showDateFormatDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDateFormatDialog = false }) { Text("Close") }
            }
        )
    }

    if (showDurationDialog) {
        AlertDialog(
            onDismissRequest = { showDurationDialog = false },
            title = { Text("Default Event Duration") },
            text = {
                Column {
                    listOf(30, 45, 60, 90, 120).forEach { mins ->
                        val label = if (mins >= 60 && mins % 60 == 0) {
                            "${mins / 60} ${if (mins == 60) "hour" else "hours"}"
                        } else {
                            "$mins minutes"
                        }
                        SelectionDialogRow(
                            title = label,
                            selected = mins == preferences.defaultEventDurationMinutes,
                            onClick = {
                                onSetDefaultEventDurationMinutes(mins)
                                showDurationDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDurationDialog = false }) { Text("Close") }
            }
        )
    }

    if (showDefaultReminderDialog) {
        AlertDialog(
            onDismissRequest = { showDefaultReminderDialog = false },
            title = { Text("Default Reminder Time") },
            text = {
                Column {
                    ReminderOption.entries.forEach { option ->
                        SelectionDialogRow(
                            title = option.label,
                            selected = option == preferences.defaultReminder,
                            onClick = {
                                onSetDefaultReminder(option)
                                showDefaultReminderDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDefaultReminderDialog = false }) { Text("Close") }
            }
        )
    }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Daily Focus Goal") },
            text = {
                Column {
                    listOf(2, 3, 4, 5, 6).forEach { blocks ->
                        SelectionDialogRow(
                            title = "$blocks focus blocks / day",
                            selected = blocks == preferences.dailyFocusGoal,
                            onClick = {
                                onSetDailyFocusGoal(blocks)
                                showGoalDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text("Close") }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ext.canvas),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. GENERAL ─────────────────────────────────────────────────────
            item {
                SettingsSectionHeader(title = "General")
            }

            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("settings_avatar_picker")
                        ) {
                            KronozaProfileAvatar(
                                photoPath = preferences.profilePhotoPath,
                                contentDescription = preferences.userName.ifBlank { "Profile avatar" },
                                userName = preferences.userName,
                                size = 58.dp
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(1.5.dp, ext.card, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Camera,
                                    contentDescription = "Change profile photo",
                                    tint = Color.White,
                                    size = 11.dp
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = preferences.userName.ifBlank { "Local Workspace" },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = ext.textPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = ext.lavenderContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        KronozaIcon(
                                            imageVector = KronozaIcons.Shield,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            size = 12.dp
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Offline",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ext.onLavenderContainer
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = preferences.userEmail.ifBlank { "Tap edit to personalize optional profile" },
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.textSecondary
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = ext.mutedSurface,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .clickable { showProfileDialog = true }
                                .testTag("settings_edit_profile")
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Edit,
                                    contentDescription = "Edit profile",
                                    tint = ext.textPrimary,
                                    size = 18.dp
                                )
                            }
                        }
                    }
                }
            }

            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        PreferenceRow(
                            icon = KronozaIcons.Palette,
                            iconBg = ext.lavenderContainer,
                            iconTint = ext.lavenderAccent,
                            title = "Kronoza Brand Variations",
                            subtitle = "Wordmark, icon & theme lockups",
                            trailingText = "View 5 logos",
                            onClick = { showBrandSheet = true },
                            testTag = "settings_brand_variations"
                        )
                        PreferenceRow(
                            icon = KronozaIcons.Replay,
                            iconBg = ext.coralContainer,
                            iconTint = ext.coralAccent,
                            title = "Replay Welcome & Setup",
                            subtitle = "Preview first-time onboarding flow",
                            trailingText = "Restart FTUE",
                            onClick = onReplayOnboarding,
                            testTag = "settings_replay_onboarding"
                        )
                    }
                }
            }

            // ── 2. APPEARANCE ──────────────────────────────────────────────────
            item {
                SettingsSectionHeader(
                    title = "Appearance",
                    trailingLabel = "${preferences.themeMode.label} • ${preferences.accentPreset.label}"
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemePreviewOption(
                        mode = ThemeMode.LIGHT,
                        selected = preferences.themeMode == ThemeMode.LIGHT,
                        onClick = { onSetThemeMode(ThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemePreviewOption(
                        mode = ThemeMode.DARK,
                        selected = preferences.themeMode == ThemeMode.DARK,
                        onClick = { onSetThemeMode(ThemeMode.DARK) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemePreviewOption(
                        mode = ThemeMode.SYSTEM,
                        selected = preferences.themeMode == ThemeMode.SYSTEM,
                        onClick = { onSetThemeMode(ThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Accent Color",
                                style = MaterialTheme.typography.titleMedium,
                                color = ext.textPrimary
                            )
                            Text(
                                text = "Personalize highlights & badges",
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.textSecondary
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val swatches = listOf(
                                AccentPreset.LAVENDER to LavenderContainer,
                                AccentPreset.SKY_BLUE to SkyBlueContainer,
                                AccentPreset.MINT to MintContainerBright,
                                AccentPreset.CORAL to CoralVibrant.copy(alpha = 0.25f)
                            )
                            swatches.forEach { (preset, swatchBg) ->
                                val isSelected = preset == preferences.accentPreset
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(swatchBg)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else ext.cardBorder,
                                            shape = CircleShape
                                        )
                                        .clickable { onSetAccentPreset(preset) }
                                        .testTag("settings_accent_${preset.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        KronozaIcon(
                                            imageVector = KronozaIcons.Check,
                                            contentDescription = preset.label,
                                            tint = MaterialTheme.colorScheme.primary,
                                            size = 18.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 3. CALENDAR PREFERENCES ────────────────────────────────────────
            item {
                SettingsSectionHeader(title = "Calendar Preferences")
            }

            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        PreferenceRow(
                            icon = KronozaIcons.Grid,
                            iconBg = ext.lavenderContainer,
                            iconTint = ext.lavenderAccent,
                            title = "Default Calendar View",
                            subtitle = "Opening calendar tab layout",
                            trailingText = preferences.defaultCalendarView.label,
                            onClick = { showViewDialog = true },
                            testTag = "settings_default_view"
                        )
                        PreferenceRow(
                            icon = KronozaIcons.CalendarOutline,
                            iconBg = ext.skyBlueContainer,
                            iconTint = ext.skyBlueAccent,
                            title = "Start of Week",
                            subtitle = "First column in grids",
                            trailingText = if (preferences.startWeekMonday) "Monday" else "Sunday",
                            onClick = { onSetStartWeekMonday(!preferences.startWeekMonday) },
                            testTag = "settings_start_week"
                        )
                        PreferenceRow(
                            icon = KronozaIcons.Sliders,
                            iconBg = ext.mintContainer,
                            iconTint = ext.mintAccent,
                            title = "Date Format",
                            subtitle = "Displayed across agendas & tasks",
                            trailingText = preferences.dateFormat.example,
                            onClick = { showDateFormatDialog = true },
                            testTag = "settings_date_format"
                        )
                        PreferenceSwitchRow(
                            icon = KronozaIcons.Clock,
                            iconBg = ext.skyBlueContainer,
                            iconTint = ext.skyBlueAccent,
                            title = "24-Hour Clock",
                            subtitle = if (preferences.timeFormat24h) "Using 14:30 format" else "Using 2:30 PM format",
                            checked = preferences.timeFormat24h,
                            onCheckedChange = onSetTimeFormat24h,
                            testTag = "settings_time_24h_switch"
                        )
                        PreferenceRow(
                            icon = KronozaIcons.Clock,
                            iconBg = ext.lavenderContainer,
                            iconTint = ext.lavenderAccent,
                            title = "Default Event Duration",
                            subtitle = "Initial length for new events",
                            trailingText = "${preferences.defaultEventDurationMinutes}m",
                            onClick = { showDurationDialog = true },
                            testTag = "settings_default_duration"
                        )
                        PreferenceRow(
                            icon = KronozaIcons.Bolt,
                            iconBg = ext.lavenderContainer,
                            iconTint = ext.lavenderAccent,
                            title = "Daily Focus Goal",
                            subtitle = "Target deep-work blocks",
                            trailingText = "${preferences.dailyFocusGoal} / day",
                            onClick = { showGoalDialog = true },
                            testTag = "settings_focus_goal"
                        )
                    }
                }
            }

            // ── 4. NOTIFICATIONS ───────────────────────────────────────────────
            item {
                SettingsSectionHeader(title = "Notifications")
            }

            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        PreferenceRow(
                            icon = KronozaIcons.Bell,
                            iconBg = ext.lavenderContainer,
                            iconTint = ext.lavenderAccent,
                            title = "Default Reminder",
                            subtitle = "Lead time for new events",
                            trailingText = preferences.defaultReminder.label,
                            onClick = { showDefaultReminderDialog = true },
                            testTag = "settings_default_reminder"
                        )
                        PreferenceSwitchRow(
                            icon = KronozaIcons.CalendarOutline,
                            iconBg = ext.skyBlueContainer,
                            iconTint = ext.skyBlueAccent,
                            title = "Event Reminders",
                            subtitle = "Schedule native alerts for events",
                            checked = preferences.eventRemindersEnabled,
                            onCheckedChange = onSetEventRemindersEnabled,
                            testTag = "settings_event_reminders_switch"
                        )
                        PreferenceSwitchRow(
                            icon = KronozaIcons.TasksOutline,
                            iconBg = ext.mintContainer,
                            iconTint = ext.mintAccent,
                            title = "Task Reminders",
                            subtitle = "Schedule native alerts for tasks",
                            checked = preferences.taskRemindersEnabled,
                            onCheckedChange = onSetTaskRemindersEnabled,
                            testTag = "settings_task_reminders_switch"
                        )
                        PreferenceSwitchRow(
                            icon = KronozaIcons.Bell,
                            iconBg = ext.mintContainer,
                            iconTint = ext.mintAccent,
                            title = "Notification Sounds",
                            subtitle = "Gentle chimes for reminders",
                            checked = preferences.notificationSounds,
                            onCheckedChange = onSetNotificationSounds,
                            testTag = "settings_sounds_switch"
                        )
                        PreferenceRow(
                            icon = KronozaIcons.Bell,
                            iconBg = if (notificationPermissionGranted) ext.mintContainer else ext.coralContainer,
                            iconTint = if (notificationPermissionGranted) ext.mintAccent else ext.coralAccent,
                            title = "Notification Permission",
                            subtitle = if (notificationPermissionGranted) "Ready to deliver alerts" else "Tap to allow reminder alerts",
                            trailingText = if (notificationPermissionGranted) "Granted" else "Allow",
                            onClick = onRequestNotificationPermission,
                            testTag = "settings_notification_permission"
                        )
                        PreferenceRow(
                            icon = KronozaIcons.Clock,
                            iconBg = if (exactAlarmsPermitted) ext.skyBlueContainer else ext.lavenderContainer,
                            iconTint = if (exactAlarmsPermitted) ext.skyBlueAccent else ext.lavenderAccent,
                            title = "Exact Alarm Scheduling",
                            subtitle = if (exactAlarmsPermitted) "AlarmManager precision enabled" else "Using WorkManager fallback",
                            trailingText = if (exactAlarmsPermitted) "Exact" else "Configure",
                            onClick = onRequestExactAlarmPermission,
                            testTag = "settings_exact_alarm_permission"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ext.lavenderContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Check,
                                    contentDescription = "Active reminder status",
                                    tint = ext.lavenderAccent,
                                    size = 20.dp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Scheduled Reminders",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ext.textPrimary
                                )
                                Text(
                                    text = "Persisted across reboots & timezone changes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.textSecondary
                                )
                            }
                            Text(
                                text = "$activeReminderCount active",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // ── 5. DATA & BACKUP ───────────────────────────────────────────────
            item {
                SettingsSectionHeader(title = "Data & Backup")
            }

            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        PreferenceRow(
                            icon = KronozaIcons.BackupExport,
                            iconBg = ext.skyBlueContainer,
                            iconTint = ext.skyBlueAccent,
                            title = "Export Local Backup",
                            subtitle = "Save SHA-256 verified JSON file",
                            trailingText = "Export",
                            onClick = onExportBackup,
                            testTag = "settings_export_backup"
                        )
                        PreferenceRow(
                            icon = KronozaIcons.BackupImport,
                            iconBg = ext.mintContainer,
                            iconTint = ext.mintAccent,
                            title = "Restore from Backup",
                            subtitle = "Merge or replace with integrity verification",
                            trailingText = "Import",
                            onClick = { showRestoreModeDialog = true },
                            testTag = "settings_restore_backup"
                        )
                        PreferenceRow(
                            icon = KronozaIcons.Trash,
                            iconBg = ext.coralContainer,
                            iconTint = ext.coralAccent,
                            title = "Clear All Local Data",
                            subtitle = "Remove all events, tasks & notifications",
                            trailingText = "Clear",
                            trailingColor = ext.coralAccent,
                            onClick = { showClearDataConfirm = true },
                            testTag = "settings_clear_data"
                        )
                    }
                }
            }

            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ext.lavenderContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    KronozaIcon(
                                        imageVector = KronozaIcons.Database,
                                        contentDescription = null,
                                        tint = ext.lavenderAccent,
                                        size = 18.dp
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Local Storage Footprint",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = ext.textPrimary
                                    )
                                    Text(
                                        text = "Stored privately in on-device Room SQLite",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ext.textSecondary
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = ext.mutedSurface
                            ) {
                                Text(
                                    text = formatBytes(storageStats.databaseBytes),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ext.textPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StorageStatChip("Events", storageStats.eventCount, Modifier.weight(1f))
                            StorageStatChip("Tasks", storageStats.taskCount, Modifier.weight(1f))
                            StorageStatChip("Reminders", storageStats.reminderCount, Modifier.weight(1f))
                            StorageStatChip("Alerts", storageStats.notificationCount, Modifier.weight(1f))
                        }
                    }
                }
            }

            // ── 6. PRIVACY ─────────────────────────────────────────────────────
            item {
                SettingsSectionHeader(title = "Privacy")
            }

            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        PreferenceRow(
                            icon = KronozaIcons.Lock,
                            iconBg = ext.mintContainer,
                            iconTint = ext.mintAccent,
                            title = "Privacy Information",
                            subtitle = "100% offline-first storage with zero tracking",
                            trailingText = "Details",
                            onClick = onOpenPrivacyInfo,
                            testTag = "settings_privacy_info"
                        )
                    }
                }
            }

            // ── 7. DEVELOPER & OPEN SOURCE ─────────────────────────────────────
            item {
                SettingsSectionHeader(title = "Developer & Open Source")
            }

            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onOpenDeveloperProfile)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .testTag("settings_developer_profile"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DeveloperAvatar(developer = dev, size = 40.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dev.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ext.textPrimary
                                )
                                Text(
                                    text = "${dev.role} • @${dev.githubHandle}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.textSecondary
                                )
                            }
                            Text(
                                text = "Profile",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            KronozaIcon(
                                imageVector = KronozaIcons.ChevronRight,
                                contentDescription = null,
                                tint = ext.textSecondary,
                                size = 16.dp
                            )
                        }
                        PreferenceRow(
                            icon = KronozaIcons.Layers,
                            iconBg = ext.skyBlueContainer,
                            iconTint = ext.skyBlueAccent,
                            title = "Technology Stack",
                            subtitle = "Kotlin, Compose, Material 3, Room & WorkManager",
                            trailingText = "${KronozaAboutCatalog.technologyStack.size} items",
                            onClick = onOpenTechStack,
                            testTag = "settings_tech_stack"
                        )
                        PreferenceRow(
                            icon = KronozaIcons.Scale,
                            iconBg = ext.lavenderContainer,
                            iconTint = ext.lavenderAccent,
                            title = "Open Source Licenses",
                            subtitle = "Third-party libraries & attributions",
                            trailingText = "${KronozaAboutCatalog.openSourceLicenses.size} libs",
                            onClick = onOpenLicenses,
                            testTag = "settings_open_source_licenses"
                        )
                        PreferenceRow(
                            icon = KronozaIcons.Code,
                            iconBg = ext.mintContainer,
                            iconTint = ext.mintAccent,
                            title = "Source Code & Contributions",
                            subtitle = "GitHub profile, repository & issue links",
                            trailingText = "Explore",
                            onClick = onOpenSourceContributions,
                            testTag = "settings_source_contributions"
                        )
                    }
                }
            }

            // ── 8. ABOUT KRONOZA ───────────────────────────────────────────────
            item {
                SettingsSectionHeader(title = "About Kronoza")
            }

            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onOpenAboutHub)
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("settings_about_credits"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KronozaLogoMark(size = 40.dp, variation = LogoVariation.PRIMARY)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "About & Credits",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ext.textPrimary
                                )
                                Text(
                                    text = "App info, developer profile, tech stack & licenses",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.textSecondary
                                )
                            }
                            Text(
                                text = "v${KronozaAboutCatalog.versionName}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            KronozaIcon(
                                imageVector = KronozaIcons.ChevronRight,
                                contentDescription = null,
                                tint = ext.textSecondary,
                                size = 16.dp
                            )
                        }
                        PreferenceRow(
                            icon = KronozaIcons.Info,
                            iconBg = ext.lavenderContainer,
                            iconTint = ext.lavenderAccent,
                            title = "About Kronoza",
                            subtitle = KronozaAboutCatalog.appTagline,
                            trailingText = "Build #${KronozaAboutCatalog.versionCode}",
                            onClick = onOpenAboutApp,
                            testTag = "settings_about_kronoza"
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Kronoza v${KronozaAboutCatalog.versionName} • Crafted by ${dev.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.textSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    trailingLabel: String? = null
) {
    val ext = LocalKronozaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = ext.textPrimary
        )
        if (trailingLabel != null) {
            Text(
                text = trailingLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SelectionDialogRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    subtitle: String? = null
) {
    val ext = LocalKronozaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = ext.textPrimary)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
            }
        }
        if (selected) {
            KronozaIcon(
                imageVector = KronozaIcons.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                size = 18.dp
            )
        }
    }
}

@Composable
private fun StorageStatChip(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ext.mutedSurface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ext.textPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ext.textSecondary
            )
        }
    }
}

@Composable
private fun PreferenceSwitchRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    val ext = LocalKronozaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            KronozaIcon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                size = 20.dp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = ext.textPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun ThemePreviewOption(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("settings_theme_${mode.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else ext.cardBorder
            ),
            color = when (mode) {
                ThemeMode.LIGHT -> Color(0xFFF8F9FB)
                ThemeMode.DARK -> Color(0xFF1F2229)
                ThemeMode.SYSTEM -> Color(0xFFF8F9FB)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (mode) {
                    ThemeMode.LIGHT -> {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFD5D8E2))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(Color.White)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(Color(0xFFE9E5FF))
                            )
                        }
                    }
                    ThemeMode.DARK -> {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF3E4252))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(Color(0xFF2B2F3A))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(Color(0xFF302B52))
                            )
                        }
                    }
                    ThemeMode.SYSTEM -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(Color(0xFFF8F9FB))
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(22.dp)
                                        .align(Alignment.BottomCenter)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE9E5FF))
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(Color(0xFF1F2229))
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(22.dp)
                                        .align(Alignment.BottomCenter)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF302B52))
                                )
                            }
                        }
                    }
                }
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        KronozaIcon(
                            imageVector = KronozaIcons.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            size = 12.dp
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = mode.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.primary else ext.textSecondary
        )
    }
}

@Composable
private fun PreferenceRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailingText: String,
    onClick: () -> Unit,
    testTag: String,
    trailingColor: Color = MaterialTheme.colorScheme.primary
) {
    val ext = LocalKronozaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            KronozaIcon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                size = 20.dp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = ext.textPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
        }
        Text(
            text = trailingText,
            style = MaterialTheme.typography.labelLarge,
            color = trailingColor
        )
        Spacer(Modifier.width(6.dp))
        KronozaIcon(
            imageVector = KronozaIcons.ChevronRight,
            contentDescription = null,
            tint = ext.textSecondary,
            size = 16.dp
        )
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes <= 0L -> "0 KB"
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> "${(bytes / 1024.0).toInt().coerceAtLeast(1)} KB"
    else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
}
