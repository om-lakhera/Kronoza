package com.omlakhera.kronoza.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omlakhera.kronoza.data.local.EventEntity
import com.omlakhera.kronoza.model.CalendarViewMode
import com.omlakhera.kronoza.model.UserPreferences
import com.omlakhera.kronoza.ui.components.CalendarSegmentedToggle
import com.omlakhera.kronoza.ui.components.KronozaCard
import com.omlakhera.kronoza.ui.components.KronozaIcon
import com.omlakhera.kronoza.ui.components.KronozaIcons
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors
import com.omlakhera.kronoza.util.CalendarEngine
import com.omlakhera.kronoza.util.KronozaDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeeklyCalendarScreen(
    events: List<EventEntity>,
    selectedDate: KronozaDate,
    preferences: UserPreferences,
    onSelectDate: (KronozaDate) -> Unit,
    onChangeViewMode: (CalendarViewMode) -> Unit,
    onOpenEventDetails: (Long, Long) -> Unit,
    onCreateEventAtSlot: (KronozaDate, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val today = remember { KronozaDate.today() }
    val baseWeekStart = remember(preferences.startWeekMonday) {
        today.startOfWeek(preferences.startWeekMonday)
    }
    val initialAnchorPage = 500
    val selectedWeekStart = remember(selectedDate, preferences.startWeekMonday) {
        selectedDate.startOfWeek(preferences.startWeekMonday)
    }
    val targetPage = remember(selectedWeekStart, baseWeekStart) {
        val diffWeeks = ((selectedWeekStart.epochDay - baseWeekStart.epochDay) / 7L).toInt()
        (initialAnchorPage + diffWeeks).coerceIn(0, 1000)
    }
    val pagerState = rememberPagerState(initialPage = targetPage, pageCount = { 1001 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(targetPage) {
        if (pagerState.currentPage != targetPage && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        val pageWeekStart = baseWeekStart.plusDays((pagerState.settledPage - initialAnchorPage) * 7)
        val currentSelectedWeek = selectedDate.startOfWeek(preferences.startWeekMonday)
        if (pageWeekStart != currentSelectedWeek) {
            val offsetInWeek = (selectedDate.epochDay - currentSelectedWeek.epochDay).toInt().coerceIn(0, 6)
            onSelectDate(pageWeekStart.plusDays(offsetInWeek))
        }
    }

    val startHour = 7
    val endHour = 20
    val hourHeightDp = 56.dp
    val totalGridHeight = hourHeightDp * (endHour - startHour + 1)

    val tzLabel = remember { KronozaDate.localTimezoneOffsetLabel() }
    val nowMinutes = remember { KronozaDate.currentTimeMinutes() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ext.canvas),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CalendarSegmentedToggle(
                            selectedMode = CalendarViewMode.WEEK,
                            onSelectMode = onChangeViewMode
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onChangeViewMode(CalendarViewMode.MONTH) }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = selectedDate.formatMonthYear(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = ext.textPrimary
                            )
                            Spacer(Modifier.width(4.dp))
                            KronozaIcon(
                                imageVector = KronozaIcons.ChevronDown,
                                contentDescription = "Switch to month",
                                tint = ext.textSecondary,
                                size = 18.dp
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ext.card,
                                modifier = Modifier.size(38.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0))
                                        }
                                    },
                                    modifier = Modifier.testTag("week_prev_btn")
                                ) {
                                    KronozaIcon(
                                        imageVector = KronozaIcons.ChevronLeft,
                                        contentDescription = "Previous week",
                                        tint = ext.textPrimary,
                                        size = 17.dp
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = ext.lavenderContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable { onSelectDate(today) }
                                    .testTag("week_today_btn")
                            ) {
                                Text(
                                    text = "Today",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ext.onLavenderContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = ext.card,
                                modifier = Modifier.size(38.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(1000))
                                        }
                                    },
                                    modifier = Modifier.testTag("week_next_btn")
                                ) {
                                    KronozaIcon(
                                        imageVector = KronozaIcons.ChevronRight,
                                        contentDescription = "Next week",
                                        tint = ext.textPrimary,
                                        size = 17.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Swipeable Week Strip + Grid Pager
            item {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val weekStart = baseWeekStart.plusDays((page - initialAnchorPage) * 7)
                    val weekDays = remember(weekStart) { List(7) { idx -> weekStart.plusDays(idx) } }
                    val weekOccurrences = remember(events, weekStart) {
                        CalendarEngine.expandEventsInRange(
                            events = events,
                            startEpochDay = weekStart.epochDay,
                            endEpochDay = weekStart.plusDays(6).epochDay
                        )
                    }
                    val occsByDay = remember(weekOccurrences) {
                        weekOccurrences.groupBy { it.occurrenceEpochDay }
                    }
                    val allDayBannerEvents = remember(weekOccurrences) {
                        weekOccurrences.filter { it.event.allDay || it.isMultiDayContinuation || it.event.isMultiDay }
                            .distinctBy { it.event.id }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Week Strip Card
                        KronozaCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tzLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.textSecondary,
                                    modifier = Modifier.width(44.dp)
                                )
                                weekDays.forEach { day ->
                                    val isSelected = day == selectedDate
                                    val isToday = day == today
                                    val hasEvents = occsByDay[day.epochDay].orEmpty().isNotEmpty()
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> ext.lavenderContainer
                                            else -> Color.Transparent
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { onSelectDate(day) }
                                            .testTag("week_strip_${day.epochDay}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = day.shortWeekdayName(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                                else ext.textSecondary
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = day.dayOfMonth.toString(),
                                                style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else ext.textPrimary
                                            )
                                            Spacer(Modifier.height(3.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            !hasEvents -> Color.Transparent
                                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                                            else -> MaterialTheme.colorScheme.primary
                                                        }
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // All-day & Multi-day Banner Strip
                        if (allDayBannerEvents.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allDayBannerEvents.forEach { occ ->
                                    val ev = occ.event
                                    val bg = ext.eventContainer(ev)
                                    val fg = ext.onEventContainer(ev)
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = bg,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .clickable { onOpenEventDetails(ev.id, occ.occurrenceEpochDay) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(ext.eventAccent(ev))
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = if (ev.isMultiDay) "${ev.title} (${KronozaDate.fromEpochDay(ev.dateEpochDay).formatShortDate()}–${KronozaDate.fromEpochDay(ev.effectiveEndDateEpochDay).formatShortDate()})"
                                                else "${ev.title} • All day",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = fg,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 7-Column Time Grid Card with Overlapping Event Columns
                        KronozaCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 14.dp)
                            ) {
                                Column(modifier = Modifier.width(44.dp)) {
                                    for (h in startHour..endHour) {
                                        Box(
                                            modifier = Modifier.height(hourHeightDp),
                                            contentAlignment = Alignment.TopStart
                                        ) {
                                            Text(
                                                text = KronozaDate.formatHourLabel(h, preferences.timeFormat24h),
                                                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                                color = ext.textSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(totalGridHeight)
                                ) {
                                    // Background hour grid + slot click targets
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        for (h in startHour..endHour) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(hourHeightDp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(1.dp)
                                                        .background(ext.cardBorder)
                                                )
                                                Row(modifier = Modifier.fillMaxSize()) {
                                                    weekDays.forEach { day ->
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .fillMaxSize()
                                                                .clickable { onCreateEventAtSlot(day, h) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Placed overlapping events per column
                                    Row(modifier = Modifier.fillMaxSize()) {
                                        weekDays.forEach { day ->
                                            val placedEvents = remember(occsByDay[day.epochDay]) {
                                                CalendarEngine.layoutOverlappingEvents(occsByDay[day.epochDay].orEmpty())
                                            }
                                            BoxWithConstraints(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(totalGridHeight)
                                                    .padding(horizontal = 1.dp)
                                            ) {
                                                val colAreaWidth = maxWidth
                                                placedEvents.forEach { placed ->
                                                    val occ = placed.occurrence
                                                    val event = occ.event
                                                    val clampedStart = occ.spanStartMinutes.coerceIn(startHour * 60, endHour * 60)
                                                    val clampedEnd = occ.spanEndMinutes.coerceIn(clampedStart + 30, (endHour + 1) * 60)
                                                    val topOffsetDp = hourHeightDp * ((clampedStart - startHour * 60) / 60f)
                                                    val blockHeightDp = (hourHeightDp * ((clampedEnd - clampedStart) / 60f))
                                                        .coerceAtLeast(32.dp)

                                                    val itemWidth = colAreaWidth / placed.columnCount
                                                    val itemOffsetX = itemWidth * placed.columnIndex

                                                    val container = ext.eventContainer(event)
                                                    val textColor = ext.onEventContainer(event)

                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = container,
                                                        modifier = Modifier
                                                            .offset(x = itemOffsetX, y = topOffsetDp)
                                                            .width((itemWidth - 1.dp).coerceAtLeast(16.dp))
                                                            .height(blockHeightDp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .clickable { onOpenEventDetails(event.id, occ.occurrenceEpochDay) }
                                                            .testTag("week_event_${event.id}")
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 3.dp),
                                                            verticalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                text = event.title,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = ext.textPrimary,
                                                                maxLines = 2,
                                                                overflow = TextOverflow.Ellipsis,
                                                                lineHeight = 11.sp,
                                                                fontSize = 9.sp
                                                            )
                                                            Text(
                                                                text = KronozaDate.formatMinutesCompact(
                                                                    occ.spanStartMinutes,
                                                                    preferences.timeFormat24h
                                                                ),
                                                                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                                                color = textColor,
                                                                fontSize = 8.sp,
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Current time indicator line if today is in this week
                                    if (today in weekDays) {
                                        val clampedNow = nowMinutes.coerceIn(startHour * 60, endHour * 60)
                                        val nowOffsetDp = hourHeightDp * ((clampedNow - startHour * 60) / 60f)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .offset(y = nowOffsetDp - 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(1.5.dp)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Legend
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeekLegendItem("Meetings", ext.lavenderAccent)
                    WeekLegendItem("Deep Work", ext.skyBlueAccent)
                    WeekLegendItem("Wellness", ext.mintAccent)
                    WeekLegendItem("Deadlines", ext.coralAccent)
                }
            }
        }
    }
}

@Composable
private fun WeekLegendItem(label: String, color: Color) {
    val ext = LocalKronozaColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ext.textSecondary
        )
    }
}
