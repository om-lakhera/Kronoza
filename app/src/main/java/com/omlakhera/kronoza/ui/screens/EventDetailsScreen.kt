package com.omlakhera.kronoza.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.omlakhera.kronoza.R
import com.omlakhera.kronoza.data.local.EventEntity
import com.omlakhera.kronoza.model.RecurrenceEditScope
import com.omlakhera.kronoza.model.RepeatFrequency
import com.omlakhera.kronoza.model.UserPreferences
import com.omlakhera.kronoza.ui.components.KronozaAnimatedCheckbox
import com.omlakhera.kronoza.ui.components.KronozaCard
import com.omlakhera.kronoza.ui.components.KronozaEmptyStateCard
import com.omlakhera.kronoza.ui.components.KronozaIcon
import com.omlakhera.kronoza.ui.components.KronozaIcons
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors
import com.omlakhera.kronoza.util.KronozaDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    event: EventEntity?,
    occurrenceEpochDay: Long?,
    preferences: UserPreferences,
    snackbarHostState: SnackbarHostState,
    onEditEvent: (Long, Long?) -> Unit,
    onDuplicateEvent: (Long) -> Unit,
    onRescheduleEvent: (Long, Long, Int, Int) -> Unit,
    onDeleteEventWithScope: (EventEntity, Long, RecurrenceEditScope) -> Unit,
    onToggleChecklistItem: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRescheduleDialog by remember { mutableStateOf(false) }

    if (event == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(ext.canvas)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            KronozaEmptyStateCard(
                icon = KronozaIcons.CalendarOutline,
                title = "Event not found",
                subtitle = "This event may have been removed or rescheduled.",
                modifier = Modifier.widthIn(max = 460.dp)
            )
        }
        return
    }

    val displayEpochDay = remember(event.dateEpochDay, occurrenceEpochDay, event.repeatFrequency) {
        occurrenceEpochDay?.takeIf { it > 0L && event.repeatFrequency != RepeatFrequency.NONE }
            ?: event.dateEpochDay
    }
    val eventDate = remember(displayEpochDay) { KronozaDate.fromEpochDay(displayEpochDay) }
    val endDate = remember(event.effectiveEndDateEpochDay) {
        KronozaDate.fromEpochDay(event.effectiveEndDateEpochDay)
    }
    val today = remember { KronozaDate.today() }
    val relativeLabel = remember(event, displayEpochDay, today) {
        val diffDays = displayEpochDay - today.epochDay
        when {
            diffDays == 0L -> {
                val minsDiff = event.startMinutes - KronozaDate.currentTimeMinutes()
                when {
                    minsDiff in 1..120 -> "In $minsDiff mins"
                    minsDiff in -60..0 -> "Happening now"
                    else -> "Today"
                }
            }
            diffDays == 1L -> "Tomorrow"
            diffDays > 1L -> "In $diffDays days"
            else -> eventDate.formatWithPreference(preferences.dateFormat)
        }
    }

    val isRecurring = event.repeatFrequency != RepeatFrequency.NONE

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(if (isRecurring) "Delete Recurring Event" else "Delete Event") },
            text = {
                Text(
                    if (isRecurring) {
                        "Delete only the occurrence on ${eventDate.formatWeekdayShortDate()}, or remove the entire recurring series?"
                    } else {
                        "Remove \"${event.title}\" from your calendar?"
                    }
                )
            },
            confirmButton = {
                if (isRecurring) {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteEventWithScope(event, displayEpochDay, RecurrenceEditScope.THIS_OCCURRENCE)
                        }
                    ) {
                        Text("This occurrence", color = ext.coralAccent)
                    }
                } else {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteEventWithScope(event, displayEpochDay, RecurrenceEditScope.ENTIRE_SERIES)
                        }
                    ) {
                        Text("Delete", color = ext.coralAccent)
                    }
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                    if (isRecurring) {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                onDeleteEventWithScope(event, displayEpochDay, RecurrenceEditScope.ENTIRE_SERIES)
                            }
                        ) {
                            Text("Entire series", color = ext.coralAccent)
                        }
                    }
                }
            }
        )
    }

    if (showRescheduleDialog) {
        var pickedDate by remember(displayEpochDay) { mutableStateOf(eventDate) }
        var pickedStart by remember(event.startMinutes) { mutableIntStateOf(event.startMinutes) }
        var pickedEnd by remember(event.endMinutes) { mutableIntStateOf(event.endMinutes) }
        var pickingDate by remember { mutableStateOf(false) }
        var pickingStart by remember { mutableStateOf(false) }
        var pickingEnd by remember { mutableStateOf(false) }

        if (pickingDate) {
            val state = rememberDatePickerState(initialSelectedDateMillis = pickedDate.epochDay * 86_400_000L)
            DatePickerDialog(
                onDismissRequest = { pickingDate = false },
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let { pickedDate = KronozaDate.fromEpochDay(it / 86_400_000L) }
                        pickingDate = false
                    }) { Text("Done") }
                },
                dismissButton = { TextButton(onClick = { pickingDate = false }) { Text("Cancel") } }
            ) { DatePicker(state = state) }
        }

        if (pickingStart) {
            val state = rememberTimePickerState(pickedStart / 60, pickedStart % 60, preferences.timeFormat24h)
            AlertDialog(
                onDismissRequest = { pickingStart = false },
                confirmButton = {
                    TextButton(onClick = {
                        val s = state.hour * 60 + state.minute
                        val duration = (pickedEnd - pickedStart).coerceAtLeast(30)
                        pickedStart = s
                        pickedEnd = (s + duration).coerceAtMost(23 * 60 + 55)
                        pickingStart = false
                    }) { Text("Set") }
                },
                dismissButton = { TextButton(onClick = { pickingStart = false }) { Text("Cancel") } },
                text = { TimePicker(state = state) }
            )
        }

        if (pickingEnd) {
            val state = rememberTimePickerState(pickedEnd / 60, pickedEnd % 60, preferences.timeFormat24h)
            AlertDialog(
                onDismissRequest = { pickingEnd = false },
                confirmButton = {
                    TextButton(onClick = {
                        val e = state.hour * 60 + state.minute
                        pickedEnd = e.coerceAtLeast(pickedStart + 15)
                        pickingEnd = false
                    }) { Text("Set") }
                },
                dismissButton = { TextButton(onClick = { pickingEnd = false }) { Text("Cancel") } },
                text = { TimePicker(state = state) }
            )
        }

        AlertDialog(
            onDismissRequest = { showRescheduleDialog = false },
            title = { Text("Reschedule Event") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ext.mutedSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { pickingDate = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Date", style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
                            Text(
                                pickedDate.formatWithPreference(preferences.dateFormat),
                                style = MaterialTheme.typography.titleSmall,
                                color = ext.textPrimary
                            )
                        }
                    }
                    if (!event.allDay) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ext.mutedSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { pickingStart = true }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Start", style = MaterialTheme.typography.labelSmall, color = ext.textSecondary)
                                    Text(
                                        KronozaDate.formatTime(pickedStart, preferences.timeFormat24h),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = ext.textPrimary
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ext.mutedSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { pickingEnd = true }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("End", style = MaterialTheme.typography.labelSmall, color = ext.textSecondary)
                                    Text(
                                        KronozaDate.formatTime(pickedEnd, preferences.timeFormat24h),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = ext.textPrimary
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
                        showRescheduleDialog = false
                        onRescheduleEvent(event.id, pickedDate.epochDay, pickedStart, pickedEnd)
                    }
                ) { Text("Reschedule") }
            },
            dismissButton = {
                TextButton(onClick = { showRescheduleDialog = false }) { Text("Cancel") }
            }
        )
    }

    val accent = ext.eventAccent(event)
    val container = ext.eventContainer(event)
    val onContainer = ext.onEventContainer(event)

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Metadata Pill + Quick Edit / Duplicate / Reschedule / Delete Circles
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = ext.card,
                        border = BorderStroke(1.dp, ext.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.CalendarOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                size = 15.dp
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = relativeLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = ext.textPrimary
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = ext.card,
                            border = BorderStroke(1.dp, ext.cardBorder),
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(
                                onClick = { onDuplicateEvent(event.id) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("details_top_duplicate")
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Duplicate,
                                    contentDescription = "Duplicate event",
                                    tint = ext.textPrimary,
                                    size = 17.dp
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = ext.card,
                            border = BorderStroke(1.dp, ext.cardBorder),
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(
                                onClick = { showRescheduleDialog = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("details_top_reschedule")
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Reschedule,
                                    contentDescription = "Reschedule event",
                                    tint = ext.textPrimary,
                                    size = 17.dp
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = ext.card,
                            border = BorderStroke(1.dp, ext.cardBorder),
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(
                                onClick = { onEditEvent(event.id, displayEpochDay) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("details_top_edit")
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Edit,
                                    contentDescription = "Edit event",
                                    tint = ext.textPrimary,
                                    size = 17.dp
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = ext.card,
                            border = BorderStroke(1.dp, ext.cardBorder),
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("details_top_delete")
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Trash,
                                    contentDescription = "Delete event",
                                    tint = ext.coralAccent,
                                    size = 17.dp
                                )
                            }
                        }
                    }
                }
            }

            // Refined Tinted Surface Hero Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = container,
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = ext.card.copy(alpha = 0.9f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(accent)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = event.category.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ext.textPrimary
                                    )
                                }
                            }
                            if (event.repeatFrequency != RepeatFrequency.NONE) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    KronozaIcon(
                                        imageVector = KronozaIcons.Repeat,
                                        contentDescription = null,
                                        tint = onContainer,
                                        size = 14.dp
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${event.repeatFrequency.label} Sync",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = onContainer
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = ext.textPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            KronozaIcon(
                                imageVector = KronozaIcons.CalendarOutline,
                                contentDescription = null,
                                tint = onContainer,
                                size = 16.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            val dateRangeLabel = if (event.isMultiDay && displayEpochDay == event.dateEpochDay) {
                                "${eventDate.formatWithPreference(preferences.dateFormat)} – ${endDate.formatWithPreference(preferences.dateFormat)}"
                            } else {
                                "${eventDate.weekdayName()}, ${eventDate.formatWithPreference(preferences.dateFormat)}"
                            }
                            val timeStr = if (event.allDay) {
                                "$dateRangeLabel • All Day"
                            } else {
                                "$dateRangeLabel • ${KronozaDate.formatTimeRange(event.startMinutes, event.endMinutes, preferences.timeFormat24h)}"
                            }
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                                color = onContainer
                            )
                        }
                        if (event.meetingUrl.isNotBlank()) {
                            Spacer(Modifier.height(18.dp))
                            Button(
                                onClick = {
                                    val uri = Uri.parse(event.meetingUrl)
                                    val scheme = uri.scheme?.lowercase()
                                    if (scheme == "https" || scheme == "http") {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        }.onFailure {
                                            scope.launch { snackbarHostState.showSnackbar("No browser available to open meeting link") }
                                        }
                                    } else {
                                        scope.launch { snackbarHostState.showSnackbar("Meeting link: ${event.meetingUrl}") }
                                    }
                                },
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("details_join_call_btn")
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.VideoCall,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    size = 18.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Join Video Call", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // Location & Reminder Card
            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ext.skyBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                KronozaIcon(
                                    imageVector = if (event.meetingUrl.isNotBlank()) KronozaIcons.VideoCall else KronozaIcons.LocationPin,
                                    contentDescription = null,
                                    tint = ext.skyBlueAccent,
                                    size = 19.dp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Location", style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
                                Text(
                                    text = event.location.ifBlank { "No location added" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ext.textPrimary
                                )
                            }
                            if (event.location.isNotBlank() || event.meetingUrl.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = ext.mutedSurface,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .clickable {
                                            val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val textToCopy = event.meetingUrl.ifBlank { event.location }
                                            clip.setPrimaryClip(ClipData.newPlainText("Event Location", textToCopy))
                                            scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
                                        }
                                        .testTag("details_copy_location")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        KronozaIcon(
                                            imageVector = KronozaIcons.Copy,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            size = 14.dp
                                        )
                                        Spacer(Modifier.width(5.dp))
                                        Text(
                                            text = "Copy",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ext.mintContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Bell,
                                    contentDescription = null,
                                    tint = ext.mintAccent,
                                    size = 19.dp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reminder", style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
                                Text(
                                    text = event.reminder.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ext.textPrimary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = ext.mutedSurface
                            ) {
                                Text(
                                    text = if (event.reminder.minutesBefore > 0) "Active" else "Off",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.textSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Attendees Card
            if (event.attendees.isNotEmpty()) {
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
                                Text(
                                    text = "Attendees (${event.attendees.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ext.textPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = ext.mintContainer
                                ) {
                                    Text(
                                        text = "All Accepted",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ext.onMintContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            event.attendees.forEach { attendee ->
                                val initials = attendee.name.trim()
                                    .split(Regex("\\s+"))
                                    .filter { it.isNotEmpty() }
                                    .take(2)
                                    .joinToString("") { it.first().uppercaseChar().toString() }
                                    .ifBlank { "?" }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(ext.lavenderContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = attendee.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = ext.textPrimary
                                        )
                                        Text(
                                            text = attendee.role,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ext.textSecondary
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = ext.mintContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            KronozaIcon(
                                                imageVector = KronozaIcons.Check,
                                                contentDescription = null,
                                                tint = ext.onMintContainer,
                                                size = 13.dp
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "Accepted",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ext.onMintContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Agenda & Notes Card
            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Notes,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 18.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Agenda & Notes",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ext.textPrimary
                                )
                            }
                            if (event.checklist.isNotEmpty()) {
                                Text(
                                    text = "Tap to check off",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.textSecondary
                                )
                            }
                        }
                        if (event.notes.isNotBlank()) {
                            Text(
                                text = event.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ext.textSecondary
                            )
                        } else if (event.checklist.isEmpty()) {
                            Text(
                                text = "No agenda notes added yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.textSecondary
                            )
                        }
                        event.checklist.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = ext.mutedSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onToggleChecklistItem(event.id, item.id) }
                                    .testTag("details_checklist_${item.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    KronozaAnimatedCheckbox(
                                        checked = item.done,
                                        accentColor = MaterialTheme.colorScheme.primary,
                                        borderColor = ext.textSecondary,
                                        onToggle = { onToggleChecklistItem(event.id, item.id) },
                                        boxSize = 22.dp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = item.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (item.done) ext.textSecondary else ext.textPrimary,
                                        textDecoration = if (item.done) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Edit / Delete Action Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = ext.card,
                        border = BorderStroke(1.dp, ext.cardBorder),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { onEditEvent(event.id, displayEpochDay) }
                            .testTag("details_edit_btn")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.Edit,
                                contentDescription = null,
                                tint = ext.textPrimary,
                                size = 18.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Edit Event",
                                style = MaterialTheme.typography.labelLarge,
                                color = ext.textPrimary
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = ext.coralContainer,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { showDeleteDialog = true }
                            .testTag("details_delete_btn")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.Trash,
                                contentDescription = null,
                                tint = ext.coralAccent,
                                size = 18.dp
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Delete",
                                style = MaterialTheme.typography.labelLarge,
                                color = ext.coralAccent
                            )
                        }
                    }
                }
            }
        }
    }
}
