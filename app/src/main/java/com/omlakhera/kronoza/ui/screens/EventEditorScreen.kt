package com.omlakhera.kronoza.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omlakhera.kronoza.data.local.EventEntity
import com.omlakhera.kronoza.model.EventCategory
import com.omlakhera.kronoza.model.EventColorOption
import com.omlakhera.kronoza.model.RecurrenceEditScope
import com.omlakhera.kronoza.model.ReminderOption
import com.omlakhera.kronoza.model.RepeatFrequency
import com.omlakhera.kronoza.model.UserPreferences
import com.omlakhera.kronoza.ui.components.KronozaCard
import com.omlakhera.kronoza.ui.components.KronozaIcon
import com.omlakhera.kronoza.ui.components.KronozaIcons
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors
import com.omlakhera.kronoza.util.CalendarEngine
import com.omlakhera.kronoza.util.KronozaDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorScreen(
    existingEvent: EventEntity?,
    existingEvents: List<EventEntity>,
    initialDateEpochDay: Long?,
    initialHour: Int?,
    occurrenceEpochDay: Long?,
    preferences: UserPreferences,
    onCancel: () -> Unit,
    onSave: (EventEntity) -> Unit,
    onSaveWithScope: (EventEntity, Long, EventEntity, RecurrenceEditScope) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val defaultDate = remember(initialDateEpochDay) {
        if (initialDateEpochDay != null && initialDateEpochDay > 0L) {
            KronozaDate.fromEpochDay(initialDateEpochDay)
        } else {
            KronozaDate.today()
        }
    }
    val defaultStart = remember(initialHour) {
        if (initialHour != null && initialHour in 0..22) initialHour * 60 else 9 * 60 + 30
    }
    val defaultDuration = remember(preferences.defaultEventDurationMinutes) {
        preferences.defaultEventDurationMinutes.coerceIn(15, 360)
    }

    var title by remember { mutableStateOf(existingEvent?.title ?: "") }
    var allDay by remember { mutableStateOf(existingEvent?.allDay ?: false) }
    var date by remember {
        mutableStateOf(
            existingEvent?.let {
                val occ = occurrenceEpochDay?.takeIf { d -> d > 0L && it.repeatFrequency != RepeatFrequency.NONE }
                KronozaDate.fromEpochDay(occ ?: it.dateEpochDay)
            } ?: defaultDate
        )
    }
    var endDate by remember {
        mutableStateOf(
            existingEvent?.let {
                val occ = occurrenceEpochDay?.takeIf { d -> d > 0L && it.repeatFrequency != RepeatFrequency.NONE }
                if (occ != null) KronozaDate.fromEpochDay(occ) else KronozaDate.fromEpochDay(it.effectiveEndDateEpochDay)
            } ?: defaultDate
        )
    }
    var startMinutes by remember { mutableIntStateOf(existingEvent?.startMinutes ?: defaultStart) }
    var endMinutes by remember {
        mutableIntStateOf(
            existingEvent?.endMinutes ?: (defaultStart + defaultDuration).coerceAtMost(23 * 60 + 55)
        )
    }
    var repeatFreq by remember { mutableStateOf(existingEvent?.repeatFrequency ?: RepeatFrequency.NONE) }
    var location by remember { mutableStateOf(existingEvent?.location ?: "") }
    var category by remember { mutableStateOf(existingEvent?.category ?: EventCategory.WORK) }
    var customColorHex by remember { mutableStateOf(existingEvent?.customColorHex ?: "") }
    var reminder by remember { mutableStateOf(existingEvent?.reminder ?: preferences.defaultReminder) }
    var notes by remember { mutableStateOf(existingEvent?.notes ?: "") }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(existingEvent?.id, occurrenceEpochDay) {
        if (existingEvent != null) {
            title = existingEvent.title
            allDay = existingEvent.allDay
            val occ = occurrenceEpochDay?.takeIf { d -> d > 0L && existingEvent.repeatFrequency != RepeatFrequency.NONE }
            date = KronozaDate.fromEpochDay(occ ?: existingEvent.dateEpochDay)
            endDate = if (occ != null) date else KronozaDate.fromEpochDay(existingEvent.effectiveEndDateEpochDay)
            startMinutes = existingEvent.startMinutes
            endMinutes = existingEvent.endMinutes
            repeatFreq = existingEvent.repeatFrequency
            location = existingEvent.location
            category = existingEvent.category
            customColorHex = existingEvent.customColorHex
            reminder = existingEvent.reminder
            notes = existingEvent.notes
        }
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var repeatMenuExpanded by remember { mutableStateOf(false) }
    var reminderMenuExpanded by remember { mutableStateOf(false) }
    var pendingSaveEntity by remember { mutableStateOf<EventEntity?>(null) }

    val isMultiDay = endDate.epochDay > date.epochDay
    val effectiveAccentColor = remember(category, customColorHex, ext) {
        CalendarEngine.parseHexColorOrNull(customColorHex) ?: ext.categoryAccent(category)
    }

    val conflicts = remember(
        date,
        endDate,
        startMinutes,
        endMinutes,
        allDay,
        existingEvents,
        existingEvent?.id
    ) {
        if (endDate < date || (!allDay && !isMultiDay && endMinutes <= startMinutes)) {
            emptyList()
        } else {
            CalendarEngine.findConflictingEvents(
                candidateDateEpochDay = date.epochDay,
                candidateEndDateEpochDay = endDate.epochDay,
                candidateStartMinutes = startMinutes,
                candidateEndMinutes = endMinutes,
                candidateAllDay = allDay,
                existingEvents = existingEvents,
                excludeEventId = existingEvent?.id
            )
        }
    }

    fun buildCandidateEntity(): EventEntity {
        val normalizedRepeat = if (endDate.epochDay > date.epochDay) RepeatFrequency.NONE else repeatFreq
        return (existingEvent ?: EventEntity(
            title = "",
            dateEpochDay = date.epochDay,
            startMinutes = startMinutes,
            endMinutes = endMinutes
        )).copy(
            title = title.trim(),
            dateEpochDay = date.epochDay,
            endDateEpochDay = if (endDate.epochDay > date.epochDay) endDate.epochDay else 0L,
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            allDay = allDay,
            repeatFrequency = normalizedRepeat,
            location = location.trim(),
            meetingUrl = existingEvent?.meetingUrl ?: "",
            category = category,
            customColorHex = customColorHex,
            reminder = reminder,
            notes = notes.trim(),
            badgeLabel = existingEvent?.badgeLabel?.ifBlank { category.displayName } ?: category.displayName
        )
    }

    if (pendingSaveEntity != null && existingEvent != null) {
        val candidate = pendingSaveEntity!!
        val targetOccDay = occurrenceEpochDay?.takeIf { it > 0L } ?: existingEvent.dateEpochDay
        AlertDialog(
            onDismissRequest = { pendingSaveEntity = null },
            title = { Text("Save Recurring Event") },
            text = {
                Text("Apply these changes only to ${date.formatWeekdayShortDate()}, or to the entire recurring series?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingSaveEntity = null
                        onSaveWithScope(existingEvent, targetOccDay, candidate, RecurrenceEditScope.THIS_OCCURRENCE)
                    }
                ) {
                    Text("This occurrence")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { pendingSaveEntity = null }) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            pendingSaveEntity = null
                            onSaveWithScope(existingEvent, targetOccDay, candidate, RecurrenceEditScope.ENTIRE_SERIES)
                        }
                    ) {
                        Text("Entire series")
                    }
                }
            }
        )
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.epochDay * 86_400_000L
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val picked = KronozaDate.fromEpochDay(millis / 86_400_000L)
                            val prevSpan = (endDate.epochDay - date.epochDay).coerceAtLeast(0L)
                            date = picked
                            endDate = picked.plusDays(prevSpan.toInt())
                            validationMessage = null
                        }
                        showStartDatePicker = false
                    }
                ) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.epochDay * 86_400_000L
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val picked = KronozaDate.fromEpochDay(millis / 86_400_000L)
                            endDate = if (picked < date) date else picked
                            if (endDate > date) {
                                repeatFreq = RepeatFrequency.NONE
                            }
                            validationMessage = null
                        }
                        showEndDatePicker = false
                    }
                ) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = startMinutes / 60,
            initialMinute = startMinutes % 60,
            is24Hour = preferences.timeFormat24h
        )
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newStart = timeState.hour * 60 + timeState.minute
                        startMinutes = newStart
                        if (!isMultiDay && endMinutes <= newStart) {
                            endMinutes = (newStart + defaultDuration).coerceAtMost(23 * 60 + 55)
                        }
                        validationMessage = null
                        showStartTimePicker = false
                    }
                ) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timeState) }
        )
    }

    if (showEndTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = endMinutes / 60,
            initialMinute = endMinutes % 60,
            is24Hour = preferences.timeFormat24h
        )
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = timeState.hour * 60 + timeState.minute
                        endMinutes = if (!isMultiDay) picked.coerceAtLeast(startMinutes + 15) else picked
                        validationMessage = null
                        showEndTimePicker = false
                    }
                ) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timeState) }
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
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Editor Top Action Row (Close X, Title, Save Pill)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ext.card,
                        border = BorderStroke(1.dp, ext.cardBorder),
                        modifier = Modifier.size(48.dp)
                    ) {
                        IconButton(
                            onClick = onCancel,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("event_editor_cancel")
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.Close,
                                contentDescription = "Close",
                                tint = ext.textPrimary,
                                size = 20.dp
                            )
                        }
                    }
                    Text(
                        text = if (existingEvent == null) "New Event" else "Edit Event",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ext.textPrimary
                    )
                    Button(
                        onClick = {
                            when {
                                title.isBlank() -> {
                                    validationMessage = "Please enter an event title before saving."
                                    return@Button
                                }
                                endDate < date -> {
                                    validationMessage = "End date cannot be earlier than start date."
                                    return@Button
                                }
                                !allDay && !isMultiDay && endMinutes <= startMinutes -> {
                                    validationMessage = "End time must be after start time."
                                    return@Button
                                }
                            }
                            val entity = buildCandidateEntity()
                            if (existingEvent != null &&
                                existingEvent.repeatFrequency != RepeatFrequency.NONE &&
                                occurrenceEpochDay != null &&
                                occurrenceEpochDay > 0L
                            ) {
                                pendingSaveEntity = entity
                            } else {
                                onSave(entity)
                            }
                        },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("event_editor_save")
                    ) {
                        KronozaIcon(
                            imageVector = KronozaIcons.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            size = 16.dp
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Save", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Validation Error Banner
            item {
                AnimatedVisibility(visible = validationMessage != null) {
                    KronozaCard(
                        containerColor = ext.coralContainer,
                        borderColor = ext.coralAccent.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.AlertTriangle,
                                contentDescription = null,
                                tint = ext.coralAccent,
                                size = 18.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = validationMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ext.onCoralContainer
                            )
                        }
                    }
                }
            }

            // Real-time Conflict Detection Banner
            item {
                AnimatedVisibility(visible = conflicts.isNotEmpty() && !allDay) {
                    val names = conflicts.take(2).joinToString(", ") { "\"${it.title}\"" }
                    val extra = if (conflicts.size > 2) " +${conflicts.size - 2} more" else ""
                    KronozaCard(
                        containerColor = ext.coralContainer.copy(alpha = 0.72f),
                        borderColor = ext.coralAccent.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("event_conflict_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.AlertTriangle,
                                contentDescription = null,
                                tint = ext.coralAccent,
                                size = 18.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Schedule overlap detected",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ext.onCoralContainer
                                )
                                Text(
                                    text = "Overlaps with $names$extra",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.onCoralContainer
                                )
                            }
                        }
                    }
                }
            }

            // Event Title Card
            item {
                KronozaCard(
                    borderColor = if (validationMessage != null && title.isBlank()) ext.coralAccent else ext.cardBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(effectiveAccentColor)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Event Title",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (validationMessage != null && title.isBlank()) ext.coralAccent else ext.textSecondary
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        BasicTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                if (it.isNotBlank()) validationMessage = null
                            },
                            textStyle = MaterialTheme.typography.titleLarge.copy(color = ext.textPrimary),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("event_title_input"),
                            decorationBox = { inner ->
                                if (title.isEmpty()) {
                                    Text(
                                        text = "e.g. Q4 Product Roadmap Sync",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = ext.textSecondary.copy(alpha = 0.55f)
                                    )
                                }
                                inner()
                            }
                        )
                    }
                }
            }

            // Date, Multi-day, Time & Repeat Card
            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ext.lavenderContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Clock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 18.dp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "All-day",
                                style = MaterialTheme.typography.titleMedium,
                                color = ext.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = allDay,
                                onCheckedChange = { allDay = it },
                                modifier = Modifier.testTag("event_all_day_switch")
                            )
                        }

                        // Start & End Date Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = ext.mutedSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { showStartDatePicker = true }
                                    .testTag("event_start_date_row")
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                                    Text("Start Date", style = MaterialTheme.typography.labelSmall, color = ext.textSecondary)
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = date.formatWithPreference(preferences.dateFormat),
                                        style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
                                        color = ext.textPrimary
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = ext.mutedSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { showEndDatePicker = true }
                                    .testTag("event_end_date_row")
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                                    Text("End Date", style = MaterialTheme.typography.labelSmall, color = ext.textSecondary)
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = endDate.formatWithPreference(preferences.dateFormat),
                                        style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
                                        color = ext.textPrimary
                                    )
                                }
                            }
                        }

                        if (!allDay) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = ext.mutedSurface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { showStartTimePicker = true }
                                        .testTag("event_start_time_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Start", style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
                                        Text(
                                            text = KronozaDate.formatTime(startMinutes, preferences.timeFormat24h),
                                            style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                                            color = ext.textPrimary
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = ext.mutedSurface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { showEndTimePicker = true }
                                        .testTag("event_end_time_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("End", style = MaterialTheme.typography.bodySmall, color = ext.textSecondary)
                                        Text(
                                            text = KronozaDate.formatTime(endMinutes, preferences.timeFormat24h),
                                            style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                                            color = ext.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Repeat Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ext.mutedSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Repeat,
                                    contentDescription = null,
                                    tint = ext.textSecondary,
                                    size = 18.dp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Repeat",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ext.textPrimary
                                )
                                if (isMultiDay) {
                                    Text(
                                        text = "Unavailable for multi-day spans",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.textSecondary
                                    )
                                }
                            }
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isMultiDay) ext.mutedSurface else ext.lavenderContainer,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .clickable(enabled = !isMultiDay) { repeatMenuExpanded = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isMultiDay) RepeatFrequency.NONE.label else repeatFreq.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isMultiDay) ext.textSecondary else ext.onLavenderContainer
                                        )
                                        if (!isMultiDay) {
                                            Spacer(Modifier.width(4.dp))
                                            KronozaIcon(
                                                imageVector = KronozaIcons.ChevronDown,
                                                contentDescription = null,
                                                tint = ext.onLavenderContainer,
                                                size = 15.dp
                                            )
                                        }
                                    }
                                }
                                DropdownMenu(
                                    expanded = repeatMenuExpanded,
                                    onDismissRequest = { repeatMenuExpanded = false }
                                ) {
                                    RepeatFrequency.entries.forEach { freq ->
                                        DropdownMenuItem(
                                            text = { Text(freq.label) },
                                            onClick = {
                                                repeatFreq = freq
                                                repeatMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Location / Video Call Card
            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ext.skyBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.VideoCall,
                                contentDescription = null,
                                tint = ext.skyBlueAccent,
                                size = 19.dp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Location / Video Call",
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.textSecondary
                            )
                            Spacer(Modifier.height(4.dp))
                            BasicTextField(
                                value = location,
                                onValueChange = { location = it },
                                textStyle = MaterialTheme.typography.titleSmall.copy(color = ext.textPrimary),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    if (location.isEmpty()) {
                                        Text(
                                            text = "Add room or Google Meet link",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = ext.textSecondary.copy(alpha = 0.55f)
                                        )
                                    }
                                    inner()
                                }
                            )
                        }
                        IconButton(
                            onClick = {
                                if (location.isBlank()) location = "Google Meet • Link attached"
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.Duplicate,
                                contentDescription = "Attach Meet link",
                                tint = MaterialTheme.colorScheme.primary,
                                size = 18.dp
                            )
                        }
                    }
                }
            }

            // Category & Custom Event Color Card
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
                                text = "Category & Event Color",
                                style = MaterialTheme.typography.titleMedium,
                                color = ext.textPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = effectiveAccentColor.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = effectiveAccentColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EventCategory.entries.forEach { cat ->
                                val selected = cat == category
                                val bg = ext.categoryContainer(cat)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(bg)
                                        .border(
                                            width = if (selected) 2.dp else 0.dp,
                                            color = if (selected) ext.categoryAccent(cat) else Color.Transparent,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { category = cat }
                                        .testTag("event_cat_${cat.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = ext.onCategoryContainer(cat),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Custom Event Color Swatches
                        Text(
                            text = "Custom Color Accent",
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.textSecondary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EventColorOption.entries.forEach { option ->
                                val hex = option.hex.orEmpty()
                                val selected = customColorHex.equals(hex, ignoreCase = true)
                                val swatchColor = option.hex?.let { CalendarEngine.parseHexColorOrNull(it) }
                                    ?: ext.categoryAccent(category)
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (selected) swatchColor.copy(alpha = 0.16f) else ext.mutedSurface,
                                    border = if (selected) BorderStroke(1.5.dp, swatchColor) else null,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .clickable { customColorHex = hex }
                                        .testTag("event_color_${option.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(swatchColor)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                            color = if (selected) swatchColor else ext.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Reminder & Notes Card
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
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ext.mutedSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Bell,
                                    contentDescription = null,
                                    tint = ext.textPrimary,
                                    size = 18.dp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Reminder",
                                style = MaterialTheme.typography.titleMedium,
                                color = ext.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = ext.mutedSurface,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .clickable { reminderMenuExpanded = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = reminder.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = ext.textPrimary
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        KronozaIcon(
                                            imageVector = KronozaIcons.ChevronDown,
                                            contentDescription = null,
                                            tint = ext.textSecondary,
                                            size = 15.dp
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = reminderMenuExpanded,
                                    onDismissRequest = { reminderMenuExpanded = false }
                                ) {
                                    ReminderOption.entries.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt.label) },
                                            onClick = {
                                                reminder = opt
                                                reminderMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ext.mutedSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    KronozaIcon(
                                        imageVector = KronozaIcons.Notes,
                                        contentDescription = null,
                                        tint = ext.textSecondary,
                                        size = 15.dp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Notes", style = MaterialTheme.typography.labelSmall, color = ext.textSecondary)
                                }
                                Spacer(Modifier.height(8.dp))
                                BasicTextField(
                                    value = notes,
                                    onValueChange = { notes = it },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = ext.textPrimary),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(78.dp),
                                    decorationBox = { inner ->
                                        if (notes.isEmpty()) {
                                            Text(
                                                text = "Add meeting agenda, links, or preparation notes...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = ext.textSecondary.copy(alpha = 0.55f)
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
