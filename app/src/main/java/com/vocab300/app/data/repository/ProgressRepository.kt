package com.vocab300.app.data.repository

import com.vocab300.app.data.datastore.UserPrefsManager
import com.vocab300.app.data.datastore.UserStats
import com.vocab300.app.data.local.dao.LessonProgressDao
import com.vocab300.app.data.local.dao.ProgressDao
import com.vocab300.app.data.local.entity.LessonProgressEntity
import com.vocab300.app.data.local.entity.WordProgressEntity
import com.vocab300.app.domain.AnswerResult
import com.vocab300.app.domain.LessonUnlockChecker
import com.vocab300.app.domain.MasteryUpdater
import com.vocab300.app.domain.ReviewSelector
import kotlinx.coroutines.flow.Flow

class ProgressRepository(
    private val progressDao: ProgressDao,
    private val lessonProgressDao: LessonProgressDao,
    private val userPrefsManager: UserPrefsManager
) {
    val userStats: Flow<UserStats> = userPrefsManager.userStats

    fun observeAllWordProgress(): Flow<List<WordProgressEntity>> = progressDao.observeAll()

    fun observeFavorites(): Flow<List<WordProgressEntity>> = progressDao.observeFavorites()

    suspend fun getWordProgress(wordId: String): WordProgressEntity? = progressDao.getByWordId(wordId)

    /** Mức thành thạo hiện tại (0-5) của từng từ trong [wordIds]; từ chưa học không có trong map. */
    suspend fun getMasteryMap(wordIds: List<String>): Map<String, Int> {
        if (wordIds.isEmpty()) return emptyMap()
        return progressDao.getByWordIds(wordIds).associate { it.wordId to it.masteryLevel }
    }

    suspend fun getAllWordProgress(): List<WordProgressEntity> = progressDao.getAll()

    suspend fun getLessonProgress(language: String): List<LessonProgressEntity> =
        lessonProgressDao.getByLanguage(language)

    fun observeLessonProgress(language: String): Flow<List<LessonProgressEntity>> =
        lessonProgressDao.observeByLanguage(language)

    /**
     * Ghi nhận kết quả trả lời cho 1 từ: cập nhật đếm đúng/sai, mức thành thạo, ngày ôn tiếp theo.
     *
     * v1.1.0: KHÔNG còn trừ tim khi sai — Vocab300 đã bỏ hoàn toàn hệ thống tim khỏi trải nghiệm
     * người dùng. Trả lời sai chỉ tăng số lần sai, điều chỉnh mastery và đưa từ vào ôn tập sớm hơn,
     * không có hình phạt nào khác; người dùng luôn được phép tiếp tục.
     */
    suspend fun recordAnswer(wordId: String, result: AnswerResult, nowMillis: Long) {
        val existing = progressDao.getByWordId(wordId)
        val currentMastery = existing?.masteryLevel ?: 0
        val newMastery = MasteryUpdater.nextMasteryLevel(currentMastery, result)
        val nextReview = MasteryUpdater.nextReviewTimestamp(newMastery, nowMillis)
        val isCorrectish = result != AnswerResult.INCORRECT

        val updated = (existing ?: WordProgressEntity(wordId = wordId)).copy(
            timesCorrect = (existing?.timesCorrect ?: 0) + if (isCorrectish) 1 else 0,
            timesWrong = (existing?.timesWrong ?: 0) + if (result == AnswerResult.INCORRECT) 1 else 0,
            masteryLevel = newMastery,
            lastReviewedAt = nowMillis,
            nextReviewAt = nextReview
        )
        progressDao.upsert(updated)

        when (result) {
            AnswerResult.CORRECT -> userPrefsManager.addXp()
            AnswerResult.NEARLY_CORRECT -> userPrefsManager.addXp(amount = UserPrefsManager.XP_PER_CORRECT / 2)
            AnswerResult.INCORRECT -> Unit // không còn trừ tim hay bất kỳ hình phạt nào khác
        }
    }

    /** Giữ lại chữ ký cũ (Boolean) để tương thích ngược với mọi lời gọi chưa cập nhật; ánh xạ sang AnswerResult. */
    @Deprecated("Dùng recordAnswer(wordId, AnswerResult, nowMillis) để hỗ trợ NEARLY_CORRECT.")
    suspend fun recordAnswer(wordId: String, isCorrect: Boolean, nowMillis: Long) {
        recordAnswer(wordId, if (isCorrect) AnswerResult.CORRECT else AnswerResult.INCORRECT, nowMillis)
    }

    suspend fun toggleFavorite(wordId: String) {
        val existing = progressDao.getByWordId(wordId) ?: WordProgressEntity(wordId = wordId)
        progressDao.upsert(existing.copy(isFavorite = !existing.isFavorite))
    }

    suspend fun toggleKnown(wordId: String) {
        val existing = progressDao.getByWordId(wordId) ?: WordProgressEntity(wordId = wordId)
        progressDao.upsert(existing.copy(isKnown = !existing.isKnown, masteryLevel = MasteryUpdater.MAX_MASTERY))
    }

    /** Lưu kết quả bài học (điểm tốt nhất), cập nhật streak nếu đạt, cập nhật bài đang học dở. */
    suspend fun completeLesson(language: String, lessonIndex: Int, scorePercent: Int, nowMillis: Long) {
        val existing = lessonProgressDao.getOne(language, lessonIndex)
        val bestScore = maxOf(existing?.bestScorePercent ?: 0, scorePercent)
        val passed = bestScore >= LessonUnlockChecker.PASS_THRESHOLD_PERCENT

        lessonProgressDao.upsert(
            LessonProgressEntity(
                language = language,
                lessonIndex = lessonIndex,
                bestScorePercent = bestScore,
                isCompleted = passed,
                lastAttemptAt = nowMillis
            )
        )

        userPrefsManager.recordStudyActivityToday()

        if (passed) {
            // Mở khoá bài tiếp theo -> đặt làm bài "đang học" hiện tại
            userPrefsManager.setCurrentLessonIndex(language, lessonIndex + 1)
        } else {
            userPrefsManager.setCurrentLessonIndex(language, lessonIndex)
        }
    }

    suspend fun selectWordsForReview(limit: Int, nowMillis: Long): List<WordProgressEntity> {
        return ReviewSelector.selectForReview(progressDao.getAll(), nowMillis, limit)
    }

    suspend fun resetAllProgress() {
        progressDao.clearAll()
        lessonProgressDao.clearAll()
        userPrefsManager.resetProgressPrefs()
    }
}
