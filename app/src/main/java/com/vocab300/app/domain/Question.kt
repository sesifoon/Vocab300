package com.vocab300.app.domain

enum class QuestionType {
    WORD_TO_MEANING,  // Hiển thị từ, chọn nghĩa đúng
    MEANING_TO_WORD,  // Hiển thị nghĩa tiếng Việt, chọn từ đúng
    TYPE_ANSWER,      // Nhập từ bằng bàn phím
    MATCHING,         // Ghép cặp từ với nghĩa (nhiều cặp trong 1 câu)
    LISTENING         // Nghe phát âm, chọn đáp án đúng
}

/**
 * Một câu hỏi trong bài học.
 * - [options] dùng cho trắc nghiệm (WORD_TO_MEANING, MEANING_TO_WORD, LISTENING), đã được xáo trộn.
 * - [correctAnswer] là đáp án đúng (giá trị text để so khớp, không phụ thuộc vị trí trong options).
 * - [matchingPairs] chỉ dùng cho MATCHING: danh sách (từ, nghĩa) cần ghép.
 * - [attemptCount] tăng dần mỗi lần câu hỏi này được đưa ra (dùng để biết đây có phải lần đầu không).
 */
data class Question(
    val id: String,
    val wordId: String,
    val type: QuestionType,
    val prompt: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val matchingPairs: List<Pair<String, String>> = emptyList(),
    val ttsText: String? = null,
    val ttsLanguage: Language? = null,
    val attemptCount: Int = 0
)
