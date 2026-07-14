package com.vocab300.app.domain

/**
 * Chấm đáp án tập trung — thay cho việc từng màn hình tự so sánh chuỗi.
 *
 * Quy tắc theo [EntryType]:
 * - ABBREVIATION: phải đúng TOÀN BỘ ký tự sau chuẩn hóa, KHÔNG áp dụng gần đúng
 *   (AUT và AOT là 2 mã khác nhau, dù chỉ lệch 1 ký tự).
 * - SHORT_WORD: yêu cầu chính xác sau chuẩn hóa.
 * - LONG_WORD: cho phép lệch tối đa 1 ký tự → NEARLY_CORRECT.
 * - MEDIUM_PHRASE / LONG_PHRASE: mặc định không bắt gõ toàn bộ (xem QuestionGenerator/ExerciseSelector);
 *   nếu vẫn có input để chấm (chế độ nâng cao trong tương lai), dùng ngưỡng tỉ lệ từ đúng ~80%.
 */
object AnswerValidator {

    private const val ADVANCED_PHRASE_MATCH_THRESHOLD = 0.8

    fun validate(userInput: String, correctAnswer: String, entryType: EntryType): AnswerResult {
        val normalizedInput = normalize(userInput)
        val normalizedCorrect = normalize(correctAnswer)

        if (normalizedInput == normalizedCorrect) return AnswerResult.CORRECT
        if (normalizedInput.isBlank()) return AnswerResult.INCORRECT

        return when (entryType) {
            EntryType.ABBREVIATION -> AnswerResult.INCORRECT // không có gần đúng cho mã viết tắt
            EntryType.SHORT_WORD -> AnswerResult.INCORRECT
            EntryType.LONG_WORD -> {
                if (levenshtein(normalizedInput, normalizedCorrect) <= 1) {
                    AnswerResult.NEARLY_CORRECT
                } else {
                    AnswerResult.INCORRECT
                }
            }
            EntryType.MEDIUM_PHRASE, EntryType.LONG_PHRASE -> validatePhrase(normalizedInput, normalizedCorrect)
        }
    }

    /** Dùng cho trắc nghiệm/ghép cặp: chỉ có đúng hoặc sai, không có gần đúng. */
    fun validateChoice(selected: String, correctAnswer: String): AnswerResult {
        return if (normalize(selected) == normalize(correctAnswer)) AnswerResult.CORRECT else AnswerResult.INCORRECT
    }

    private fun validatePhrase(normalizedInput: String, normalizedCorrect: String): AnswerResult {
        val inputWords = normalizedInput.split(" ").filter { it.isNotBlank() }.toSet()
        val correctWords = normalizedCorrect.split(" ").filter { it.isNotBlank() }.toSet()
        if (correctWords.isEmpty()) return AnswerResult.INCORRECT

        val matched = correctWords.count { it in inputWords }
        val ratio = matched.toDouble() / correctWords.size
        return if (ratio >= ADVANCED_PHRASE_MATCH_THRESHOLD) AnswerResult.NEARLY_CORRECT else AnswerResult.INCORRECT
    }

    /**
     * Chuẩn hóa: trim, gộp khoảng trắng, hạ chữ thường, bỏ dấu câu không quan trọng.
     * Không bỏ dấu tiếng Việt/Hàn (chỉ bỏ dấu câu ASCII thông dụng) để tránh làm sai lệch nghĩa.
     */
    fun normalize(text: String): String {
        return text
            .trim()
            .lowercase()
            .replace(Regex("[.,!?;:'\"()\\[\\]]"), "")
            .replace(Regex("\\s+"), " ")
    }

    /** Khoảng cách chỉnh sửa Levenshtein — dùng để cho phép lệch tối đa 1 ký tự ở từ đơn dài. */
    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)

        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                curr[j] = if (a[i - 1] == b[j - 1]) {
                    prev[j - 1]
                } else {
                    1 + minOf(prev[j - 1], prev[j], curr[j - 1])
                }
            }
            for (j in 0..b.length) prev[j] = curr[j]
        }
        return prev[b.length]
    }
}
