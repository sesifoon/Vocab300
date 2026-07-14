package com.vocab300.app.domain

import kotlin.random.Random

/**
 * Chọn dạng bài tập (QuestionType) tập trung — thay cho việc round-robin cố định trước v1.1.0.
 *
 * Nguyên tắc (spec mục V):
 * - Mastery thấp (0-1): ưu tiên Multiple choice / Match pairs / nhận biết nghĩa.
 * - Mastery trung bình (2-3): ưu tiên Listening / điền từ (Typing có hỗ trợ).
 * - Mastery cao (4-5): có thể dùng Typing / recall chủ động.
 * - Không tạo Listening nếu TTS không khả dụng.
 * - Không tạo Typing toàn bộ cho cụm dài ở mastery thấp (thực ra: không bao giờ Typing cụm dài,
 *   xem [allowedTypes] — cụm dài không bắt gõ toàn bộ trong chế độ học thông thường).
 * - Không lặp cùng 1 dạng bài quá nhiều lần liên tiếp khi còn lựa chọn khác phù hợp.
 * - Luôn trả về ít nhất 1 dạng hợp lệ, không bao giờ crash vì danh sách rỗng.
 */
object ExerciseSelector {

    /**
     * Danh sách dạng bài được phép cho 1 mục từ, theo [EntryType] + trạng thái TTS + mastery.
     * Luôn trả về danh sách KHÔNG RỖNG (fallback về WORD_TO_MEANING nếu mọi điều kiện đều loại hết).
     */
    fun allowedTypes(
        entryType: EntryType,
        ttsAvailable: Boolean,
        masteryLevel: Int
    ): List<QuestionType> {
        var types = when (entryType) {
            EntryType.ABBREVIATION -> listOf(
                QuestionType.WORD_TO_MEANING,
                QuestionType.MEANING_TO_WORD,
                QuestionType.TYPE_ANSWER,
                QuestionType.MATCHING
            )

            EntryType.SHORT_WORD -> listOf(
                QuestionType.WORD_TO_MEANING,
                QuestionType.MEANING_TO_WORD,
                QuestionType.TYPE_ANSWER,
                QuestionType.LISTENING,
                QuestionType.MATCHING
            )

            EntryType.LONG_WORD -> listOf(
                QuestionType.WORD_TO_MEANING,
                QuestionType.MEANING_TO_WORD,
                QuestionType.LISTENING,
                QuestionType.MATCHING,
                QuestionType.TYPE_ANSWER // vẫn cho phép, AnswerValidator sẽ chấm NEARLY_CORRECT nếu lệch 1 ký tự
            )

            EntryType.MEDIUM_PHRASE -> {
                val base = mutableListOf(
                    QuestionType.WORD_TO_MEANING,
                    QuestionType.MEANING_TO_WORD,
                    QuestionType.MATCHING,
                    QuestionType.LISTENING
                )
                // "Chỉ yêu cầu nhập đầy đủ khi mastery đủ cao"
                if (masteryLevel >= 4) base.add(QuestionType.TYPE_ANSWER)
                base
            }

            EntryType.LONG_PHRASE -> listOf(
                // "Không yêu cầu người dùng nhập toàn bộ cụm nghĩa dài trong chế độ học thông thường"
                // -> không có TYPE_ANSWER ở đây theo mặc định.
                QuestionType.WORD_TO_MEANING,
                QuestionType.MEANING_TO_WORD,
                QuestionType.MATCHING,
                QuestionType.LISTENING
            )
        }

        if (!ttsAvailable) {
            types = types.filter { it != QuestionType.LISTENING }
        }

        return types.ifEmpty { listOf(QuestionType.WORD_TO_MEANING) }
    }

    /**
     * Chọn 1 dạng bài cụ thể cho 1 từ, có xét tới dạng vừa xuất hiện gần nhất để tránh lặp liên tiếp
     * khi còn lựa chọn khác phù hợp. Không bao giờ throw / trả về null.
     */
    fun selectType(
        entryType: EntryType,
        ttsAvailable: Boolean,
        masteryLevel: Int,
        recentTypes: List<QuestionType>,
        excludeTypes: Set<QuestionType> = emptySet(),
        random: Random = Random.Default
    ): QuestionType {
        val allowed = allowedTypes(entryType, ttsAvailable, masteryLevel)
            .filter { it !in excludeTypes }
            .ifEmpty { allowedTypes(entryType, ttsAvailable, masteryLevel) } // không để rỗng nếu exclude hết

        val lastType = recentTypes.lastOrNull()
        val candidates = if (lastType != null && allowed.size > 1) {
            allowed.filter { it != lastType }.ifEmpty { allowed }
        } else {
            allowed
        }

        val weighted = candidates.flatMap { type -> List(weightFor(type, masteryLevel)) { type } }
        return weighted.ifEmpty { candidates }.random(random)
    }

    /** Trọng số ưu tiên theo mastery — số lần lặp lại trong danh sách trọng số (weighted random đơn giản). */
    private fun weightFor(type: QuestionType, masteryLevel: Int): Int = when {
        masteryLevel <= 1 -> when (type) {
            QuestionType.WORD_TO_MEANING, QuestionType.MEANING_TO_WORD, QuestionType.MATCHING -> 3
            QuestionType.LISTENING -> 1
            QuestionType.TYPE_ANSWER -> 1
        }
        masteryLevel <= 3 -> when (type) {
            QuestionType.LISTENING -> 3
            QuestionType.TYPE_ANSWER -> 2
            QuestionType.WORD_TO_MEANING, QuestionType.MEANING_TO_WORD, QuestionType.MATCHING -> 2
        }
        else -> when (type) {
            QuestionType.TYPE_ANSWER -> 3
            QuestionType.LISTENING -> 2
            QuestionType.WORD_TO_MEANING, QuestionType.MEANING_TO_WORD, QuestionType.MATCHING -> 1
        }
    }
}
