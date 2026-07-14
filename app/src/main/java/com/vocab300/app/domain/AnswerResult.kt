package com.vocab300.app.domain

/**
 * Kết quả chấm một câu trả lời. Thay thế hoàn toàn cho Boolean isCorrect kể từ v1.1.0
 * để hỗ trợ "gần đúng" cho từ/cụm dài dễ gõ sai 1 ký tự.
 */
enum class AnswerResult {
    CORRECT,
    NEARLY_CORRECT,
    INCORRECT
}
