package com.vocab300.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tiến độ học của một từ cụ thể: số lần đúng/sai, mức thành thạo, ngày cần ôn lại...
 * wordId tham chiếu tới VocabularyWordEntity.id (không dùng khóa ngoại cứng để đơn giản
 * hoá import ban đầu; ràng buộc được đảm bảo ở tầng Repository).
 */
@Entity(tableName = "word_progress")
data class WordProgressEntity(
    @PrimaryKey val wordId: String,
    val timesCorrect: Int = 0,
    val timesWrong: Int = 0,
    val masteryLevel: Int = 0,       // 0..5
    val lastReviewedAt: Long? = null, // epoch millis, null = chưa từng học
    val nextReviewAt: Long? = null,   // epoch millis, null = chưa cần ôn (chưa học)
    val isKnown: Boolean = false,     // người dùng tự đánh dấu "đã thuộc"
    val isFavorite: Boolean = false
)
