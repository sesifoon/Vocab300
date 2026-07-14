package com.vocab300.app.data.local.entity

import androidx.room.Entity

/**
 * Tiến độ của một bài học (10 từ/bài) trong lộ trình của một ngôn ngữ.
 * Khóa chính ghép: language + lessonIndex (ví dụ "EN"-0, "EN"-1, "KO"-0...).
 */
@Entity(tableName = "lesson_progress", primaryKeys = ["language", "lessonIndex"])
data class LessonProgressEntity(
    val language: String,      // "EN" hoặc "KO"
    val lessonIndex: Int,      // 0-based
    val bestScorePercent: Int = 0,
    val isCompleted: Boolean = false, // true nếu bestScorePercent >= 80
    val lastAttemptAt: Long? = null
)
