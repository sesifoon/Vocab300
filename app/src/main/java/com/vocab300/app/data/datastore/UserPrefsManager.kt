package com.vocab300.app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

private val Context.dataStore by preferencesDataStore(name = "vocab300_prefs")

enum class DarkModeOption { SYSTEM, LIGHT, DARK }

/**
 * Trạng thái tổng hợp hiển thị ở Home / Stats.
 *
 * v1.1.0: đã bỏ hệ thống tim khỏi trải nghiệm người dùng (xem CHANGELOG.md).
 * `hearts`/`maxHearts` KHÔNG còn nằm trong model này — dữ liệu tim cũ trong DataStore
 * (nếu có từ bản trước) vẫn được giữ nguyên key để tương thích ngược, chỉ không đọc vào đây nữa.
 */
data class UserStats(
    val totalXp: Int,
    val xpToday: Int,
    val streakDays: Int,
    val dailyGoal: Int,
    val soundEnabled: Boolean,
    val darkMode: DarkModeOption,
    val onboardingDone: Boolean,
    val importDone: Boolean
)

/**
 * Quản lý toàn bộ trạng thái người dùng lưu bằng DataStore Preferences:
 * XP, tim, chuỗi ngày học, mục tiêu ngày, cờ đã import dữ liệu, cờ đã onboarding, cài đặt âm thanh/dark mode.
 */
class UserPrefsManager(private val context: Context) {

    private object Keys {
        val IMPORT_DONE = booleanPreferencesKey("import_done")
        val IMPORTED_SOURCE_VERSION = stringPreferencesKey("imported_source_version")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val TOTAL_XP = intPreferencesKey("total_xp")
        val XP_TODAY = intPreferencesKey("xp_today")
        val XP_TODAY_DATE = stringPreferencesKey("xp_today_date")
        val HEARTS = intPreferencesKey("hearts")
        val HEARTS_DATE = stringPreferencesKey("hearts_date")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val LAST_STUDY_DATE = stringPreferencesKey("last_study_date")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val CURRENT_LESSON_EN = intPreferencesKey("current_lesson_en")
        val CURRENT_LESSON_KO = intPreferencesKey("current_lesson_ko")
        val LAST_LANGUAGE = stringPreferencesKey("last_language")
    }

    companion object {
        @Deprecated("Vocab300 v1.1.0 đã bỏ hệ thống tim. Giữ lại chỉ để tương thích ngược dữ liệu cũ.")
        const val MAX_HEARTS = 5
        const val XP_PER_CORRECT = 10
    }

    private fun todayString(): String = LocalDate.now(ZoneId.systemDefault()).toString()

    val userStats: Flow<UserStats> = context.dataStore.data.map { prefs ->
        val today = todayString()
        val xpDate = prefs[Keys.XP_TODAY_DATE]
        val xpToday = if (xpDate == today) prefs[Keys.XP_TODAY] ?: 0 else 0

        UserStats(
            totalXp = prefs[Keys.TOTAL_XP] ?: 0,
            xpToday = xpToday,
            streakDays = prefs[Keys.STREAK_DAYS] ?: 0,
            dailyGoal = prefs[Keys.DAILY_GOAL] ?: 10,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            darkMode = runCatching {
                DarkModeOption.valueOf(prefs[Keys.DARK_MODE] ?: DarkModeOption.SYSTEM.name)
            }.getOrDefault(DarkModeOption.SYSTEM),
            onboardingDone = prefs[Keys.ONBOARDING_DONE] ?: false,
            importDone = prefs[Keys.IMPORT_DONE] ?: false
        )
    }

    @Deprecated("Dùng getImportedSourceVersion()/setImportedSourceVersion() — v1.1.0 chuyển sang so sánh sourceVersion thay vì cờ boolean, để hỗ trợ import bổ sung khi dữ liệu tăng lên.")
    suspend fun isImportDone(): Boolean =
        context.dataStore.data.map { it[Keys.IMPORT_DONE] ?: false }.first()

    @Deprecated("Dùng setImportedSourceVersion(version) thay thế.")
    suspend fun setImportDone() {
        context.dataStore.edit { it[Keys.IMPORT_DONE] = true }
    }

    /** sourceVersion của lần import gần nhất; null nếu chưa từng import (app mới cài lần đầu). */
    suspend fun getImportedSourceVersion(): String? =
        context.dataStore.data.map { it[Keys.IMPORTED_SOURCE_VERSION] }.first()

    suspend fun setImportedSourceVersion(version: String) {
        context.dataStore.edit {
            it[Keys.IMPORTED_SOURCE_VERSION] = version
            it[Keys.IMPORT_DONE] = true // giữ đồng bộ cờ cũ để tương thích ngược với mọi code chưa cập nhật
        }
    }

    suspend fun setOnboardingDone(dailyGoal: Int) {
        context.dataStore.edit {
            it[Keys.ONBOARDING_DONE] = true
            it[Keys.DAILY_GOAL] = dailyGoal
        }
    }

    suspend fun setDailyGoal(goal: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL] = goal }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setDarkMode(option: DarkModeOption) {
        context.dataStore.edit { it[Keys.DARK_MODE] = option.name }
    }

    /** Cộng XP đúng 1 câu trả lời đúng: cộng cả tổng XP và XP hôm nay (tự reset nếu qua ngày mới). */
    suspend fun addXp(amount: Int = XP_PER_CORRECT) {
        val today = todayString()
        context.dataStore.edit { prefs ->
            val xpDate = prefs[Keys.XP_TODAY_DATE]
            val currentXpToday = if (xpDate == today) prefs[Keys.XP_TODAY] ?: 0 else 0
            prefs[Keys.XP_TODAY] = currentXpToday + amount
            prefs[Keys.XP_TODAY_DATE] = today
            prefs[Keys.TOTAL_XP] = (prefs[Keys.TOTAL_XP] ?: 0) + amount
        }
    }

    /** Trừ 1 tim khi trả lời sai. Không cho xuống dưới 0. */
    @Deprecated(
        "Vocab300 v1.1.0 đã bỏ hệ thống tim khỏi trải nghiệm người dùng. " +
            "Hàm này không còn được gọi ở bất kỳ đâu trong app, chỉ giữ lại để không phá vỡ " +
            "dữ liệu DataStore cũ của người dùng đã cài bản trước."
    )
    suspend fun loseHeart() {
        val today = todayString()
        context.dataStore.edit { prefs ->
            val heartsDate = prefs[Keys.HEARTS_DATE]
            val currentHearts = if (heartsDate == today) prefs[Keys.HEARTS] ?: MAX_HEARTS else MAX_HEARTS
            prefs[Keys.HEARTS] = (currentHearts - 1).coerceAtLeast(0)
            prefs[Keys.HEARTS_DATE] = today
        }
    }

    @Deprecated(
        "Vocab300 v1.1.0 đã bỏ hệ thống tim khỏi trải nghiệm người dùng. " +
            "Hàm này không còn được gọi ở bất kỳ đâu trong app, chỉ giữ lại để không phá vỡ " +
            "dữ liệu DataStore cũ của người dùng đã cài bản trước."
    )
    suspend fun refillHearts() {
        context.dataStore.edit { prefs ->
            prefs[Keys.HEARTS] = MAX_HEARTS
            prefs[Keys.HEARTS_DATE] = todayString()
        }
    }

    /**
     * Cập nhật chuỗi ngày học khi hoàn thành ít nhất một bài trong ngày.
     * - Nếu hôm nay đã tính streak rồi thì không tăng thêm.
     * - Nếu ngày học gần nhất là hôm qua -> streak += 1.
     * - Nếu ngày học gần nhất không phải hôm qua và không phải hôm nay -> streak reset về 1.
     */
    suspend fun recordStudyActivityToday() {
        val today = LocalDate.now(ZoneId.systemDefault())
        val todayStr = today.toString()
        context.dataStore.edit { prefs ->
            val lastDateStr = prefs[Keys.LAST_STUDY_DATE]
            if (lastDateStr == todayStr) return@edit // đã tính hôm nay rồi

            val currentStreak = prefs[Keys.STREAK_DAYS] ?: 0
            val newStreak = if (lastDateStr != null) {
                val lastDate = runCatching { LocalDate.parse(lastDateStr) }.getOrNull()
                if (lastDate != null && lastDate.plusDays(1) == today) {
                    currentStreak + 1
                } else {
                    1
                }
            } else {
                1
            }
            prefs[Keys.STREAK_DAYS] = newStreak
            prefs[Keys.LAST_STUDY_DATE] = todayStr
        }
    }

    suspend fun setCurrentLessonIndex(language: String, lessonIndex: Int) {
        context.dataStore.edit { prefs ->
            if (language == "EN") prefs[Keys.CURRENT_LESSON_EN] = lessonIndex
            else prefs[Keys.CURRENT_LESSON_KO] = lessonIndex
            prefs[Keys.LAST_LANGUAGE] = language
        }
    }

    val currentLessonEn: Flow<Int> = context.dataStore.data.map { it[Keys.CURRENT_LESSON_EN] ?: 0 }
    val currentLessonKo: Flow<Int> = context.dataStore.data.map { it[Keys.CURRENT_LESSON_KO] ?: 0 }
    val lastLanguage: Flow<String> = context.dataStore.data.map { it[Keys.LAST_LANGUAGE] ?: "EN" }

    /** Reset toàn bộ tiến độ người dùng (XP, tim, streak, mục tiêu về mặc định, KHÔNG xoá cờ import). */
    suspend fun resetProgressPrefs() {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOTAL_XP] = 0
            prefs[Keys.XP_TODAY] = 0
            prefs[Keys.XP_TODAY_DATE] = todayString()
            prefs[Keys.STREAK_DAYS] = 0
            prefs.remove(Keys.LAST_STUDY_DATE)
            prefs[Keys.CURRENT_LESSON_EN] = 0
            prefs[Keys.CURRENT_LESSON_KO] = 0
        }
    }
}
