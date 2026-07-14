package com.vocab300.app.domain

import com.vocab300.app.data.local.entity.WordProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewSelectorTest {

    private val now = 1_000_000_000L
    private val oneDayMs = 24 * 60 * 60 * 1000L

    @Test
    fun `tu sai nhieu duoc uu tien cao hon tu chua bao gio sai`() {
        val wrongWord = WordProgressEntity(
            wordId = "w1", timesWrong = 5, timesCorrect = 1, masteryLevel = 1,
            lastReviewedAt = now, nextReviewAt = now - oneDayMs
        )
        val neverWrongWord = WordProgressEntity(
            wordId = "w2", timesWrong = 0, timesCorrect = 5, masteryLevel = 3,
            lastReviewedAt = now, nextReviewAt = now - oneDayMs
        )

        val result = ReviewSelector.selectForReview(listOf(neverWrongWord, wrongWord), now, limit = 2)

        assertEquals("w1", result.first().wordId)
    }

    @Test
    fun `tu qua han on tap lau duoc uu tien hon tu moi qua han`() {
        val longOverdue = WordProgressEntity(
            wordId = "old", masteryLevel = 3, lastReviewedAt = now, nextReviewAt = now - 10 * oneDayMs
        )
        val justDue = WordProgressEntity(
            wordId = "new", masteryLevel = 3, lastReviewedAt = now, nextReviewAt = now - 1000
        )

        val result = ReviewSelector.selectForReview(listOf(justDue, longOverdue), now, limit = 2)

        assertEquals("old", result.first().wordId)
    }

    @Test
    fun `tu thanh thao cao it duoc uu tien hon tu thanh thao thap`() {
        val highMastery = WordProgressEntity(
            wordId = "high", masteryLevel = 5, lastReviewedAt = now, nextReviewAt = now - oneDayMs
        )
        val lowMastery = WordProgressEntity(
            wordId = "low", masteryLevel = 0, lastReviewedAt = now, nextReviewAt = now - oneDayMs
        )

        val result = ReviewSelector.selectForReview(listOf(highMastery, lowMastery), now, limit = 2)

        assertEquals("low", result.first().wordId)
    }

    @Test
    fun `tu chua hoc lan nao khong duoc chon uu tien trong danh sach on tap`() {
        val neverLearned = WordProgressEntity(wordId = "never", lastReviewedAt = null, nextReviewAt = null)
        val learnedDue = WordProgressEntity(
            wordId = "due", masteryLevel = 1, lastReviewedAt = now, nextReviewAt = now - oneDayMs
        )

        val result = ReviewSelector.selectForReview(listOf(neverLearned, learnedDue), now, limit = 1)

        assertEquals("due", result.first().wordId)
    }

    @Test
    fun `gioi han so luong tra ve dung limit`() {
        val words = (1..20).map {
            WordProgressEntity(wordId = "w$it", masteryLevel = 0, lastReviewedAt = now, nextReviewAt = now - oneDayMs)
        }
        val result = ReviewSelector.selectForReview(words, now, limit = 5)
        assertEquals(5, result.size)
    }

    @Test
    fun `danh sach rong tra ve rong`() {
        assertTrue(ReviewSelector.selectForReview(emptyList(), now, limit = 10).isEmpty())
    }

    @Test
    fun `chua hoc tu nao thi tra ve rong de UI tu hien thi thong bao (khong bi ket)`() {
        val neverLearned = listOf(
            WordProgressEntity(wordId = "a", lastReviewedAt = null, nextReviewAt = null),
            WordProgressEntity(wordId = "b", lastReviewedAt = null, nextReviewAt = null)
        )
        assertTrue(ReviewSelector.selectForReview(neverLearned, now, limit = 10).isEmpty())
    }

    @Test
    fun `bac 2 - khong co tu den han thi lay theo so lan sai cao nhat`() {
        val notDueLowWrong = WordProgressEntity(
            wordId = "low-wrong", timesWrong = 1, masteryLevel = 3,
            lastReviewedAt = now, nextReviewAt = now + 5 * oneDayMs // chưa đến hạn
        )
        val notDueHighWrong = WordProgressEntity(
            wordId = "high-wrong", timesWrong = 8, masteryLevel = 3,
            lastReviewedAt = now, nextReviewAt = now + 5 * oneDayMs // chưa đến hạn
        )

        val result = ReviewSelector.selectForReview(listOf(notDueLowWrong, notDueHighWrong), now, limit = 2)

        assertEquals(2, result.size)
        assertEquals("high-wrong", result.first().wordId) // không có từ nào đến hạn -> rơi xuống bậc 2
    }

    @Test
    fun `bac 3 - khong co tu den han va khong co tu sai thi lay theo mastery thap nhat`() {
        val notDueHighMastery = WordProgressEntity(
            wordId = "hm", timesWrong = 0, masteryLevel = 5,
            lastReviewedAt = now, nextReviewAt = now + 5 * oneDayMs
        )
        val notDueLowMastery = WordProgressEntity(
            wordId = "lm", timesWrong = 0, masteryLevel = 1,
            lastReviewedAt = now, nextReviewAt = now + 5 * oneDayMs
        )

        val result = ReviewSelector.selectForReview(listOf(notDueHighMastery, notDueLowMastery), now, limit = 2)

        assertEquals(2, result.size)
        assertEquals("lm", result.first().wordId) // không đến hạn, không có từ sai -> rơi xuống bậc 3
    }

    @Test
    fun `uu tien bac 1 truoc, chi bo sung bac 2 va 3 khi con thieu cho`() {
        val due = WordProgressEntity(
            wordId = "due1", masteryLevel = 3, lastReviewedAt = now, nextReviewAt = now - oneDayMs
        )
        val notDueHighWrong = WordProgressEntity(
            wordId = "wrong1", timesWrong = 10, masteryLevel = 3,
            lastReviewedAt = now, nextReviewAt = now + 5 * oneDayMs
        )

        // limit = 1: đã đủ từ bậc 1 (due1), KHÔNG được lấy thêm từ bậc 2 dù wrong1 có sai rất nhiều.
        val result = ReviewSelector.selectForReview(listOf(due, notDueHighWrong), now, limit = 1)

        assertEquals(1, result.size)
        assertEquals("due1", result.first().wordId)
    }
}
