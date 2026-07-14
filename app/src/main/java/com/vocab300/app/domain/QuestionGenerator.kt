package com.vocab300.app.domain

import com.vocab300.app.data.local.entity.VocabularyWordEntity
import kotlin.random.Random

/**
 * Sinh danh sách câu hỏi cho một bài học / phiên ôn tập.
 *
 * v1.1.0: không còn round-robin cố định qua 4 loại câu hỏi. Dạng bài được chọn cho từng từ
 * qua [ExerciseSelector], dựa trên [EntryClassifier] (loại mục từ: mã viết tắt / từ ngắn / từ dài /
 * cụm trung bình / cụm dài), mastery hiện tại, và TTS có khả dụng hay không.
 *
 * [allWordsSamePool] là toàn bộ từ CÙNG NGÔN NGỮ với bài học, dùng để lấy đáp án nhiễu (distractor)
 * và các cặp để ghép trong dạng MATCHING. Không giả định luôn đủ 4 lựa chọn — nếu thiếu distractor
 * hợp lệ, số lựa chọn sẽ giảm xuống tối thiểu 2 thay vì crash hoặc lặp đáp án.
 */
object QuestionGenerator {

    private const val MAX_OPTIONS = 4
    private const val MIN_OPTIONS = 2
    private const val MATCHING_PAIR_COUNT = 4

    /**
     * @param masteryByWordId mức thành thạo hiện tại của từng từ (0-5); từ không có trong map coi như mastery 0.
     * @param ttsAvailableForLanguage cho biết TTS có khả dụng cho một ngôn ngữ cụ thể hay không, để loại
     *   dạng LISTENING khi không phát âm được thay vì tạo câu hỏi không thể trả lời.
     */
    fun generate(
        lessonWords: List<VocabularyWordEntity>,
        allWordsSamePool: List<VocabularyWordEntity>,
        masteryByWordId: Map<String, Int> = emptyMap(),
        ttsAvailableForLanguage: (Language) -> Boolean = { true },
        random: Random = Random.Default
    ): List<Question> {
        if (lessonWords.isEmpty()) return emptyList()

        val distractorPool = allWordsSamePool.filter { pool -> lessonWords.none { it.id == pool.id } }
            .ifEmpty { allWordsSamePool }

        val questions = mutableListOf<Question>()
        val recentTypes = mutableListOf<QuestionType>()

        for (word in lessonWords) {
            val language = runCatching { Language.valueOf(word.language) }.getOrNull()
            val ttsAvailable = language?.let(ttsAvailableForLanguage) ?: false
            val entryType = EntryClassifier.classify(word.word)
            val mastery = masteryByWordId[word.id] ?: 0

            val type = ExerciseSelector.selectType(
                entryType = entryType,
                ttsAvailable = ttsAvailable,
                masteryLevel = mastery,
                recentTypes = recentTypes.takeLast(2),
                excludeTypes = setOf(QuestionType.MATCHING),
                random = random
            )
            recentTypes.add(type)

            val question = buildQuestion(word, type, distractorPool, entryType, random)
            if (question != null) questions.add(question)
        }

        // Thêm 1 câu MATCHING nếu bài có đủ từ (>= 4), ghép tối đa 4 cặp từ trong chính bài học.
        // Không giả định luôn đủ 4 cặp — nếu bài có ít từ hơn, dùng đúng số từ hiện có (tối thiểu 2 cặp).
        if (lessonWords.size >= MIN_OPTIONS) {
            val pairCount = minOf(MATCHING_PAIR_COUNT, lessonWords.size)
            val pairWords = lessonWords.shuffled(random).take(pairCount)
            questions.add(
                Question(
                    id = "matching_${lessonWords.first().id}",
                    wordId = pairWords.first().id,
                    type = QuestionType.MATCHING,
                    prompt = "Ghép từ với nghĩa tương ứng",
                    correctAnswer = "",
                    matchingPairs = pairWords.map { it.word to it.meaningVi }
                )
            )
        }

        return avoidAdjacentSameWord(questions.shuffled(random), random)
    }

    /** Không lặp cùng một từ liên tiếp nếu còn câu hỏi của từ khác phù hợp để hoán đổi. */
    private fun avoidAdjacentSameWord(list: List<Question>, random: Random): List<Question> {
        if (list.size < 2) return list
        val result = list.toMutableList()
        for (i in 1 until result.size) {
            if (result[i].wordId == result[i - 1].wordId) {
                val swapIndex = (i + 1 until result.size).firstOrNull { result[it].wordId != result[i - 1].wordId }
                if (swapIndex != null) {
                    val tmp = result[i]
                    result[i] = result[swapIndex]
                    result[swapIndex] = tmp
                }
            }
        }
        return result
    }

    private fun buildQuestion(
        word: VocabularyWordEntity,
        type: QuestionType,
        distractorPool: List<VocabularyWordEntity>,
        entryType: EntryType,
        random: Random
    ): Question? {
        val language = runCatching { Language.valueOf(word.language) }.getOrNull()

        return when (type) {
            QuestionType.WORD_TO_MEANING -> buildChoiceQuestion(
                word, distractorPool, random, type,
                prompt = word.word, correctAnswer = word.meaningVi,
                optionSelector = { it.meaningVi }, entryType = entryType
            )

            QuestionType.MEANING_TO_WORD -> buildChoiceQuestion(
                word, distractorPool, random, type,
                prompt = word.meaningVi, correctAnswer = word.word,
                optionSelector = { it.word }, entryType = entryType
            )

            QuestionType.LISTENING -> buildChoiceQuestion(
                word, distractorPool, random, type,
                prompt = word.word, correctAnswer = word.meaningVi,
                optionSelector = { it.meaningVi }, entryType = entryType,
                ttsText = word.word, ttsLanguage = language
            )

            QuestionType.TYPE_ANSWER -> Question(
                id = "${word.id}_type",
                wordId = word.id,
                type = QuestionType.TYPE_ANSWER,
                prompt = word.meaningVi,
                correctAnswer = word.word,
                ttsText = word.word,
                ttsLanguage = language
            )

            QuestionType.MATCHING -> null // MATCHING được tạo riêng ở generate(), không tạo qua nhánh này
        }
    }

    private fun buildChoiceQuestion(
        word: VocabularyWordEntity,
        distractorPool: List<VocabularyWordEntity>,
        random: Random,
        type: QuestionType,
        prompt: String,
        correctAnswer: String,
        optionSelector: (VocabularyWordEntity) -> String,
        entryType: EntryType,
        ttsText: String? = null,
        ttsLanguage: Language? = null
    ): Question {
        // Ưu tiên distractor cùng loại mục từ (VD: mã đi với mã, cụm dài đi với cụm dài) và cùng category
        // trước khi lấy đại trà, để đáp án nhiễu có độ khó/hình thức tương đồng thay vì lộ đáp án quá dễ.
        val sameTypeSameCategory = distractorPool.filter {
            EntryClassifier.classify(it.word) == entryType && it.category == word.category
        }
        val sameType = distractorPool.filter { EntryClassifier.classify(it.word) == entryType }
        val rest = distractorPool

        val orderedPool = (sameTypeSameCategory.shuffled(random) +
            sameType.shuffled(random) +
            rest.shuffled(random)).distinctBy { it.id }

        val distractors = orderedPool
            .map(optionSelector)
            .filter { it != correctAnswer }
            .distinct()
            .take(MAX_OPTIONS - 1)

        // Không giả định luôn đủ 4 đáp án: nếu thiếu distractor hợp lệ, giảm số lựa chọn (tối thiểu 2 tổng
        // cộng, tức chỉ cần >= 1 distractor). Nếu 0 distractor khả dụng, phần lựa chọn sẽ rỗng và UI cần
        // tự xử lý; QuestionGenerator đảm bảo distractorPool không rỗng ở generate() nên trường hợp này hiếm.
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
