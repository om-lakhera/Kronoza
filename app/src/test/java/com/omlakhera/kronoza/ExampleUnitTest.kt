package com.omlakhera.kronoza

import com.omlakhera.kronoza.util.KronozaDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun dateArithmetic_and_monthGrid_workCorrectly() {
        val date = KronozaDate(2026, 11, 23)
        assertEquals(1, date.dayOfWeek) // Monday
        assertEquals("November 2026", date.formatMonthYear())
        assertEquals(KronozaDate(2026, 11, 30), date.plusDays(7))

        val grid = KronozaDate.buildMonthGrid(2026, 11, startMonday = true)
        assertEquals(42, grid.size)
        assertTrue(grid.contains(date))
        assertEquals(1, grid.first().dayOfWeek)
    }

    @Test
    fun timeFormatting_handles12HourAndCompact() {
        assertEquals("09:30 AM", KronozaDate.formatMinutes12h(9 * 60 + 30))
        assertEquals("02:00 PM", KronozaDate.formatMinutes12h(14 * 60))
        assertEquals("8:30a", KronozaDate.formatMinutesCompact(8 * 60 + 30))
    }
}
