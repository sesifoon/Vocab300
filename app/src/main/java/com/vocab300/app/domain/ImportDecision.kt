package com.vocab300.app.domain

/**
 * Quyết định có nên chạy import dữ liệu từ assets vào Room hay không.
 * Tách riêng logic thuần này để unit test được mà không cần Android runtime (DataStore/Room thật).
 */
object ImportDecision {

    /**
     * v1.1.0: quyết định dựa trên `sourceVersion` thay vì đếm số từ cố định (300), để hỗ trợ
     * mở rộng dữ liệu lên 1.000 từ hoặc hơn trong tương lai mà không cần sửa logic này.
     * - Chưa từng import (`storedSourceVersion == null`) -> import.
     * - `sourceVersion` trong asset khác với lần import gần nhất -> import lại (insertAll dùng
     *   OnConflictStrategy.IGNORE nên các từ đã có KHÔNG bị ghi đè, chỉ từ MỚI được thêm vào).
     * - Giống nhau -> không import lại, tránh parse JSON mỗi lần mở app.
     */
    fun shouldImport(storedSourceVersion: String?, currentSourceVersion: String): Boolean {
        return storedSourceVersion != currentSourceVersion
    }

    @Deprecated(
        "Dùng shouldImport(storedSourceVersion, currentSourceVersion) — quy tắc dựa trên số lượng cố định " +
            "không còn phù hợp khi bộ dữ liệu có thể tăng lên 1.000+ từ."
    )
    fun shouldImport(importFlagAlreadySet: Boolean, currentWordCountInDb: Int, expectedWordCount: Int): Boolean {
        if (importFlagAlreadySet) return false
        return currentWordCountInDb < expectedWordCount
    }
}
