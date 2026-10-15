package com.zubki.app.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakTest {

    private val today = LocalDate.of(2026, 10, 12)

    // Дни отсчитываются назад от сегодняшнего: days(0, 1) — это сегодня и вчера
    private fun days(vararg back: Int): Set<LocalDate> =
        back.map { today.minusDays(it.toLong()) }.toSet()

    @Test
    fun noDaysNoStreak() {
        assertEquals(StreakInfo(0, 0, false), streakInfo(emptySet(), today))
    }

    @Test
    fun consecutiveDaysAddUp() {
        assertEquals(StreakInfo(3, 3, false), streakInfo(days(0, 1, 2), today))
    }

    @Test
    fun unmarkedTodayDoesNotBreakStreak() {
        assertEquals(StreakInfo(3, 3, false), streakInfo(days(1, 2, 3), today))
    }

    @Test
    fun oneMissedDayIsOnlyPause() {
        assertEquals(StreakInfo(3, 3, false), streakInfo(days(0, 2, 3), today))
    }

    @Test
    fun pauseIsShownWhileTodayIsStillOpen() {
        assertEquals(StreakInfo(2, 2, true), streakInfo(days(2, 3), today))
    }

    @Test
    fun twoMissedDaysResetStreak() {
        assertEquals(StreakInfo(1, 2, false), streakInfo(days(0, 3, 4), today))
    }

    @Test
    fun longGapEndsStreakButKeepsBest() {
        assertEquals(StreakInfo(0, 2, false), streakInfo(days(3, 4), today))
    }
}
