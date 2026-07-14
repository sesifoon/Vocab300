package com.vocab300.app.domain

import com.vocab300.app.data.local.entity.VocabularyWordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuestionGeneratorTest {

    private fun word(id: String, word: String, meaning: String) = VocabularyWordEntity(
        id = id, language = "EN", word = word, meaningVi = meaning,
        pronunciation = null, example = null, category = "Test", imageKey = null,
        sequenceOrder = id.filter { it.isDigit() }.toInt()
    )

    private val pool = (1..20).map { word("EN%03d".format(it), "word$it", "nghia$it") }

    @Test
    fun `sinh du so cau hoi cho 10 tu, co them 1 cau matching`() {
        val lessonWords = pool.take(10)
        val questions = QuestionGenerator.generate(lessonWords, pool, random = Random(42))

        // 10 câu đơn (1 mỗi từ) + 1 câu matching = 11
        assertEquals(11, questions.size)
        assertTrue(questions.any { it.type == QuestionType.MATCHING })
    }

    @Test
    fun `moi cau trac nghiem co dung 4 lua chon va chua dap an dung`() {
        val lessonWords = pool.take(10)
        val questions = QuestionGenerator.generate(lessonWords, pool, random = Random(1))

        questions.filter { it.type != QuestionType.MATCHING && it.type != QuestionType.TYPE_ANSWER }
            .forEach { q ->
                assertEquals(4, q.options.size)
                assertTrue(q.options.contains(q.correctAnswer))
            }
    }

    @Test
    fun `danh sach tu rong thi khong sinh cau hoi nao`() {
        val questions = QuestionGenerator.generate(emptyList(), pool)
        assertTrue(questions.isEmpty())
    }

    @Test
    fun `cau matching co dung 4 cap tu-nghia`() {
        val lessonWords = pool.take(10)
        val questions = QuestionGenerator.generate(lessonWords, pool, random = Random(7))
        val matching = questions.first { it.type == QuestionType.MATCHING }
        assertEquals(4, matching.matchingPairs.size)
    }
}
