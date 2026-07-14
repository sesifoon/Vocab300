package com.vocab300.app.domain

/**
 * Phân loại tập trung cho mọi mục từ — thay cho việc rải điều kiện độ dài ở nhiều màn hình.
 * Chỉ dựa trên trường `word` (không phải `meaningVi`); độ dài nghĩa được xét riêng qua [hasLongMeaning]
 * vì một mã viết tắt (VD: AUT) hoàn toàn có thể có nghĩa tiếng Việt rất dài.
 */
object EntryClassifier {

    private const val ABBREVIATION_MIN_LEN = 2
    private const val ABBREVIATION_MAX_LEN = 6
    private const val SHORT_WORD_MAX_LEN = 12
    private const val LONG_PHRASE_MIN_WORDS = 5
    private const val LONG_PHRASE_MIN_CHARS = 30
    private const val LONG_MEANING_MIN_WORDS = 5
    private const val LONG_MEANING_MIN_CHARS = 30

    fun classify(word: String): EntryType {
        val trimmed = word.trim()
        val tokens = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }

        if (tokens.size <= 1) {
            if (isAbbreviation(trimmed)) return EntryType.ABBREVIATION
            return if (trimmed.length > SHORT_WORD_MAX_LEN) EntryType.LONG_WORD else EntryType.SHORT_WORD
        }

        return if (tokens.size >= LONG_PHRASE_MIN_WORDS || trimmed.length > LONG_PHRASE_MIN_CHARS) {
            EntryType.LONG_PHRASE
        } else {
            EntryType.MEDIUM_PHRASE
        }
    }

    private fun isAbbreviation(token: String): Boolean {
        if (token.length !in ABBREVIATION_MIN_LEN..ABBREVIATION_MAX_LEN) return false
        if (!token.any { it.isLetter() }) return false // toàn số/ký hiệu thì không tính là mã viết tắt
        return token == token.uppercase() && token == token.filter { !it.isWhitespace() }
    }

    /** Nghĩa tiếng Việt có dài hay không — dùng để tránh bắt gõ lại toàn bộ nghĩa dài. */
    fun hasLongMeaning(meaningVi: String): Boolean {
        val trimmed = meaningVi.trim()
        val wordCount = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        return wordCount >= LONG_MEANING_MIN_WORDS || trimmed.length > LONG_MEANING_MIN_CHARS
    }
}
