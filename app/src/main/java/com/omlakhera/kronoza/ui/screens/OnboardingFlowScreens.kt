package com.omlakhera.kronoza.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omlakhera.kronoza.model.CalendarViewMode
import com.omlakhera.kronoza.model.EventCategory
import com.omlakhera.kronoza.model.ThemeMode
import com.omlakhera.kronoza.model.UserPreferences
import com.omlakhera.kronoza.reminders.KronozaNotificationHelper
import com.omlakhera.kronoza.ui.components.KronozaCalendarIllustration
import com.omlakhera.kronoza.ui.components.KronozaCard
import com.omlakhera.kronoza.ui.components.KronozaIcon
import com.omlakhera.kronoza.ui.components.KronozaIcons
import com.omlakhera.kronoza.ui.components.KronozaLogoMark
import com.omlakhera.kronoza.ui.components.KronozaMotion
import com.omlakhera.kronoza.ui.components.KronozaProfileAvatar
import com.omlakhera.kronoza.ui.components.KronozaTasksIllustration
import com.omlakhera.kronoza.ui.components.KronozaWelcomeIllustration
import com.omlakhera.kronoza.ui.components.LocalReducedMotion
import com.omlakhera.kronoza.ui.components.LogoVariation
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors
import kotlinx.coroutines.delay

enum class OnboardingStep {
    SPLASH,
    WELCOME,
    CALENDAR_INTRO,
    TASKS_INTRO,
    PROFILE_SETUP,
    PERMISSIONS
}

@Composable
fun OnboardingFlowRoot(
    initialPreferences: UserPreferences,
    canScheduleExactAlarms: () -> Boolean,
    onUpdateProfile: (String, String) -> Unit,
    onSaveProfilePhoto: (Uri) -> Unit,
    onSetPreferredCategories: (Set<EventCategory>) -> Unit,
    onSetStartWeekMonday: (Boolean) -> Unit,
    onSetDefaultCalendarView: (CalendarViewMode) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onCompleteOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val reduced = LocalReducedMotion.current
    var step by remember { mutableStateOf(OnboardingStep.SPLASH) }

    BackHandler(enabled = step.ordinal > OnboardingStep.WELCOME.ordinal) {
        val entries = OnboardingStep.entries
        val prevIndex = (step.ordinal - 1).coerceAtLeast(OnboardingStep.WELCOME.ordinal)
        step = entries[prevIndex]
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ext.canvas),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val direction = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                KronozaMotion.directionalSlideTransform(direction, reduced)
            },
            label = "ftue_step"
        ) { currentStep ->
            when (currentStep) {
                OnboardingStep.SPLASH -> SplashScreen(
                    onFinished = { step = OnboardingStep.WELCOME }
                )
                OnboardingStep.WELCOME -> WelcomeScreen(
                    onGetStarted = { step = OnboardingStep.CALENDAR_INTRO },
                    onAlreadySetUp = onCompleteOnboarding
                )
                OnboardingStep.CALENDAR_INTRO -> OnboardingFeatureScreen(
                    stepIndex = 1,
                    totalSteps = 4,
                    title = "Everything in one place.",
                    subtitle = "Switch effortlessly between Month, Week, Day, and Agenda views with real timezone-aware scheduling.",
                    illustration = { KronozaCalendarIllustration() },
                    onBack = { step = OnboardingStep.WELCOME },
                    onNext = { step = OnboardingStep.TASKS_INTRO },
                    onSkip = onCompleteOnboarding
                )
                OnboardingStep.TASKS_INTRO -> OnboardingFeatureScreen(
                    stepIndex = 2,
                    totalSteps = 4,
                    title = "Calm focus, clear priorities.",
                    subtitle = "Track tasks with due times, overdue indicators, subtasks, and productivity insights built from your real activity.",
                    illustration = { KronozaTasksIllustration() },
                    onBack = { step = OnboardingStep.CALENDAR_INTRO },
                    onNext = { step = OnboardingStep.PROFILE_SETUP },
                    onSkip = onCompleteOnboarding
                )
                OnboardingStep.PROFILE_SETUP -> ProfileSetupScreen(
                    initialPreferences = initialPreferences,
                    onBack = { step = OnboardingStep.TASKS_INTRO },
                    onPickProfilePhoto = onSaveProfilePhoto,
                    onSaveAndContinue = { name, email, startMon, viewMode, theme, categories ->
                        onUpdateProfile(name, email)
                        onSetStartWeekMonday(startMon)
                        onSetDefaultCalendarView(viewMode)
                        onSetThemeMode(theme)
                        onSetPreferredCategories(categories)
                        step = OnboardingStep.PERMISSIONS
                    }
                )
                OnboardingStep.PERMISSIONS -> PermissionsSetupScreen(
                    canScheduleExactAlarms = canScheduleExactAlarms,
                    onBack = { step = OnboardingStep.PROFILE_SETUP },
                    onFinish = onCompleteOnboarding
                )
            }
        }
    }
}

@Composable
private fun SplashScreen(
    onFinished: () -> Unit
) {
    val ext = LocalKronozaColors.current
    val reduced = LocalReducedMotion.current
    val alphaAnim = remember { Animatable(if (reduced) 1f else 0f) }
    val scaleAnim = remember { Animatable(if (reduced) 1f else 0.92f) }

    LaunchedEffect(Unit) {
        if (!reduced) {
            alphaAnim.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            scaleAnim.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        }
        delay(if (reduced) 450 else 800)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ext.canvas)
            .clickable { onFinished() }
            .testTag("ftue_splash"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            KronozaLogoMark(size = 84.dp, variation = LogoVariation.PRIMARY)
            Spacer(Modifier.height(22.dp))
            Text(
                text = "KRONOZA",
                style = MaterialTheme.typography.displayLarge.copy(
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = ext.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Make time work for you.",
                style = MaterialTheme.typography.bodyLarge,
                color = ext.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onAlreadySetUp: () -> Unit
) {
    val ext = LocalKronozaColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 560.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KronozaLogoMark(size = 34.dp, variation = LogoVariation.PRIMARY)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "KRONOZA",
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = ext.textPrimary
                )
            }
            TextButton(
                onClick = onAlreadySetUp,
                modifier = Modifier.testTag("ftue_welcome_skip")
            ) {
                Text("Skip")
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            KronozaWelcomeIllustration()
            Spacer(Modifier.height(28.dp))
            Text(
                text = "Your time. Your way.",
                style = MaterialTheme.typography.displayLarge,
                color = ext.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Plan your days, organize your priorities and make every moment count.",
                style = MaterialTheme.typography.bodyLarge,
                color = ext.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onGetStarted,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(vertical = 15.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ftue_get_started")
            ) {
                Text("Get Started", style = MaterialTheme.typography.titleMedium)
            }
            TextButton(
                onClick = onAlreadySetUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ftue_already_setup")
            ) {
                Text(
                    text = "Already set up? Open app",
                    style = MaterialTheme.typography.labelLarge,
                    color = ext.textSecondary
                )
            }
        }
    }
}

@Composable
private fun OnboardingFeatureScreen(
    stepIndex: Int,
    totalSteps: Int,
    title: String,
    subtitle: String,
    illustration: @Composable () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val ext = LocalKronozaColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 560.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                KronozaIcon(
                    imageVector = KronozaIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = ext.textPrimary,
                    size = 22.dp
                )
            }
            StepDots(current = stepIndex, total = totalSteps)
            TextButton(onClick = onSkip) {
                Text("Skip")
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            illustration()
            Spacer(Modifier.height(28.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                color = ext.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = ext.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(vertical = 15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ftue_next_$stepIndex")
        ) {
            Text("Continue", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileSetupScreen(
    initialPreferences: UserPreferences,
    onBack: () -> Unit,
    onPickProfilePhoto: (Uri) -> Unit,
    onSaveAndContinue: (String, String, Boolean, CalendarViewMode, ThemeMode, Set<EventCategory>) -> Unit
) {
    val ext = LocalKronozaColors.current
    var name by remember { mutableStateOf(initialPreferences.userName) }
    var email by remember { mutableStateOf(initialPreferences.userEmail) }
    var startMonday by remember { mutableStateOf(initialPreferences.startWeekMonday) }
    var defaultView by remember { mutableStateOf(initialPreferences.defaultCalendarView) }
    var themeMode by remember { mutableStateOf(initialPreferences.themeMode) }
    var selectedCategories by remember {
        mutableStateOf(
            initialPreferences.preferredCategories.ifEmpty { EventCategory.entries.toSet() }
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPickProfilePhoto(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 560.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                KronozaIcon(
                    imageVector = KronozaIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = ext.textPrimary,
                    size = 22.dp
                )
            }
            StepDots(current = 3, total = 4)
            Spacer(Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Personalize your workspace",
                style = MaterialTheme.typography.headlineMedium,
                color = ext.textPrimary
            )
            Text(
                text = "Configure your profile, preferred categories, calendar defaults, and theme.",
                style = MaterialTheme.typography.bodyMedium,
                color = ext.textSecondary
            )

            KronozaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("ftue_profile_photo_picker")
                        ) {
                            KronozaProfileAvatar(
                                photoPath = initialPreferences.profilePhotoPath,
                                contentDescription = "Profile photo",
                                userName = name,
                                size = 56.dp
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(1.5.dp, ext.card, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Camera,
                                    contentDescription = "Upload photo",
                                    tint = Color.White,
                                    size = 10.dp
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Profile Photo (Optional)",
                                style = MaterialTheme.typography.titleSmall,
                                color = ext.textPrimary
                            )
                            Text(
                                text = "Tap avatar to select from your device",
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.textSecondary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ftue_name_input")
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address (Optional)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ftue_email_input")
                    )
                }
            }

            KronozaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Focus Categories", style = MaterialTheme.typography.titleSmall, color = ext.textPrimary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EventCategory.entries.forEach { category ->
                            val isSelected = category in selectedCategories
                            SetupChip(
                                label = category.displayName,
                                selected = isSelected,
                                onClick = {
                                    selectedCategories = if (isSelected && selectedCategories.size > 1) {
                                        selectedCategories - category
                                    } else {
                                        selectedCategories + category
                                    }
                                }
                            )
                        }
                    }

                    Text("Start of Week", style = MaterialTheme.typography.titleSmall, color = ext.textPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SetupChip("Monday", startMonday, { startMonday = true }, Modifier.weight(1f))
                        SetupChip("Sunday", !startMonday, { startMonday = false }, Modifier.weight(1f))
                    }

                    Text("Default Calendar View", style = MaterialTheme.typography.titleSmall, color = ext.textPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalendarViewMode.entries.forEach { mode ->
                            SetupChip(
                                label = mode.label,
                                selected = defaultView == mode,
                                onClick = { defaultView = mode },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Text("Theme", style = MaterialTheme.typography.titleSmall, color = ext.textPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            SetupChip(
                                label = mode.label,
                                selected = themeMode == mode,
                                onClick = { themeMode = mode },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                onSaveAndContinue(
                    name.trim(),
                    email.trim(),
                    startMonday,
                    defaultView,
                    themeMode,
                    selectedCategories
                )
            },
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(vertical = 15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ftue_profile_continue")
        ) {
            Text("Save & Continue", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun PermissionsSetupScreen(
    canScheduleExactAlarms: () -> Boolean,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val ext = LocalKronozaColors.current
    val context = LocalContext.current
    var notifGranted by remember {
        mutableStateOf(KronozaNotificationHelper.hasNotificationPermission(context))
    }
    var exactAlarmGranted by remember { mutableStateOf(canScheduleExactAlarms()) }
    var refreshTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTick) {
        notifGranted = KronozaNotificationHelper.hasNotificationPermission(context)
        exactAlarmGranted = canScheduleExactAlarms()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifGranted = granted
    }

    val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        refreshTick++
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 560.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                KronozaIcon(
                    imageVector = KronozaIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = ext.textPrimary,
                    size = 22.dp
                )
            }
            StepDots(current = 4, total = 4)
            Spacer(Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Never miss a moment",
                style = MaterialTheme.typography.headlineMedium,
                color = ext.textPrimary
            )
            Text(
                text = "Enable notifications and exact alarms for timely reminders. If restricted, Kronoza automatically falls back to WorkManager.",
                style = MaterialTheme.typography.bodyMedium,
                color = ext.textSecondary
            )

            KronozaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ext.lavenderContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.Bell,
                                contentDescription = null,
                                tint = ext.lavenderAccent,
                                size = 20.dp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Event & Task Notifications", style = MaterialTheme.typography.titleSmall, color = ext.textPrimary)
                            Text("Alerts before meetings and due tasks", style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
                        }
                    }
                    if (notifGranted) {
                        StatusBadge("Permission Granted", ext.mintContainer, ext.onMintContainer)
                    } else {
                        OutlinedButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    notifGranted = KronozaNotificationHelper.hasNotificationPermission(context)
                                }
                            },
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.testTag("ftue_grant_notifications")
                        ) {
                            Text("Allow Notifications")
                        }
                    }
                }
            }

            KronozaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ext.skyBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.Clock,
                                contentDescription = null,
                                tint = ext.skyBlueAccent,
                                size = 20.dp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Precise Exact Alarms", style = MaterialTheme.typography.titleSmall, color = ext.textPrimary)
                            Text("On-the-minute delivery even in Doze mode", style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
                        }
                    }
                    if (exactAlarmGranted) {
                        StatusBadge("Exact Alarms Enabled", ext.mintContainer, ext.onMintContainer)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                exactAlarmSettingsLauncher.launch(intent)
                            },
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.testTag("ftue_grant_exact_alarm")
                        ) {
                            Text("Enable Exact Alarms")
                        }
                    }
                }
            }
        }

        Button(
            onClick = onFinish,
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(vertical = 15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ftue_finish_setup")
        ) {
            Text("Finish Setup & Enter Kronoza", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun StepDots(current: Int, total: Int) {
    val ext = LocalKronozaColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 1..total) {
            Box(
                modifier = Modifier
                    .width(if (i == current) 22.dp else 8.dp)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(if (i == current) MaterialTheme.colorScheme.primary else ext.cardBorder)
            )
        }
    }
}

@Composable
private fun SetupChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) ext.lavenderContainer else ext.mutedSurface,
        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                KronozaIcon(
                    imageVector = KronozaIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    size = 13.dp
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) ext.onLavenderContainer else ext.textPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StatusBadge(text: String, bg: Color, fg: Color) {
    Surface(shape = RoundedCornerShape(999.dp), color = bg) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KronozaIcon(
                imageVector = KronozaIcons.Check,
                contentDescription = null,
                tint = fg,
                size = 14.dp
            )
            Spacer(Modifier.width(6.dp))
            Text(text = text, style = MaterialTheme.typography.labelMedium, color = fg)
        }
    }
}
