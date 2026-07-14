package com.vocab300.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonSessionTest {

    private fun question(id: String) = Question(
        id = id, wordId = id, type = QuestionType.WORD_TO_MEANING,
        prompt = "prompt-$id", options = listOf("a", "b"), correctAnswer = "a"
    )

    @Test
    fun `tra loi dung het thi phien hoan thanh va tinh du correctFirstTry`() {
        val session = LessonSession(listOf(question("q1"), question("q2"), question("q3")))

        session.submitAnswer(true)
        session.submitAnswer(true)
        session.submitAnswer(true)

        assertTrue(session.isFinished)
        assertEquals(3, session.correctFirstTry)
    }

    @Test
    fun `cau tra loi sai duoc dua xuong cuoi hang doi de hoi lai`() {
        val session = LessonSession(listOf(question("q1"), question("q2")))

        assertEquals("q1", session.currentQuestion()?.id)
        session.submitAnswer(false) // q1 sai -> đưa xuống cuối

        // Bây giờ câu hiện tại phải là q2, không phải q1 lặp lại ngay
        assertEquals("q2", session.currentQuestion()?.id)
        session.submitAnswer(true) // q2 đúng lần đầu

        // q1 phải xuất hiện lại ở cuối vì đã sai trước đó
        assertEquals("q1", session.currentQuestion()?.id)
        assertFalse(session.isFinished)

        session.submitAnswer(true) // q1 trả lời lại đúng -> kết thúc
        assertTrue(session.isFinished)
    }

    @Test
    fun `cau da sai roi tra loi dung lai khong duoc tinh vao correctFirstTry`() {
        val session = LessonSession(listOf(question("q1"), question("q2")))

        session.submitAnswer(false) // q1 sai lần đầu
        session.submitAnswer(true)  // q2 đúng lần đầu -> +1
        session.submitAnswer(true)  // q1 đúng ở lần thứ 2 -> KHÔNG cộng vào correctFirstTry

        assertEquals(1, session.correctFirstTry) // chỉ q2 được tính
        assertTrue(session.isFinished)
    }

    @Test
    fun `wordIdsAnsweredWrong ghi nhan dung cac tu da tra loi sai`() {
        val session = LessonSession(listOf(question("q1"), question("q2"), question("q3")))
        session.submitAnswer(false) // q1 sai
        session.submitAnswer(true)  // q2 đúng
        session.submitAnswer(true)  // q1 đúng lại (đã bị đẩy xuống cuối)
        session.submitAnswer(true)  // q3 đúng

        assertEquals(setOf("q1"), session.wordIdsAnsweredWrong)
    }

    @Test
    fun `tong so cau hoi duy nhat khong doi du co requeue`() {
        val session = LessonSession(listOf(question("q1"), question("q2")))
        assertEquals(2, session.totalQuestions)
        session.submitAnswer(false)
        assertEquals(2, session.totalQuestions) // không đổi dù hàng đợi thực tế dài hơn tạm thời
    }
}
