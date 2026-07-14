package com.vocab300.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringEngineTest {

    @Test
    fun `10 tren 10 cau dung dat 100 phan tram va passed`() {
        val result = ScoringEngine.calculateResult(totalQuestions = 10, correctFirstTry = 10, timeSpentMillis = 60_000)
        assertEquals(100, result.scorePercent)
        assertTrue(result.passed)
        assertEquals(100, result.xpEarned)
    }

    @Test
    fun `8 tren 10 cau dung dat dung 80 phan tram va passed (nguong toi thieu)`() {
        val result = ScoringEngine.calculateResult(totalQuestions = 10, correctFirstTry = 8, timeSpentMillis = 60_000)
        assertEquals(80, result.scorePercent)
        assertTrue(result.passed)
    }

    @Test
    fun `7 tren 10 cau dung la 70 phan tram va khong passed`() {
        val result = ScoringEngine.calculateResult(totalQuestions = 10, correctFirstTry = 7, timeSpentMillis = 60_000)
        assertEquals(70, result.scorePercent)
        assertFalse(result.passed)
    }

    @Test
    fun `xp tinh dung bang so cau dung nhan xp moi cau`() {
        val result = ScoringEngine.calculateResult(
            totalQuestions = 12, correctFirstTry = 9, timeSpentMillis = 1000, xpPerCorrect = 10
        )
        assertEquals(90, result.xpEarned)
    }

    @Test
    fun `correctFirstTry vuot qua totalQuestions bi gioi han lai`() {
        val result = ScoringEngine.calculateResult(totalQuestions = 10, correctFirstTry = 15, timeSpentMillis = 1000)
        assertEquals(10, result.correctFirstTry)
        assertEquals(100, result.scorePercent)
    }

    @Test
    fun `0 cau dung la 0 phan tram va khong passed`() {
        val result = ScoringEngine.calculateResult(totalQuestions = 10, correctFirstTry = 0, timeSpentMillis = 1000)
        assertEquals(0, result.scorePercent)
        assertFalse(result.passed)
    }
}
