package com.vocab300.app.domain

import com.vocab300.app.data.local.entity.WordProgressEntity

/**
 * Thuật toán ôn tập đơn giản (không dùng spaced-repetition phức tạp):
 * - Từ sai nhiều được ưu tiên cao nhất.
 * - Từ đã đến hạn ôn (nextReviewAt <= now) hoặc lâu chưa học được ưu tiên.
 * - Từ có mức thành thạo cao (4-5) và chưa đến hạn thì ít được chọn hơn.
 * - Từ được đánh dấu "đã thuộc" (isKnown) bị loại trừ trừ khi không còn từ nào khác để ôn.
 */
object ReviewSelector {

    fun selectForReview(
        allProgress: List<WordProgressEntity>,
        nowMillis: Long,
        limit: Int
    ): List<WordProgressEntity> {
        if (allProgress.isEmpty() || limit <= 0) return emptyList()

        val candidates = allProgress.filter { !it.isKnown }
            .ifEmpty { allProgress } // nếu tất cả đã đánh dấu thuộc, vẫn cho phép ôn lại toàn bộ

        val scored = candidates.map { progress -> progress to priorityScore(progress, nowMillis) }
        return scored
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    private fun priorityScore(progress: WordProgressEntity, nowMillis: Long): Int {
        var score = 0

        // Chưa học lần nào -> ưu tiên thấp hơn từ đã sai (những từ này thuộc phạm vi bài học mới, không phải ôn tập)
        if (progress.lastReviewedAt == null) return -1000

        // Từ sai nhiều lần được ưu tiên rất cao
        score += progress.timesWrong * 15

        // Từ đã đến hạn ôn (hoặc quá hạn) được cộng điểm ưu tiên, càng quá hạn lâu càng ưu tiên
        val nextReview = progress.nextReviewAt
        if (nextReview != null) {
            if (nextReview <= nowMillis) {
                val overdueDays = ((nowMillis - nextReview) / (24 * 60 * 60 * 1000L)).coerceAtLeast(0)
                score += 20 + overdueDays.toInt().coerceAtMost(30)
            } else {
                score -= 10 // chưa đến hạn, giảm ưu tiên
            }
        }

        // Mức thành thạo càng thấp càng ưu tiên; càng cao càng ít ưu tiên
        score += (MasteryUpdater.MAX_MASTERY - progress.masteryLevel) * 5

        return score
    }
}
