package com.vocab300.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonUnlockCheckerTest {

    @Test
    fun `bai dau tien luon mo khoa bat ke diem bai truoc`() {
        assertTrue(LessonUnlockChecker.isUnlocked(lessonIndex = 0, previousLessonBestPercent = 0))
    }

    @Test
    fun `bai thu 2 mo khoa khi bai truoc dat toi thieu 80 phan tram`() {
        assertTrue(LessonUnlockChecker.isUnlocked(lessonIndex = 1, previousLessonBestPercent = 80))
        assertTrue(LessonUnlockChecker.isUnlocked(lessonIndex = 1, previousLessonBestPercent = 90))
    }

    @Test
    fun `bai thu 2 khong mo khoa khi bai truoc duoi 80 phan tram`() {
        assertFalse(LessonUnlockChecker.isUnlocked(lessonIndex = 1, previousLessonBestPercent = 79))
        assertFalse(LessonUnlockChecker.isUnlocked(lessonIndex = 1, previousLessonBestPercent = 0))
    }

    @Test
    fun `buildLessonNodes danh dau dung trang thai locked in_progress completed`() {
        val lessons = LessonPlanner.buildLessons(Language.EN, (1..30).map { "EN%03d".format(it) })
        // Bài 0 đã hoàn thành 90%, bài 1 chưa học, bài 2 chưa mở khoá
        val scores = mapOf(0 to 90)
        val completed = setOf(0)

        val nodes = LessonUnlockChecker.buildLessonNodes(lessons, scores, completed)

        assertEquals(LessonNodeState.COMPLETED, nodes[0].state)
        assertEquals(LessonNodeState.IN_PROGRESS, nodes[1].state)
        assertEquals(LessonNodeState.LOCKED, nodes[2].state)
    }

    @Test
    fun `buildLessonNodes khong mo khoa bai 3 neu bai 2 chua dat 80 phan tram`() {
        val lessons = LessonPlanner.buildLessons(Language.EN, (1..30).map { "EN%03d".format(it) })
        val scores = mapOf(0 to 100, 1 to 50) // bài 1 học rồi nhưng chỉ đạt 50%
        val completed = setOf(0)

        val nodes = LessonUnlockChecker.buildLessonNodes(lessons, scores, completed)

        assertEquals(LessonNodeState.LOCKED, nodes[2].state)
    }
}
