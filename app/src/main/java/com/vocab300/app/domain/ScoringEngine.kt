package com.vocab300.app.domain

import kotlin.math.roundToInt

/**
 * Tính điểm số, XP và kết quả đạt/không đạt của một bài học.
 * Điểm số dựa trên số câu trả lời ĐÚNG NGAY LẦN ĐẦU / tổng số câu hỏi duy nhất trong bài
 * (không tính các lần trả lời lại của câu đã sai, để tránh làm sai lệch điểm số).
 */
object ScoringEngine {

    const val XP_PER_CORRECT = 10

    fun calculateResult(
        totalQuestions: Int,
        correctFirstTry: Int,
        timeSpentMillis: Long,
        xpPerCorrect: Int = XP_PER_CORRECT,
        nearlyCorrectFirstTry: Int = 0
    ): LessonResult {
        require(totalQuestions > 0) { "totalQuestions phải > 0" }
        val clampedCorrect = correctFirstTry.coerceIn(0, totalQuestions)
        val clampedNearly = nearlyCorrectFirstTry.coerceIn(0, totalQuestions - clampedCorrect)
        // NEARLY_CORRECT tính nửa trọng số — "nhẹ hơn" đáp án đúng hoàn toàn, nhưng vẫn được ghi nhận.
        val weightedScore = clampedCorrect + (clampedNearly * 0.5)
        val percent = ((weightedScore / totalQuestions) * 100).roundToInt()
        return LessonResult(
            totalQuestions = totalQuestions,
            correctFirstTry = clampedCorrect,
            xpEarned = clampedCorrect * xpPerCorrect,
            timeSpentMillis = timeSpentMillis,
            scorePercent = percent,
            passed = percent >= LessonUnlockChecker.PASS_THRESHOLD_PERCENT
        )
    }
}
