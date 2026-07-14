package com.vocab300.app.domain

import com.vocab300.app.data.local.entity.VocabularyWordEntity
import kotlin.random.Random

/**
 * Sinh danh sách câu hỏi (10-15 câu) cho một bài học, phân bổ đều 5 dạng câu hỏi,
 * xáo trộn thứ tự câu hỏi và xáo trộn vị trí đáp án đúng trong các lựa chọn.
 *
 * [allWordsSamePool] là toàn bộ từ CÙNG NGÔN NGỮ với bài học, dùng để lấy đáp án nhiễu (distractor)
 * và các cặp để ghép trong dạng MATCHING.
 */
object QuestionGenerator {

    private const val NUM_OPTIONS = 4
    private const val MATCHING_PAIR_COUNT = 4

    fun generate(
        lessonWords: List<VocabularyWordEntity>,
        allWordsSamePool: List<VocabularyWordEntity>,
        random: Random = Random.Default
    ): List<Question> {
        if (lessonWords.isEmpty()) return emptyList()

        val distractorPool = allWordsSamePool.filter { pool -> lessonWords.none { it.id == pool.id } }
            .ifEmpty { allWordsSamePool }

        val questions = mutableListOf<Question>()
        val types = QuestionType.entries.filter { it != QuestionType.MATCHING }

        lessonWords.forEachIndexed { index, word ->
            // Phân bổ tuần hoàn 4 dạng câu hỏi đơn (không phải matching) cho từng từ
            when (types[index % types.size]) {
                QuestionType.WORD_TO_MEANING -> questions.add(
                    buildChoiceQuestion(
                        word = word,
                        distractorPool = distractorPool,
                        random = random,
                        type = QuestionType.WORD_TO_MEANING,
                        prompt = word.word,
                        correctAnswer = word.meaningVi,
                        optionSelector = { it.meaningVi }
                    )
                )

                QuestionType.MEANING_TO_WORD -> questions.add(
                    buildChoiceQuestion(
                        word = word,
                        distractorPool = distractorPool,
                        random = random,
                        type = QuestionType.MEANING_TO_WORD,
                        prompt = word.meaningVi,
                        correctAnswer = word.word,
                        optionSelector = { it.word }
                    )
                )

                QuestionType.TYPE_ANSWER -> questions.add(
                    Question(
                        id = "${word.id}_type",
                        wordId = word.id,
                        type = QuestionType.TYPE_ANSWER,
                        prompt = word.meaningVi,
                        correctAnswer = word.word,
                        ttsText = word.word,
                        ttsLanguage = runCatching { Language.valueOf(word.language) }.getOrNull()
                    )
                )

                QuestionType.LISTENING -> questions.add(
                    buildChoiceQuestion(
                        word = word,
                        distractorPool = distractorPool,
                        random = random,
                        type = QuestionType.LISTENING,
                        prompt = word.word,
                        correctAnswer = word.meaningVi,
                        optionSelector = { it.meaningVi },
                        ttsText = word.word,
                        ttsLanguage = runCatching { Language.valueOf(word.language) }.getOrNull()
                    )
                )

                QuestionType.MATCHING -> Unit // không rơi vào nhánh này do đã lọc ở trên
            }
        }

        // Thêm 1 câu MATCHING nếu bài có đủ từ (>= 4), ghép 4 cặp từ trong chính bài học
        if (lessonWords.size >= MATCHING_PAIR_COUNT) {
            val pairWords = lessonWords.shuffled(random).take(MATCHING_PAIR_COUNT)
            questions.add(
                Question(
                    id = "matching_${lessonWords.first().id}",
                    wordId = pairWords.first().id,
                    type = QuestionType.MATCHING,
                    prompt = "Ghép từ với nghĩa tương ứng",
                    correctAnswer = "", // đúng/sai của MATCHING được xác định khi ghép hết cặp, không so 1 giá trị
                    matchingPairs = pairWords.map { it.word to it.meaningVi }
                )
            )
        }

        return questions.shuffled(random)
    }

    private fun buildChoiceQuestion(
        word: VocabularyWordEntity,
        distractorPool: List<VocabularyWordEntity>,
        random: Random,
        type: QuestionType,
        prompt: String,
        correctAnswer: String,
        optionSelector: (VocabularyWordEntity) -> String,
        ttsText: String? = null,
        ttsLanguage: Language? = null
    ): Question {
        val distractors = distractorPool
            .shuffled(random)
            .map(optionSelector)
            .filter { it != correctAnswer }
            .distinct()
            .take(NUM_OPTIONS - 1)

        val options = (distractors + correctAnswer).shuffled(random)

        return Question(
            id = "${word.id}_${type.name}",
            wordId = word.id,
            type = type,
            prompt = prompt,
            options = options,
            correctAnswer = correctAnswer,
            ttsText = ttsText,
            ttsLanguage = ttsLanguage
        )
    }
}
