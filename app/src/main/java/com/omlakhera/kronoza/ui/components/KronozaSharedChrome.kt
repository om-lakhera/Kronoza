package com.omlakhera.kronoza.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.omlakhera.kronoza.R
import com.omlakhera.kronoza.model.CalendarViewMode
import com.omlakhera.kronoza.ui.navigation.KronozaRoutes
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

val KronozaBottomNavItems = listOf(
    BottomNavItem(
        route = KronozaRoutes.HOME,
        label = "Home",
        selectedIcon = KronozaIcons.HomeFilled,
        unselectedIcon = KronozaIcons.HomeOutline,
        testTag = "nav_home"
    ),
    BottomNavItem(
        route = KronozaRoutes.CALENDAR,
        label = "Calendar",
        selectedIcon = KronozaIcons.CalendarFilled,
        unselectedIcon = KronozaIcons.CalendarOutline,
        testTag = "nav_calendar"
    ),
    BottomNavItem(
        route = KronozaRoutes.TASKS,
        label = "Tasks",
        selectedIcon = KronozaIcons.TasksFilled,
        unselectedIcon = KronozaIcons.TasksOutline,
        testTag = "nav_tasks"
    ),
    BottomNavItem(
        route = KronozaRoutes.INSIGHTS,
        label = "Insights",
        selectedIcon = KronozaIcons.InsightsFilled,
        unselectedIcon = KronozaIcons.InsightsOutline,
        testTag = "nav_insights"
    ),
    BottomNavItem(
        route = KronozaRoutes.SETTINGS,
        label = "Settings",
        selectedIcon = KronozaIcons.SettingsFilled,
        unselectedIcon = KronozaIcons.SettingsOutline,
        testTag = "nav_settings"
    )
)

@Composable
fun KronozaCard(
    modifier: Modifier = Modifier,
    containerColor: Color = LocalKronozaColors.current.card,
    borderColor: Color = LocalKronozaColors.current.cardBorder,
    cornerRadius: Int = 20,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .kronozaPressScale()
                        .clip(RoundedCornerShape(cornerRadius.dp))
                        .clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(cornerRadius.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp
    ) {
        Column(content = content)
    }
}

@Composable
fun KronozaProfileAvatar(
    photoPath: String,
    contentDescription: String?,
    userName: String = "",
    size: Dp = 34.dp,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val customBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = photoPath) {
        value = if (photoPath.isBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    val file = File(photoPath)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                    } else null
                }.getOrNull()
            }
        }
    }

    val shapeModifier = modifier
        .size(size)
        .clip(CircleShape)
        .border(1.dp, ext.cardBorder, CircleShape)

    if (customBitmap != null) {
        Image(
            bitmap = customBitmap!!,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = shapeModifier
        )
    } else {
        val initials = remember(userName) {
            userName.trim()
                .split(Regex("\\s+"))
                .filter { it.isNotEmpty() }
                .take(2)
                .joinToString("") { it.first().uppercaseChar().toString() }
        }
        Box(
            modifier = shapeModifier.background(ext.lavenderContainer),
            contentAlignment = Alignment.Center
        ) {
            if (initials.isNotEmpty()) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                KronozaIcon(
                    imageVector = KronozaIcons.User,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    size = (size * 0.52f).coerceAtLeast(16.dp)
                )
            }
        }
    }
}

@Composable
fun KronozaEmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    accentContainer: Color = LocalKronozaColors.current.lavenderContainer,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    KronozaCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onAction
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentContainer),
                contentAlignment = Alignment.Center
            ) {
                KronozaIcon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    size = 24.dp
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ext.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ext.textSecondary,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onAction) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun KronozaLoadingState(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = ext.textSecondary
        )
    }
}

@Composable
fun KronozaTopAppBar(
    title: String,
    profilePhotoPath: String = "",
    userName: String = "",
    showBack: Boolean = false,
    showActions: Boolean = true,
    unreadNotifications: Int = 0,
    onBackClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {}
) {
    val ext = LocalKronozaColors.current
    Surface(
        color = ext.card,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBack) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("top_back_button")
                    ) {
                        KronozaIcon(
                            imageVector = KronozaIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = ext.textPrimary,
                            size = 21.dp
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                }
                KronozaLogoMark(
                    size = 30.dp,
                    variation = LogoVariation.PRIMARY
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = ext.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (showActions) {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("top_search_button")
                    ) {
                        KronozaIcon(
                            imageVector = KronozaIcons.Search,
                            contentDescription = "Search",
                            tint = ext.textPrimary,
                            size = 20.dp
                        )
                    }
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("top_notifications_button")
                    ) {
                        Box {
                            KronozaIcon(
                                imageVector = KronozaIcons.Bell,
                                contentDescription = "Notifications",
                                tint = ext.textPrimary,
                                size = 20.dp
                            )
                            if (unreadNotifications > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .align(Alignment.TopEnd)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .border(1.5.dp, ext.card, CircleShape)
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable(onClick = onAvatarClick)
                        .testTag("top_avatar_button"),
                    contentAlignment = Alignment.Center
                ) {
                    KronozaProfileAvatar(
                        photoPath = profilePhotoPath,
                        contentDescription = "Profile settings",
                        userName = userName,
                        size = 34.dp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ext.cardBorder)
            )
        }
    }
}

@Composable
fun KronozaBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val ext = LocalKronozaColors.current
    val reduced = LocalReducedMotion.current
    Surface(
        color = ext.card,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ext.cardBorder)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KronozaBottomNavItems.forEach { item ->
                    val selected = currentRoute == item.route
                    val tint by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primary else ext.textSecondary,
                        animationSpec = KronozaMotion.fastTween(reduced),
                        label = "nav_tint"
                    )
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onNavigate(item.route) }
                            .minimumInteractiveComponentSize()
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag(item.testTag),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        KronozaIcon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = tint,
                            size = 22.dp
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = tint,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KronozaNavRail(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val ext = LocalKronozaColors.current
    NavigationRail(
        containerColor = ext.card,
        contentColor = ext.textPrimary
    ) {
        Spacer(Modifier.height(16.dp))
        KronozaBottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationRailItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    KronozaIcon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        size = 22.dp
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = ext.textSecondary,
                    unselectedTextColor = ext.textSecondary
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

@Composable
fun KronozaFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp),
        modifier = modifier
            .size(56.dp)
            .kronozaPressScale(pressedScale = 0.94f)
            .testTag("main_fab")
    ) {
        KronozaIcon(
            imageVector = KronozaIcons.Plus,
            contentDescription = "Create new event or task",
            tint = MaterialTheme.colorScheme.onPrimary,
            size = 24.dp
        )
    }
}

@Composable
fun CalendarSegmentedToggle(
    selectedMode: CalendarViewMode,
    onSelectMode: (CalendarViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val reduced = LocalReducedMotion.current
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = ext.mutedSurface,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalendarViewMode.entries.forEach { mode ->
                val isSelected = mode == selectedMode
                val pillBg by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = KronozaMotion.fastTween(reduced),
                    label = "cal_mode_bg"
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = pillBg,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onSelectMode(mode) }
                        .testTag("calendar_mode_${mode.name.lowercase()}")
                ) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else ext.textSecondary,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCreateSheet(
    onDismiss: () -> Unit,
    onCreateEvent: () -> Unit,
    onCreateTask: () -> Unit
) {
    val ext = LocalKronozaColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ext.card,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Create in Kronoza",
                style = MaterialTheme.typography.headlineSmall,
                color = ext.textPrimary
            )
            KronozaCard(
                containerColor = ext.lavenderContainer.copy(alpha = 0.55f),
                borderColor = Color.Transparent,
                onClick = {
                    onDismiss()
                    onCreateEvent()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_create_event")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        KronozaIcon(
                            imageVector = KronozaIcons.CalendarOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            size = 21.dp
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("New Event", style = MaterialTheme.typography.titleMedium, color = ext.textPrimary)
                        Text(
                            "Schedule a meeting, focus block, or multi-day event",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.textSecondary
                        )
                    }
                }
            }
            KronozaCard(
                containerColor = ext.skyBlueContainer.copy(alpha = 0.65f),
                borderColor = Color.Transparent,
                onClick = {
                    onDismiss()
                    onCreateTask()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_create_task")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ext.skyBlueAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        KronozaIcon(
                            imageVector = KronozaIcons.TasksOutline,
                            contentDescription = null,
                            tint = Color.White,
                            size = 21.dp
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("New Task", style = MaterialTheme.typography.titleMedium, color = ext.textPrimary)
                        Text(
                            "Add a to-do with priority, due time, and subtasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.textSecondary
                        )
                    }
                }
            }
        }
    }
}
