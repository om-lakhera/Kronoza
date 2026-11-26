package com.omlakhera.kronoza.util

import androidx.compose.ui.graphics.Color
import com.omlakhera.kronoza.data.local.EventEntity
import com.omlakhera.kronoza.model.DateFormatOption
import com.omlakhera.kronoza.model.EventCategory
import com.omlakhera.kronoza.model.RepeatFrequency
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.floor

/**
 * API-24-compatible calendar & timezone-aware date/time engine backed by Gregorian math and java.util.Calendar.
 */
data class KronozaDate(
    val year: Int,
    val month: Int, // 1..12
    val dayOfMonth: Int // 1..31
) : Comparable<KronozaDate> {

    val epochDay: Long
        get() {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(year, month - 1, dayOfMonth, 0, 0, 0)
            }
            return floor(cal.timeInMillis / 86_400_000.0).toLong()
        }

    /** 1 = Monday .. 7 = Sunday */
    val dayOfWeek: Int
        get() {
            val cal = toCalendar(TimeZone.getTimeZone("UTC"))
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                else -> 7
            }
        }

    val weekOfYear: Int
        get() = toCalendar(TimeZone.getDefault()).apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
        }.get(Calendar.WEEK_OF_YEAR)

    fun toCalendar(timeZone: TimeZone = TimeZone.getDefault()): Calendar =
        Calendar.getInstance(timeZone).apply {
            clear()
            set(year, month - 1, dayOfMonth, 0, 0, 0)
        }

    fun plusDays(days: Int): KronozaDate {
        return fromEpochDay(epochDay + days)
    }

    /**
     * Adds [months] (positive or negative) and clamps [dayOfMonth] cleanly to the target month's length
     * (for example: Jan 31 + 1 month -> Feb 28 or Feb 29 on leap years).
     */
    fun plusMonths(months: Int): KronozaDate {
        val totalZeroBasedMonths = (year * 12 + (month - 1)) + months
        val targetYear = floor(totalZeroBasedMonths / 12.0).toInt()
        val targetMonthZeroBased = ((totalZeroBasedMonths % 12) + 12) % 12
        val targetMonth = targetMonthZeroBased + 1
        val maxDay = daysInMonth(targetYear, targetMonth)
        return KronozaDate(targetYear, targetMonth, dayOfMonth.coerceIn(1, maxDay))
    }

    fun startOfWeek(startMonday: Boolean): KronozaDate {
        val offset = if (startMonday) dayOfWeek - 1 else dayOfWeek % 7
        return plusDays(-offset)
    }

    fun daysUntil(other: KronozaDate): Long = other.epochDay - this.epochDay

    fun epochMillisAt(
        minutesFromMidnight: Int,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long {
        val clamped = minutesFromMidnight.coerceIn(0, 1439)
        val hour = clamped / 60
        val minute = clamped % 60
        return Calendar.getInstance(timeZone).apply {
            clear()
            set(year, month - 1, dayOfMonth, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun monthName(): String = MONTH_NAMES[(month - 1).coerceIn(0, 11)]

    fun shortMonthName(): String = SHORT_MONTH_NAMES[(month - 1).coerceIn(0, 11)]

    fun weekdayName(): String = WEEKDAY_NAMES[(dayOfWeek - 1).coerceIn(0, 6)]

    fun shortWeekdayName(): String = SHORT_WEEKDAY_NAMES[(dayOfWeek - 1).coerceIn(0, 6)]

    fun formatMonthYear(): String = "${monthName()} $year"

    fun formatShortDate(): String = "${shortMonthName()} $dayOfMonth"

    fun formatWeekdayShortDate(): String = "${shortWeekdayName()}, ${shortMonthName()} $dayOfMonth"

    fun formatFullDate(): String = "${weekdayName()}, ${shortMonthName()} $dayOfMonth"

    fun formatGreetingDate(): String = "${weekdayName()}, ${monthName()} $dayOfMonth"

    fun formatWithPreference(option: DateFormatOption): String = when (option) {
        DateFormatOption.MMM_D_YYYY -> "${shortMonthName()} $dayOfMonth, $year"
        DateFormatOption.DD_MM_YYYY -> String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month, year)
        DateFormatOption.MM_DD_YYYY -> String.format(Locale.US, "%02d/%02d/%04d", month, dayOfMonth, year)
    }

    override fun compareTo(other: KronozaDate): Int = epochDay.compareTo(other.epochDay)

    companion object {
        private val MONTH_NAMES = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        private val SHORT_MONTH_NAMES = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        private val WEEKDAY_NAMES = listOf(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
        )
        private val SHORT_WEEKDAY_NAMES = listOf(
            "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
        )

        fun isLeapYear(year: Int): Boolean {
            return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        }

        fun daysInMonth(year: Int, month: Int): Int = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }

        fun today(timeZone: TimeZone = TimeZone.getDefault()): KronozaDate =
            fromCalendar(Calendar.getInstance(timeZone))

        fun fromCalendar(cal: Calendar): KronozaDate = KronozaDate(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1,
            dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        )

        fun fromEpochMillis(
            millis: Long,
            timeZone: TimeZone = TimeZone.getDefault()
        ): Pair<KronozaDate, Int> {
            val cal = Calendar.getInstance(timeZone).apply { timeInMillis = millis }
            val date = fromCalendar(cal)
            val minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            return date to minutes
        }

        fun fromEpochDay(epochDay: Long): KronozaDate {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = epochDay * 86_400_000L
            }
            return KronozaDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun currentTimeMinutes(timeZone: TimeZone = TimeZone.getDefault()): Int {
            val cal = Calendar.getInstance(timeZone)
            return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        }

        fun formatMinutes12h(totalMinutes: Int): String {
            val clamped = ((totalMinutes % 1440) + 1440) % 1440
            val hour24 = clamped / 60
            val minute = clamped % 60
            val amPm = if (hour24 < 12) "AM" else "PM"
            val hour12 = when {
                hour24 == 0 -> 12
                hour24 > 12 -> hour24 - 12
                else -> hour24
            }
            return String.format(Locale.US, "%02d:%02d %s", hour12, minute, amPm)
        }

        fun formatMinutes24h(totalMinutes: Int): String {
            val clamped = ((totalMinutes % 1440) + 1440) % 1440
            val hour24 = clamped / 60
            val minute = clamped % 60
            return String.format(Locale.US, "%02d:%02d", hour24, minute)
        }

        fun formatTime(totalMinutes: Int, use24h: Boolean): String =
            if (use24h) formatMinutes24h(totalMinutes) else formatMinutes12h(totalMinutes)

        fun formatTimeRange(startMinutes: Int, endMinutes: Int, use24h: Boolean): String =
            "${formatTime(startMinutes, use24h)} – ${formatTime(endMinutes, use24h)}"

        fun formatMinutesCompact(totalMinutes: Int, use24h: Boolean = false): String {
            val clamped = ((totalMinutes % 1440) + 1440) % 1440
            val hour24 = clamped / 60
            val minute = clamped % 60
            if (use24h) {
                return String.format(Locale.US, "%02d:%02d", hour24, minute)
            }
            val suffix = if (hour24 < 12) "a" else "p"
            val hour12 = when {
                hour24 == 0 -> 12
                hour24 > 12 -> hour24 - 12
                else -> hour24
            }
            return String.format(Locale.US, "%d:%02d%s", hour12, minute, suffix)
        }

        fun formatHourLabel24(hour: Int): String = String.format(Locale.US, "%02d:00", hour)

        fun formatHourLabel12(hour: Int): String = when {
            hour == 0 -> "12 AM"
            hour < 12 -> "$hour AM"
            hour == 12 -> "12 PM"
            else -> "${hour - 12} PM"
        }

        fun formatHourLabel(hour: Int, use24h: Boolean): String =
            if (use24h) formatHourLabel24(hour) else formatHourLabel12(hour)

        fun localTimezoneOffsetLabel(
            timeZone: TimeZone = TimeZone.getDefault(),
            atMillis: Long = System.currentTimeMillis()
        ): String {
            val offsetMinutes = timeZone.getOffset(atMillis) / 60_000
            val sign = if (offsetMinutes >= 0) "+" else "-"
            val absMins = kotlin.math.abs(offsetMinutes)
            val hours = absMins / 60
            val mins = absMins % 60
            return if (mins == 0) "GMT$sign$hours" else String.format(Locale.US, "GMT%s%d:%02d", sign, hours, mins)
        }

        fun localTimezoneSummary(timeZone: TimeZone = TimeZone.getDefault()): String {
            return "${timeZone.id} (${localTimezoneOffsetLabel(timeZone)})"
        }

        fun buildMonthGrid(year: Int, month: Int, startMonday: Boolean): List<KronozaDate> {
            val firstOfMonth = KronozaDate(year, month, 1)
            val start = firstOfMonth.startOfWeek(startMonday)
            return List(42) { index -> start.plusDays(index) }
        }
    }
}

/**
 * Represents a materialized event occurrence on a specific [occurrenceEpochDay].
 */
data class EventOccurrence(
    val event: EventEntity,
    val occurrenceEpochDay: Long,
    val spanStartMinutes: Int,
    val spanEndMinutes: Int,
    val isRecurringInstance: Boolean,
    val isMultiDayContinuation: Boolean
)

data class TimelinePlacedEvent(
    val occurrence: EventOccurrence,
    val columnIndex: Int,
    val columnCount: Int
)

object CalendarEngine {

    /**
     * Expands multi-day and recurring events into concrete day occurrences within [startEpochDay..endEpochDay].
     */
    fun expandEventsInRange(
        events: List<EventEntity>,
        startEpochDay: Long,
        endEpochDay: Long
    ): List<EventOccurrence> {
        if (endEpochDay < startEpochDay) return emptyList()
        val results = mutableListOf<EventOccurrence>()
        for (event in events) {
            val effectiveEndDay = maxOf(event.dateEpochDay, event.endDateEpochDay)
            val spanDays = (effectiveEndDay - event.dateEpochDay).toInt().coerceIn(0, 60)
            val excluded = event.excludedEpochDays.toSet()

            if (event.repeatFrequency == RepeatFrequency.NONE || spanDays > 0) {
                for (day in maxOf(startEpochDay, event.dateEpochDay)..minOf(endEpochDay, effectiveEndDay)) {
                    if (day in excluded) continue
                    val dayStartMins = when {
                        event.allDay -> 0
                        day == event.dateEpochDay -> event.startMinutes
                        else -> 0
                    }
                    val dayEndMins = when {
                        event.allDay -> 24 * 60
                        day == effectiveEndDay -> event.endMinutes
                        else -> 24 * 60
                    }
                    results += EventOccurrence(
                        event = event,
                        occurrenceEpochDay = day,
                        spanStartMinutes = dayStartMins,
                        spanEndMinutes = maxOf(dayStartMins + 15, dayEndMins),
                        isRecurringInstance = false,
                        isMultiDayContinuation = day > event.dateEpochDay
                    )
                }
            } else {
                when (event.repeatFrequency) {
                    RepeatFrequency.DAILY -> {
                        val firstDay = maxOf(startEpochDay, event.dateEpochDay)
                        for (day in firstDay..endEpochDay) {
                            if (day in excluded) continue
                            results += EventOccurrence(
                                event = event,
                                occurrenceEpochDay = day,
                                spanStartMinutes = if (event.allDay) 0 else event.startMinutes,
                                spanEndMinutes = if (event.allDay) 24 * 60 else maxOf(event.startMinutes + 15, event.endMinutes),
                                isRecurringInstance = day > event.dateEpochDay,
                                isMultiDayContinuation = false
                            )
                        }
                    }
                    RepeatFrequency.WEEKLY -> {
                        var day = event.dateEpochDay
                        if (day < startEpochDay) {
                            val diff = startEpochDay - day
                            val weeksSkip = (diff + 6) / 7
                            day += weeksSkip * 7
                        }
                        while (day <= endEpochDay) {
                            if (day !in excluded) {
                                results += EventOccurrence(
                                    event = event,
                                    occurrenceEpochDay = day,
                                    spanStartMinutes = if (event.allDay) 0 else event.startMinutes,
                                    spanEndMinutes = if (event.allDay) 24 * 60 else maxOf(event.startMinutes + 15, event.endMinutes),
                                    isRecurringInstance = day > event.dateEpochDay,
                                    isMultiDayContinuation = false
                                )
                            }
                            day += 7
                        }
                    }
                    RepeatFrequency.MONTHLY -> {
                        val anchor = KronozaDate.fromEpochDay(event.dateEpochDay)
                        var monthOffset = 0
                        var cursor = anchor
                        while (cursor.epochDay <= endEpochDay && monthOffset <= 240) {
                            if (cursor.epochDay >= startEpochDay && cursor.epochDay !in excluded) {
                                results += EventOccurrence(
                                    event = event,
                                    occurrenceEpochDay = cursor.epochDay,
                                    spanStartMinutes = if (event.allDay) 0 else event.startMinutes,
                                    spanEndMinutes = if (event.allDay) 24 * 60 else maxOf(event.startMinutes + 15, event.endMinutes),
                                    isRecurringInstance = cursor.epochDay > event.dateEpochDay,
                                    isMultiDayContinuation = false
                                )
                            }
                            monthOffset++
                            cursor = anchor.plusMonths(monthOffset)
                        }
                    }
                    RepeatFrequency.NONE -> Unit
                }
            }
        }
        return results.sortedWith(
            compareBy({ it.occurrenceEpochDay }, { !it.event.allDay }, { it.spanStartMinutes }, { it.event.title })
        )
    }

    fun occurrencesOnDate(events: List<EventEntity>, epochDay: Long): List<EventOccurrence> =
        expandEventsInRange(events, epochDay, epochDay)

    /**
     * Sweep-line interval partitioning algorithm to lay out overlapping events side-by-side in Day and Week timelines.
     */
    fun layoutOverlappingEvents(occurrences: List<EventOccurrence>): List<TimelinePlacedEvent> {
        val timed = occurrences.filter { !it.event.allDay }
            .sortedWith(compareBy({ it.spanStartMinutes }, { -it.spanEndMinutes }))
        if (timed.isEmpty()) return emptyList()

        // Group into connected overlapping clusters
        val clusters = mutableListOf<MutableList<EventOccurrence>>()
        var currentCluster = mutableListOf<EventOccurrence>()
        var clusterEnd = -1

        for (occ in timed) {
            val effectiveEnd = maxOf(occ.spanStartMinutes + 30, occ.spanEndMinutes)
            if (currentCluster.isEmpty() || occ.spanStartMinutes < clusterEnd) {
                currentCluster.add(occ)
                clusterEnd = maxOf(clusterEnd, effectiveEnd)
            } else {
                clusters.add(currentCluster)
                currentCluster = mutableListOf(occ)
                clusterEnd = effectiveEnd
            }
        }
        if (currentCluster.isNotEmpty()) clusters.add(currentCluster)

        val placed = mutableListOf<TimelinePlacedEvent>()
        for (cluster in clusters) {
            val columnEnds = mutableListOf<Int>()
            val assigned = mutableListOf<Pair<EventOccurrence, Int>>()
            for (occ in cluster) {
                val effectiveEnd = maxOf(occ.spanStartMinutes + 30, occ.spanEndMinutes)
                var col = columnEnds.indexOfFirst { it <= occ.spanStartMinutes }
                if (col == -1) {
                    col = columnEnds.size
                    columnEnds.add(effectiveEnd)
                } else {
                    columnEnds[col] = effectiveEnd
                }
                assigned.add(occ to col)
            }
            val totalCols = columnEnds.size.coerceAtLeast(1)
            for ((occ, col) in assigned) {
                placed += TimelinePlacedEvent(
                    occurrence = occ,
                    columnIndex = col,
                    columnCount = totalCols
                )
            }
        }
        return placed
    }

    /**
     * Finds existing events that overlap with the candidate event's date & time range.
     */
    fun findConflictingEvents(
        candidateDateEpochDay: Long,
        candidateEndDateEpochDay: Long,
        candidateStartMinutes: Int,
        candidateEndMinutes: Int,
        candidateAllDay: Boolean,
        existingEvents: List<EventEntity>,
        excludeEventId: Long? = null
    ): List<EventEntity> {
        if (candidateAllDay) return emptyList()
        val endDay = maxOf(candidateDateEpochDay, candidateEndDateEpochDay)
        val expanded = expandEventsInRange(existingEvents, candidateDateEpochDay, endDay)
        return expanded.filter { occ ->
            occ.event.id != excludeEventId &&
                !occ.event.allDay &&
                occ.occurrenceEpochDay in candidateDateEpochDay..endDay &&
                occ.spanStartMinutes < candidateEndMinutes &&
                occ.spanEndMinutes > candidateStartMinutes
        }.map { it.event }.distinctBy { it.id }
    }

    fun nextOccurrenceEpochDay(event: EventEntity, afterEpochDay: Long): Long? {
        if (event.repeatFrequency == RepeatFrequency.NONE) return null
        val start = maxOf(event.dateEpochDay, afterEpochDay + 1)
        val horizon = start + 400L
        return expandEventsInRange(listOf(event), start, horizon)
            .firstOrNull { it.occurrenceEpochDay > afterEpochDay }
            ?.occurrenceEpochDay
    }

    fun parseHexColorOrNull(hex: String): Color? {
        if (hex.isBlank()) return null
        return runCatching {
            val cleaned = hex.removePrefix("#")
            val parsed = cleaned.toLong(16)
            when (cleaned.length) {
                6 -> Color(0xFF000000L or parsed)
                8 -> Color(parsed)
                else -> null
            }
        }.getOrNull()
    }
}
