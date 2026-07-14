package com.vocab300.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonPlannerTest {

    @Test
    fun `chia 200 tu thanh 20 bai, moi bai 10 tu`() {
        val ids = (1..200).map { "EN%03d".format(it) }
        val lessons = LessonPlanner.buildLessons(Language.EN, ids)

        assertEquals(20, lessons.size)
        lessons.forEach { lesson -> assertEquals(10, lesson.wordIds.size) }
    }

    @Test
    fun `chia 100 tu thanh 10 bai cho tieng Han`() {
        val ids = (1..100).map { "KO%03d".format(it) }
        val lessons = LessonPlanner.buildLessons(Language.KO, ids)

        assertEquals(10, lessons.size)
        lessons.forEach { lesson -> assertEquals(10, lesson.wordIds.size) }
    }

    @Test
    fun `so tu khong chia het thi bai cuoi it hon 10 tu`() {
        val ids = (1..25).map { "EN%03d".format(it) }
        val lessons = LessonPlanner.buildLessons(Language.EN, ids)

        assertEquals(3, lessons.size)
        assertEquals(10, lessons[0].wordIds.size)
        assertEquals(10, lessons[1].wordIds.size)
        assertEquals(5, lessons[2].wordIds.size)
    }

    @Test
    fun `danh sach rong tra ve khong co bai nao`() {
        val lessons = LessonPlanner.buildLessons(Language.EN, emptyList())
        assertTrue(lessons.isEmpty())
    }

    @Test
    fun `lessonIndex tang dan tu 0`() {
        val ids = (1..30).map { "EN%03d".format(it) }
        val lessons = LessonPlanner.buildLessons(Language.EN, ids)
        assertEquals(listOf(0, 1, 2), lessons.map { it.lessonIndex })
    }
}
