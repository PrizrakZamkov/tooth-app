package com.zubki.app.data

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class SlotForNowTest {

    private val both = listOf(Slot.Morning, Slot.Evening)

    @Test
    fun morningBeforeThreePm() {
        assertEquals(Slot.Morning, slotForNow(both, LocalTime.of(9, 0)))
    }

    @Test
    fun eveningFromThreePm() {
        assertEquals(Slot.Evening, slotForNow(both, LocalTime.of(15, 0)))
    }

    @Test
    fun onlyMorningEnabledAlwaysMorning() {
        assertEquals(Slot.Morning, slotForNow(listOf(Slot.Morning), LocalTime.of(21, 30)))
    }

    @Test
    fun onlyEveningEnabledAlwaysEvening() {
        assertEquals(Slot.Evening, slotForNow(listOf(Slot.Evening), LocalTime.of(7, 30)))
    }
}
