package com.vocab300.app.domain

/**
 * Phân loại một mục từ (dựa trên trường `word`) để quyết định dạng bài phù hợp và cách chấm điểm.
 * Bộ dữ liệu 300 từ hiện tại (kỹ thuật/kiểm thử) chủ yếu rơi vào SHORT_WORD và MEDIUM_PHRASE/LONG_PHRASE
 * (không có mã viết tắt độc lập nào), nhưng ABBREVIATION được chuẩn bị sẵn cho các bộ dữ liệu tương lai
 * (ví dụ mã nghiệp vụ như AUT, OQC, RF).
 */
enum class EntryType {
    ABBREVIATION,   // 2-6 ký tự, toàn chữ in hoa, không khoảng trắng (VD: AUT, OQC, RF)
    SHORT_WORD,     // 1 từ, tối đa 12 ký tự
    LONG_WORD,      // 1 từ, trên 12 ký tự
    MEDIUM_PHRASE,  // 2-4 từ
    LONG_PHRASE     // từ 5 từ trở lên, hoặc dài trên 30 ký tự
}
