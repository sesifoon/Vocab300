package com.vocab300.app.domain

import java.util.concurrent.TimeUnit

/**
 * Cập nhật mức độ thành thạo (0..5) của một từ dựa trên kết quả trả lời đúng/sai,
 * và tính ngày cần ôn lại tiếp theo theo mức thành thạo mới (lặp lại ngắt quãng đơn giản).
 */
object MasteryUpdater {

    const val MIN_MASTERY = 0
    const val MAX_MASTERY = 5

    /** Số ngày tới lần ôn tiếp theo, ứng với từng mức thành thạo 0..5. */
    private val REVIEW_INTERVAL_DAYS = listOf(0, 1, 2, 4, 7, 14)

    fun nextMasteryLevel(currentMastery: Int, isCorrect: Boolean): Int {
        val clampedCurrent = currentMastery.coerceIn(MIN_MASTERY, MAX_MASTERY)
        return if (isCorrect) {
            (clampedCurrent + 1).coerceAtMost(MAX_MASTERY)
        } else {
            (clampedCurrent - 2).coerceAtLeast(MIN_MASTERY)
        }
    }

    /**
     * Phiên bản mới hỗ trợ 3 trạng thái (v1.1.0). NEARLY_CORRECT không phạt và không tăng mastery
     * (nhẹ hơn hẳn so với CORRECT), theo đúng yêu cầu "cập nhật mastery nhẹ hơn đáp án đúng hoàn toàn".
     */
    fun nextMasteryLevel(currentMastery: Int, result: AnswerResult): Int {
        val clampedCurrent = currentMastery.coerceIn(MIN_MASTERY, MAX_MASTERY)
        return when (result) {
            AnswerResult.CORRECT -> (clampedCurrent + 1).coerceAtMost(MAX_MASTERY)
            AnswerResult.NEARLY_CORRECT -> clampedCurrent
            AnswerResult.INCORRECT -> (clampedCurrent - 2).coerceAtLeast(MIN_MASTERY)
        }
    }

    fun nextReviewTimestamp(newMasteryLevel: Int, nowMillis: Long): Long {
        val level = newMasteryLevel.coerceIn(MIN_MASTERY, MAX_MASTERY)
        val days = REVIEW_INTERVAL_DAYS[level]
        return nowMillis + TimeUnit.DAYS.toMillis(days.toLong())
    }
}
