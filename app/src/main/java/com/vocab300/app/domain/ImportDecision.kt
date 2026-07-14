package com.vocab300.app.domain

/**
 * Quyết định có nên chạy import dữ liệu từ assets vào Room hay không.
 * Tách riêng logic thuần này để unit test được mà không cần Android runtime (DataStore/Room thật).
 *
 * Quy tắc: chỉ import khi CẢ HAI đều chưa sẵn sàng - tức là chưa từng đánh dấu import xong
 * VÀ số từ hiện có trong DB ít hơn số từ kỳ vọng. Nếu đã import rồi (cờ true) thì tuyệt đối
 * không import lại, kể cả khi mở app nhiều lần.
 */
object ImportDecision {

    fun shouldImport(importFlagAlreadySet: Boolean, currentWordCountInDb: Int, expectedWordCount: Int): Boolean {
        if (importFlagAlreadySet) return false
        return currentWordCountInDb < expectedWordCount
    }
}
