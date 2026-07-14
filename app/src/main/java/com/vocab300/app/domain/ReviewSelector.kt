package com.vocab300.app.domain

import com.vocab300.app.data.local.entity.WordProgressEntity

/**
 * Chọn danh sách từ ưu tiên cho phiên Ôn tập, theo đúng thứ tự bậc thang yêu cầu (v1.1.0):
 *
 * 1. Từ đã đến hạn ôn (`nextReviewAt <= now`) — quá hạn càng lâu càng ưu tiên.
 * 2. Nếu vẫn còn chỗ trống (chưa đủ [limit]), bổ sung từ có số lần sai cao nhất (chưa dùng ở bậc 1).
 * 3. Nếu vẫn còn chỗ trống, bổ sung từ có mastery thấp nhất (chưa dùng ở 2 bậc trên).
 * 4. Nếu người dùng CHƯA HỌC từ nào (`lastReviewedAt == null` cho toàn bộ), trả về danh sách RỖNG —
 *    tầng UI (ReviewScreen) chịu trách nhiệm hiển thị thông báo rõ ràng và mời bắt đầu học từ mới,
 *    KHÔNG được hiển thị màn hình trống hoặc màn hình kẹt.
 *
 * Từ được đánh dấu "đã thuộc" (`isKnown`) bị loại trừ trước, trừ khi đó là toàn bộ danh sách đã học
 * (khi đó vẫn cho phép ôn lại để tránh trả về rỗng một cách không cần thiết).
 */
object ReviewSelector {

    fun selectForReview(
        allProgress: List<WordProgressEntity>,
        nowMillis: Long,
        limit: Int
    ): List<WordProgressEntity> {
        if (limit <= 0) return emptyList()

        // Bậc 4: chưa học từ nào -> trả về rỗng, để UI tự quyết định thông báo + hành động.
        val learned = allProgress.filter { it.lastReviewedAt != null }
        if (learned.isEmpty()) return emptyList()

        val candidates = learned.filter { !it.isKnown }.ifEmpty { learned }

        val result = LinkedHashMap<String, WordProgressEntity>()

        // Bậc 1: đã đến hạn ôn, quá hạn lâu nhất trước; đồng hạng thì sai nhiều hơn / mastery thấp hơn trước.
        val due = candidates
            .filter { it.nextReviewAt != null && it.nextReviewAt <= nowMillis }
            .sortedWith(
                compareByDescending<WordProgressEntity> { nowMillis - (it.nextReviewAt ?: nowMillis) }
                    .thenByDescending { it.timesWrong }
                    .thenBy { it.masteryLevel }
            )
        addUpToLimit(result, due, limit)

        // Bậc 2: nếu vẫn thiếu, bổ sung theo số lần sai cao nhất.
        if (result.size < limit) {
            val byWrongCount = candidates
                .filter { it.timesWrong > 0 }
                .sortedByDescending { it.timesWrong }
            addUpToLimit(result, byWrongCount, limit)
        }

        // Bậc 3: nếu vẫn thiếu, bổ sung theo mastery thấp nhất.
        if (result.size < limit) {
            val byLowMastery = candidates.sortedBy { it.masteryLevel }
            addUpToLimit(result, byLowMastery, limit)
        }

        return result.values.toList()
    }

    private fun addUpToLimit(
        result: LinkedHashMap<String, WordProgressEntity>,
        source: List<WordProgressEntity>,
        limit: Int
    ) {
        for (item in source) {
            if (result.size >= limit) return
            result.putIfAbsent(item.wordId, item)
        }
    }
}
