package com.omlakhera.kronoza.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.omlakhera.kronoza.data.local.NotificationEntity
import com.omlakhera.kronoza.model.NotificationType
import com.omlakhera.kronoza.ui.components.KronozaCard
import com.omlakhera.kronoza.ui.components.KronozaEmptyStateCard
import com.omlakhera.kronoza.ui.components.KronozaIcon
import com.omlakhera.kronoza.ui.components.KronozaIcons
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors
import com.omlakhera.kronoza.util.KronozaDate

private enum class NotifTab(val label: String) {
    ALL("All"),
    EVENTS("Events"),
    TASKS("Tasks"),
    INVITES("Invites")
}

@Composable
fun NotificationsScreen(
    notifications: List<NotificationEntity>,
    notificationPermissionGranted: Boolean,
    eventRemindersEnabled: Boolean,
    taskRemindersEnabled: Boolean,
    onMarkRead: (NotificationEntity) -> Unit,
    onMarkAllRead: () -> Unit,
    onRespondInvite: (NotificationEntity, Boolean) -> Unit,
    onOpenLinkedEvent: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val today = remember { KronozaDate.today() }
    var selectedTab by remember { mutableStateOf(NotifTab.ALL) }

    val unreadCount = remember(notifications) { notifications.count { it.unread } }
    val filtered = remember(notifications, selectedTab) {
        when (selectedTab) {
            NotifTab.ALL -> notifications
            NotifTab.EVENTS -> notifications.filter { it.type == NotificationType.EVENT }
            NotifTab.TASKS -> notifications.filter { it.type == NotificationType.TASK }
            NotifTab.INVITES -> notifications.filter { it.type == NotificationType.INVITE }
        }
    }
    val todayItems = remember(filtered) { filtered.filter { it.isToday } }
    val earlierItems = remember(filtered) { filtered.filter { !it.isToday } }

    val showStatusBanner = !notificationPermissionGranted || (!eventRemindersEnabled && !taskRemindersEnabled)

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
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Unread Count Pill + Mark All Read
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = ext.lavenderContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                text = "$unreadCount unread",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ext.onLavenderContainer
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onMarkAllRead)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("notif_mark_all_read"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KronozaIcon(
                            imageVector = KronozaIcons.DoneAll,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            size = 18.dp
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Mark all read",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (showStatusBanner) {
                item {
                    KronozaCard(
                        containerColor = ext.coralContainer.copy(alpha = 0.72f),
                        borderColor = ext.coralAccent.copy(alpha = 0.35f),
                        onClick = onOpenSettings,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.Shield,
                                contentDescription = null,
                                tint = ext.coralAccent,
                                size = 18.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (!notificationPermissionGranted) "Notification alerts disabled"
                                    else "Reminders paused in Settings",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ext.onCoralContainer
                                )
                                Text(
                                    text = "Tap to review notification & reminder settings",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.onCoralContainer
                                )
                            }
                            KronozaIcon(
                                imageVector = KronozaIcons.ChevronRight,
                                contentDescription = null,
                                tint = ext.onCoralContainer,
                                size = 16.dp
                            )
                        }
                    }
                }
            }

            // Filter Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NotifTab.entries.forEach { tab ->
                        val selected = tab == selectedTab
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else ext.card,
                            border = if (selected) null else BorderStroke(1.dp, ext.cardBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { selectedTab = tab }
                                .testTag("notif_tab_${tab.name.lowercase()}")
                        ) {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else ext.textPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                            )
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    KronozaEmptyStateCard(
                        icon = KronozaIcons.Bell,
                        title = if (notifications.isEmpty()) "No notifications yet" else "All caught up",
                        subtitle = if (notifications.isEmpty()) {
                            "Event and task reminder alerts will appear here as your schedule progresses."
                        } else {
                            "No ${selectedTab.label.lowercase()} notifications at the moment."
                        },
                        actionLabel = if (selectedTab != NotifTab.ALL) "Show all" else null,
                        onAction = if (selectedTab != NotifTab.ALL) {
                            { selectedTab = NotifTab.ALL }
                        } else null
                    )
                }
            }

            if (todayItems.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Today", style = MaterialTheme.typography.titleLarge, color = ext.textPrimary)
                        Text(today.formatWeekdayShortDate(), style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
                    }
                }
                items(todayItems, key = { it.id }) { notif ->
                    NotificationCard(
                        notification = notif,
                        onClick = {
                            onMarkRead(notif)
                            notif.linkedEventId?.let(onOpenLinkedEvent)
                        },
                        onAcceptInvite = { onRespondInvite(notif, true) },
                        onDeclineInvite = { onRespondInvite(notif, false) }
                    )
                }
            }

            if (earlierItems.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Earlier", style = MaterialTheme.typography.titleLarge, color = ext.textPrimary)
                        Text("This week", style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
                    }
                }
                items(earlierItems, key = { it.id }) { notif ->
                    NotificationCard(
                        notification = notif,
                        onClick = {
                            onMarkRead(notif)
                            notif.linkedEventId?.let(onOpenLinkedEvent)
                        },
                        onAcceptInvite = { onRespondInvite(notif, true) },
                        onDeclineInvite = { onRespondInvite(notif, false) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationEntity,
    onClick: () -> Unit,
    onAcceptInvite: () -> Unit,
    onDeclineInvite: () -> Unit
) {
    val ext = LocalKronozaColors.current
    val iconBg = when (notification.type) {
        NotificationType.EVENT -> if (notification.isToday) ext.lavenderContainer else ext.skyBlueContainer
        NotificationType.TASK -> ext.coralContainer
        NotificationType.INVITE -> ext.skyBlueContainer
        NotificationType.INSIGHT -> ext.mintContainer
    }
    val iconTint = when (notification.type) {
        NotificationType.EVENT -> if (notification.isToday) ext.lavenderAccent else ext.skyBlueAccent
        NotificationType.TASK -> ext.coralAccent
        NotificationType.INVITE -> ext.skyBlueAccent
        NotificationType.INSIGHT -> ext.mintAccent
    }
    val icon = when (notification.type) {
        NotificationType.EVENT -> if (notification.isToday) KronozaIcons.CalendarOutline else KronozaIcons.Reschedule
        NotificationType.TASK -> KronozaIcons.Clock
        NotificationType.INVITE -> KronozaIcons.CalendarOutline
        NotificationType.INSIGHT -> KronozaIcons.TasksFilled
    }

    KronozaCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notif_card_${notification.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                KronozaIcon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    size = 21.dp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = ext.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (notification.unread) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.textSecondary
                )

                if (notification.metaChipLabel.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val chipBg = if (notification.type == NotificationType.TASK) ext.coralContainer else ext.lavenderContainer
                        val chipFg = if (notification.type == NotificationType.TASK) ext.onCoralContainer else ext.onLavenderContainer
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = chipBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (notification.type == NotificationType.EVENT) {
                                    KronozaIcon(
                                        imageVector = KronozaIcons.VideoCall,
                                        contentDescription = null,
                                        tint = chipFg,
                                        size = 13.dp
                                    )
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(
                                    text = notification.metaChipLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = chipFg
                                )
                            }
                        }
                        Text(
                            text = notification.timeAgoLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.textSecondary
                        )
                    }
                } else {
                    Spacer(Modifier.height(6.dp))
                    if (notification.type == NotificationType.INVITE) {
                        when (notification.inviteStatus) {
                            "PENDING" -> {
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = onAcceptInvite,
                                        shape = RoundedCornerShape(999.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("notif_invite_accept")
                                    ) {
                                        Text("Accept", style = MaterialTheme.typography.labelMedium)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = ext.mutedSurface,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .clickable(onClick = onDeclineInvite)
                                            .testTag("notif_invite_decline")
                                    ) {
                                        Text(
                                            text = "Decline",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = ext.textPrimary,
                                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                            "ACCEPTED" -> {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = ext.mintContainer
                                ) {
                                    Text(
                                        text = "Accepted • Added to calendar",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.onMintContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                            "DECLINED" -> {
                                Text(
                                    text = "Declined",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.textSecondary
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                    Text(
                        text = notification.timeAgoLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = ext.textSecondary
                    )
                }
            }
        }
    }
}
