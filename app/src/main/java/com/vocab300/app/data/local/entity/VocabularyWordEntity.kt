package com.vocab300.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Một từ vựng trong bộ 300 từ (200 Anh + 100 Hàn).
 *
 * Lưu ý: dữ liệu nguồn (300_tu_Anh_Han_da_chuan_hoa.json/csv/xlsx) KHÔNG có các cột
 * pronunciation, example, imageKey — vì vậy 3 trường này luôn là null trong dữ liệu
 * import hiện tại. Chúng được giữ lại trong schema để hỗ trợ bổ sung dữ liệu sau này
 * mà không cần migrate lại toàn bộ Room database.
 */
@Entity(tableName = "vocabulary_words")
data class VocabularyWordEntity(
    @PrimaryKey val id: String,
    val language: String,       // "EN" hoặc "KO"
    val word: String,
    val meaningVi: String,
    val pronunciation: String?, // null nếu dữ liệu nguồn không có
    val example: String?,       // null nếu dữ liệu nguồn không có
    val category: String,
    val imageKey: String?,      // null nếu chưa gán ảnh riêng cho từ này
    val sequenceOrder: Int
)
