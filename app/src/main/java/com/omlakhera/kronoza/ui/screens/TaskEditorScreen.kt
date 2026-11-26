package com.omlakhera.kronoza.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.omlakhera.kronoza.data.local.TaskEntity
import com.omlakhera.kronoza.model.SubtaskItem
import com.omlakhera.kronoza.model.TaskCategory
import com.omlakhera.kronoza.model.TaskPriority
import com.omlakhera.kronoza.model.UserPreferences
import com.omlakhera.kronoza.ui.components.KronozaAnimatedCheckbox
import com.omlakhera.kronoza.ui.components.KronozaCard
import com.omlakhera.kronoza.ui.components.KronozaIcon
import com.omlakhera.kronoza.ui.components.KronozaIcons
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors
import com.omlakhera.kronoza.util.KronozaDate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(
    existingTask: TaskEntity?,
    initialDateEpochDay: Long?,
    preferences: UserPreferences,
    onCancel: () -> Unit,
    onSave: (TaskEntity) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val today = remember { KronozaDate.today() }
    val tomorrow = remember { today.plusDays(1) }
    val defaultDate = remember(initialDateEpochDay) {
        if (initialDateEpochDay != null && initialDateEpochDay > 0L) {
            KronozaDate.fromEpochDay(initialDateEpochDay)
        } else today
    }

    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    var description by remember { mutableStateOf(existingTask?.description ?: "") }
    var dueDate by remember {
        mutableStateOf(existingTask?.let { KronozaDate.fromEpochDay(it.dueEpochDay) } ?: defaultDate)
    }
    var dueMinutes by remember { mutableIntStateOf(existingTask?.dueMinutes ?: 14 * 60) }
    var priority by remember { mutableStateOf(existingTask?.priority ?: TaskPriority.MEDIUM) }
    var category by remember { mutableStateOf(existingTask?.category ?: TaskCategory.DESIGN) }
    var subtasks by remember { mutableStateOf(existingTask?.subtasks ?: emptyList()) }
    var reminderEnabled by remember { mutableStateOf(existingTask?.reminderEnabled ?: true) }
    var titleError by remember { mutableStateOf(false) }

    LaunchedEffect(existingTask?.id) {
        if (existingTask != null) {
            title = existingTask.title
            description = existingTask.description
            dueDate = KronozaDate.fromEpochDay(existingTask.dueEpochDay)
            dueMinutes = existingTask.dueMinutes
            priority = existingTask.priority
            category = existingTask.category
            subtasks = existingTask.subtasks
            reminderEnabled = existingTask.reminderEnabled
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showAddSubtaskDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog && existingTask != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Delete \"${existingTask.title}\" permanently?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete(existingTask.id)
                    }
                ) { Text("Delete", color = ext.coralAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dueDate.epochDay * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { dueDate = KronozaDate.fromEpochDay(it / 86_400_000L) }
                        showDatePicker = false
                    }
                ) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = state) }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = dueMinutes / 60,
            initialMinute = dueMinutes % 60,
            is24Hour = preferences.timeFormat24h
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueMinutes = state.hour * 60 + state.minute
                        showTimePicker = false
                    }
                ) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = state) }
        )
    }

    if (showAddSubtaskDialog) {
        var subTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddSubtaskDialog = false },
            title = { Text("Add Subtask") },
            text = {
                OutlinedTextField(
                    value = subTitle,
                    onValueChange = { subTitle = it },
                    label = { Text("Subtask title") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (subTitle.isNotBlank()) {
                            subtasks = subtasks + SubtaskItem(
                                id = UUID.randomUUID().toString(),
                                title = subTitle.trim(),
                                done = false
                            )
                        }
                        showAddSubtaskDialog = false
                    }
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubtaskDialog = false }) { Text("Cancel") }
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
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Action Header
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
                                .testTag("task_editor_cancel")
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
                        text = if (existingTask == null) "New Task" else "Edit Task",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ext.textPrimary
                    )
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                                return@Button
                            }
                            val entity = (existingTask ?: TaskEntity(
                                title = "",
                                dueEpochDay = dueDate.epochDay,
                                dueMinutes = dueMinutes
                            )).copy(
                                title = title.trim(),
                                description = description.trim(),
                                dueEpochDay = dueDate.epochDay,
                                dueMinutes = dueMinutes,
                                priority = priority,
                                category = category,
                                subtasks = subtasks,
                                reminderEnabled = reminderEnabled
                            )
                            onSave(entity)
                        },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("task_editor_save")
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

            // Inline validation banner
            item {
                AnimatedVisibility(visible = titleError) {
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
                                text = "Please enter a task title before saving.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ext.onCoralContainer
                            )
                        }
                    }
                }
            }

            // Title & Description Card
            item {
                KronozaCard(
                    borderColor = if (titleError) ext.coralAccent else ext.cardBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Task Title",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (titleError) ext.coralAccent else ext.textSecondary
                        )
                        Surface(shape = RoundedCornerShape(14.dp), color = ext.mutedSurface, modifier = Modifier.fillMaxWidth()) {
                            BasicTextField(
                                value = title,
                                onValueChange = {
                                    title = it
                                    titleError = false
                                },
                                textStyle = MaterialTheme.typography.titleMedium.copy(color = ext.textPrimary),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                                    .testTag("task_title_input"),
                                decorationBox = { inner ->
                                    if (title.isEmpty()) {
                                        Text(
                                            text = "Finalize mobile UI prototype",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = ext.textSecondary.copy(alpha = 0.55f)
                                        )
                                    }
                                    inner()
                                }
                            )
                        }

                        Text("Description (Optional)", style = MaterialTheme.typography.labelMedium, color = ext.textSecondary)
                        Surface(shape = RoundedCornerShape(14.dp), color = ext.mutedSurface, modifier = Modifier.fillMaxWidth()) {
                            BasicTextField(
                                value = description,
                                onValueChange = { description = it },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = ext.textPrimary),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(74.dp)
                                    .padding(14.dp),
                                decorationBox = { inner ->
                                    if (description.isEmpty()) {
                                        Text(
                                            text = "Add context, checklist notes, or links...",
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

            // Due Date & Time Card
            item {
                val dueBadgeText = remember(dueDate, dueMinutes, today, tomorrow, preferences.dateFormat, preferences.timeFormat24h) {
                    val dayPart = when (dueDate) {
                        today -> "Today"
                        tomorrow -> "Tomorrow"
                        else -> dueDate.formatWithPreference(preferences.dateFormat)
                    }
                    "$dayPart, ${KronozaDate.formatTime(dueMinutes, preferences.timeFormat24h)}"
                }
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
                            Text("Due Date & Time", style = MaterialTheme.typography.titleSmall, color = ext.textPrimary)
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = ext.lavenderContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable { showTimePicker = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    KronozaIcon(
                                        imageVector = KronozaIcons.Clock,
                                        contentDescription = null,
                                        tint = ext.onLavenderContainer,
                                        size = 13.dp
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = dueBadgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                        fontWeight = FontWeight.SemiBold,
                                        color = ext.onLavenderContainer
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DateChipButton(
                                label = "Today",
                                selected = dueDate == today,
                                onClick = { dueDate = today }
                            )
                            DateChipButton(
                                label = "Tomorrow",
                                selected = dueDate == tomorrow,
                                onClick = { dueDate = tomorrow }
                            )
                            DateChipButton(
                                label = "Pick date",
                                selected = dueDate != today && dueDate != tomorrow,
                                onClick = { showDatePicker = true }
                            )
                        }
                    }
                }
            }

            // Priority Card
            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Priority", style = MaterialTheme.typography.titleSmall, color = ext.textPrimary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TaskPriority.entries.forEach { p ->
                                val selected = p == priority
                                val bg = when (p) {
                                    TaskPriority.HIGH -> ext.coralContainer
                                    TaskPriority.MEDIUM -> ext.lavenderContainer
                                    TaskPriority.LOW -> ext.mintContainer
                                }
                                val fg = when (p) {
                                    TaskPriority.HIGH -> ext.onCoralContainer
                                    TaskPriority.MEDIUM -> ext.onLavenderContainer
                                    TaskPriority.LOW -> ext.onMintContainer
                                }
                                val dotColor = when (p) {
                                    TaskPriority.HIGH -> ext.coralAccent
                                    TaskPriority.MEDIUM -> ext.lavenderAccent
                                    TaskPriority.LOW -> ext.mintAccent
                                }
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = bg,
                                    border = if (selected) BorderStroke(1.5.dp, dotColor) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(999.dp))
                                        .clickable { priority = p }
                                        .testTag("task_priority_${p.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (selected) {
                                            KronozaIcon(
                                                imageVector = KronozaIcons.TasksFilled,
                                                contentDescription = null,
                                                tint = dotColor,
                                                size = 14.dp
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                            )
                                        }
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = p.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = fg
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Project / Category Card
            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Project / Category", style = MaterialTheme.typography.titleSmall, color = ext.textPrimary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TaskCategory.entries.forEach { cat ->
                                val selected = cat == category
                                val icon = when (cat) {
                                    TaskCategory.DESIGN -> KronozaIcons.Palette
                                    TaskCategory.PERSONAL -> KronozaIcons.Flag
                                    else -> KronozaIcons.Sliders
                                }
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (selected) ext.lavenderContainer else ext.mutedSurface,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .clickable { category = cat }
                                        .testTag("task_cat_${cat.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        KronozaIcon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (selected) ext.onLavenderContainer else ext.textSecondary,
                                            size = 15.dp
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = cat.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) ext.onLavenderContainer else ext.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Subtasks Card
            item {
                val doneSubs = subtasks.count { it.done }
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
                            Text("Subtasks", style = MaterialTheme.typography.titleSmall, color = ext.textPrimary)
                            Text(
                                text = "$doneSubs of ${subtasks.size} done",
                                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                color = ext.textSecondary
                            )
                        }

                        subtasks.forEach { sub ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = ext.mutedSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        subtasks = subtasks.map {
                                            if (it.id == sub.id) it.copy(done = !it.done) else it
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    KronozaAnimatedCheckbox(
                                        checked = sub.done,
                                        accentColor = ext.mintAccent,
                                        borderColor = ext.cardBorder,
                                        onToggle = {
                                            subtasks = subtasks.map {
                                                if (it.id == sub.id) it.copy(done = !it.done) else it
                                            }
                                        },
                                        boxSize = 20.dp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = sub.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (sub.done) ext.textSecondary else ext.textPrimary,
                                        textDecoration = if (sub.done) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { subtasks = subtasks.filterNot { it.id == sub.id } },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        KronozaIcon(
                                            imageVector = KronozaIcons.Close,
                                            contentDescription = "Remove subtask",
                                            tint = ext.textSecondary,
                                            size = 15.dp
                                        )
                                    }
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ext.mutedSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showAddSubtaskDialog = true }
                                .testTag("task_add_subtask_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Plus,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 18.dp
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Add subtask",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Reminder Toggle Card
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ext.skyBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.Bell,
                                contentDescription = null,
                                tint = ext.skyBlueAccent,
                                size = 19.dp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reminder", style = MaterialTheme.typography.titleMedium, color = ext.textPrimary)
                            Text(
                                text = if (preferences.defaultReminder.minutesBefore > 0) {
                                    "Notify ${preferences.defaultReminder.label.lowercase()}"
                                } else {
                                    "Notify at due time"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.textSecondary
                            )
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it }
                        )
                    }
                }
            }

            if (existingTask != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = ext.coralContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { showDeleteConfirmDialog = true }
                            .testTag("task_editor_delete")
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
                            Spacer(Modifier.width(8.dp))
                            Text("Delete Task", style = MaterialTheme.typography.labelLarge, color = ext.coralAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateChipButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val ext = LocalKronozaColors.current
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) ext.lavenderContainer else ext.mutedSurface,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KronozaIcon(
                imageVector = KronozaIcons.CalendarOutline,
                contentDescription = null,
                tint = if (selected) ext.onLavenderContainer else ext.textSecondary,
                size = 15.dp
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) ext.onLavenderContainer else ext.textPrimary
            )
        }
    }
}
