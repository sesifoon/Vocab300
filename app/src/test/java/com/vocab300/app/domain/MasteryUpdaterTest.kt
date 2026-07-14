package com.vocab300.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class MasteryUpdaterTest {

    @Test
    fun `tra loi dung tang mastery len 1`() {
        assertEquals(1, MasteryUpdater.nextMasteryLevel(currentMastery = 0, isCorrect = true))
        assertEquals(3, MasteryUpdater.nextMasteryLevel(currentMastery = 2, isCorrect = true))
    }

    @Test
    fun `mastery khong vuot qua muc toi da 5`() {
        assertEquals(5, MasteryUpdater.nextMasteryLevel(currentMastery = 5, isCorrect = true))
        assertEquals(5, MasteryUpdater.nextMasteryLevel(currentMastery = 4, isCorrect = true))
    }

    @Test
    fun `tra loi sai giam mastery 2 muc`() {
        assertEquals(1, MasteryUpdater.nextMasteryLevel(currentMastery = 3, isCorrect = false))
        assertEquals(3, MasteryUpdater.nextMasteryLevel(currentMastery = 5, isCorrect = false))
    }

    @Test
    fun `mastery khong xuong duoi 0`() {
        assertEquals(0, MasteryUpdater.nextMasteryLevel(currentMastery = 0, isCorrect = false))
        assertEquals(0, MasteryUpdater.nextMasteryLevel(currentMastery = 1, isCorrect = false))
    }

    @Test
    fun `ngay on tap tiep theo tang dan theo muc mastery`() {
        val now = 1_000_000L
        val oneDayMs = 24 * 60 * 60 * 1000L

        assertEquals(now, MasteryUpdater.nextReviewTimestamp(0, now)) // mức 0 -> ôn ngay hôm nay
        assertEquals(now + oneDayMs, MasteryUpdater.nextReviewTimestamp(1, now))
        assertEquals(now + 14 * oneDayMs, MasteryUpdater.nextReviewTimestamp(5, now))
    }
}
