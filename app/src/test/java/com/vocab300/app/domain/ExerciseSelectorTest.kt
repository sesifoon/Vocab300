package com.vocab300.app.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ExerciseSelectorTest {

    @Test
    fun `tu ngan van co Typing trong danh sach cho phep`() {
        val allowed = ExerciseSelector.allowedTypes(EntryType.SHORT_WORD, ttsAvailable = true, masteryLevel = 3)
        assertTrue(QuestionType.TYPE_ANSWER in allowed)
    }

    @Test
    fun `cum dai KHONG co Typing o moi muc mastery (khong bat go toan bo cum dai o che do mac dinh)`() {
        val allowedLowMastery = ExerciseSelector.allowedTypes(EntryType.LONG_PHRASE, ttsAvailable = true, masteryLevel = 0)
        val allowedHighMastery = ExerciseSelector.allowedTypes(EntryType.LONG_PHRASE, ttsAvailable = true, masteryLevel = 5)
        assertFalse(QuestionType.TYPE_ANSWER in allowedLowMastery)
        assertFalse(QuestionType.TYPE_ANSWER in allowedHighMastery)
    }

    @Test
    fun `cum dai dung duoc Multiple Choice va Match Pairs`() {
        val allowed = ExerciseSelector.allowedTypes(EntryType.LONG_PHRASE, ttsAvailable = true, masteryLevel = 2)
        assertTrue(QuestionType.WORD_TO_MEANING in allowed)
        assertTrue(QuestionType.MEANING_TO_WORD in allowed)
        assertTrue(QuestionType.MATCHING in allowed)
    }

    @Test
    fun `cum trung binh chi co Typing khi mastery du cao`() {
        val lowMastery = ExerciseSelector.allowedTypes(EntryType.MEDIUM_PHRASE, ttsAvailable = true, masteryLevel = 1)
        val highMastery = ExerciseSelector.allowedTypes(EntryType.MEDIUM_PHRASE, ttsAvailable = true, masteryLevel = 4)
        assertFalse(QuestionType.TYPE_ANSWER in lowMastery)
        assertTrue(QuestionType.TYPE_ANSWER in highMastery)
    }

    @Test
    fun `khong tao Listening khi TTS khong kha dung`() {
        val allowed = ExerciseSelector.allowedTypes(EntryType.SHORT_WORD, ttsAvailable = false, masteryLevel = 2)
        assertFalse(QuestionType.LISTENING in allowed)
    }

    @Test
    fun `allowedTypes khong bao gio rong`() {
        // Ngay cả khi loại trừ hết (TTS tắt + entry type hạn chế), vẫn phải còn ít nhất 1 dạng.
        val allowed = ExerciseSelector.allowedTypes(EntryType.LONG_PHRASE, ttsAvailable = false, masteryLevel = 0)
        assertTrue(allowed.isNotEmpty())
    }

    @Test
    fun `selectType khong lap lai dang bai vua dung neu con lua chon khac`() {
        val random = Random(1)
        val recentTypes = listOf(QuestionType.WORD_TO_MEANING)
        // Lặp lại nhiều lần để kiểm tra tính nhất quán của việc tránh lặp (không phải ngẫu nhiên 100% loại trừ)
        repeat(20) {
            val selected = ExerciseSelector.selectType(
                entryType = EntryType.SHORT_WORD,
                ttsAvailable = true,
                masteryLevel = 2,
                recentTypes = recentTypes,
                random = Random(it)
            )
            // Không bắt buộc tuyệt đối khác (fallback cho phép trùng nếu hết lựa chọn), nhưng với SHORT_WORD
            // có 5 dạng hợp lệ nên luôn phải khác WORD_TO_MEANING khi vừa dùng xong.
            assertFalse(selected == QuestionType.WORD_TO_MEANING)
        }
    }

    @Test
    fun `selectType luon nam trong danh sach cho phep va khong crash voi excludeTypes`() {
        val result = ExerciseSelector.selectType(
            entryType = EntryType.SHORT_WORD,
            ttsAvailable = true,
            masteryLevel = 1,
            recentTypes = emptyList(),
            excludeTypes = setOf(QuestionType.MATCHING),
            random = Random(5)
        )
        assertFalse(result == QuestionType.MATCHING)
    }
}
