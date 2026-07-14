package com.vocab300.app.domain

import com.vocab300.app.data.local.entity.VocabularyWordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Kiểm tra domain logic (LessonPlanner, QuestionGenerator) hoạt động đúng khi quy mô dữ liệu
 * tăng lên 1.000 bản ghi GIẢ LẬP — KHÔNG liên quan tới bộ dữ liệu production 300 từ thật.
 * Mục tiêu: xác nhận không có chỗ nào phụ thuộc cố định vào con số 300.
 */
class ScaleTest {

    private fun fakeWord(index: Int, language: Language): VocabularyWordEntity {
        val prefix = language.name
        return VocabularyWordEntity(
            id = "$prefix-FAKE-%04d".format(index),
            language = language.name,
            word = "word_$index",
            meaningVi = "nghĩa giả lập số $index",
            pronunciation = null,
            example = null,
            category = "Test",
            imageKey = null,
            sequenceOrder = index
        )
    }

    private val fakeThousand: List<VocabularyWordEntity> =
        (1..500).map { fakeWord(it, Language.EN) } + (1..500).map { fakeWord(it, Language.KO) }

    @Test
    fun `1000 ban ghi gia lap chia dung 100 bai moi ngon ngu`() {
        val enWords = fakeThousand.filter { it.language == "EN" }
        val koWords = fakeThousand.filter { it.language == "KO" }

        val enLessons = LessonPlanner.buildLessons(Language.EN, enWords.map { it.id })
        val koLessons = LessonPlanner.buildLessons(Language.KO, koWords.map { it.id })

        assertEquals(50, enLessons.size) // 500 từ / 10 = 50 bài
        assertEquals(50, koLessons.size)
        enLessons.forEach { assertEquals(10, it.wordIds.size) }
    }

    @Test
    fun `tat ca 1000 id la duy nhat, khong trung du danh cho 2 ngon ngu`() {
        val ids = fakeThousand.map { it.id }
        assertEquals(1000, ids.toSet().size)
    }

    @Test
    fun `QuestionGenerator khong crash va sinh du cau hoi voi pool 500 tu`() {
        val enWords = fakeThousand.filter { it.language == "EN" }
        val lessonWords = enWords.take(10)

        val questions = QuestionGenerator.generate(
            lessonWords = lessonWords,
            allWordsSamePool = enWords,
            random = Random(99)
        )

        // 10 câu đơn + 1 matching = 11, không crash dù pool có 500 từ
        assertEquals(11, questions.size)
    }

    @Test
    fun `dap an trac nghiem khong trung nhau du pool lon`() {
        val enWords = fakeThousand.filter { it.language == "EN" }
        val lessonWords = enWords.take(10)
        val questions = QuestionGenerator.generate(lessonWords, enWords, random = Random(123))

        questions.filter { it.type != QuestionType.MATCHING && it.options.isNotEmpty() }
            .forEach { q ->
                assertEquals(q.options.size, q.options.distinct().size) // không có lựa chọn trùng
                assertTrue(q.options.contains(q.correctAnswer))
            }
    }

    @Test
    fun `category it tu (chi 2 tu) khong lam QuestionGenerator crash`() {
        val tinyPool = fakeThousand.filter { it.language == "EN" }.take(2)
        val questions = QuestionGenerator.generate(tinyPool, tinyPool, random = Random(1))

        assertFalse(questions.isEmpty())
        // Với chỉ 2 từ, câu trắc nghiệm sẽ có ít hơn 4 lựa chọn (không đủ distractor) nhưng không crash.
        questions.forEach { q ->
            if (q.type != QuestionType.MATCHING && q.type != QuestionType.TYPE_ANSWER) {
                assertTrue(q.options.size in 1..4)
            }
        }
    }

    @Test
    fun `ReviewSelector hoat dong dung voi 1000 progress gia lap, khong chia cho 0`() {
        val now = System.currentTimeMillis()
        val progresses = (1..1000).map {
            com.vocab300.app.data.local.entity.WordProgressEntity(
                wordId = "id-$it",
                masteryLevel = it % 6,
                lastReviewedAt = now,
                nextReviewAt = now - it * 1000L
            )
        }
        val result = ReviewSelector.selectForReview(progresses, now, limit = 15)
        assertEquals(15, result.size)
    }
}
