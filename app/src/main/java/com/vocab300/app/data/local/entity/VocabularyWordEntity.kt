package com.vocab300.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Một từ vựng. Bộ dữ liệu production hiện tại có đúng 300 từ (200 Anh + 100 Hàn), nhưng schema này
 * KHÔNG giả định con số đó — mọi phần khác của app (thống kê, lộ trình bài học, tìm kiếm...) đều
 * tính động theo số dòng thực tế trong bảng này, để sẵn sàng cho việc thay bằng 500/1.000+ từ.
 *
 * Lưu ý: dữ liệu nguồn (300_tu_Anh_Han_da_chuan_hoa.json/csv/xlsx) KHÔNG có các cột
 * pronunciation, example, imageKey, romanization, exampleTranslationVi, difficulty, tags,
 * abbreviation, aliases, keywords — vì vậy các trường này luôn là null trong dữ liệu import hiện tại.
 * Chúng được giữ lại trong schema để hỗ trợ bổ sung dữ liệu sau này mà không cần migrate lại
 * toàn bộ Room database hay sửa code nghiệp vụ.
 *
 * v1.1.0: thêm các trường mở rộng (xem Vocab300Database MIGRATION_1_2 — cột mới, KHÔNG xoá/đổi
 * cột cũ, không làm mất dữ liệu/tiến độ hiện có) + [sourceVersion] để VocabularyImporter biết khi
 * nào cần import bổ sung (khi bộ dữ liệu tăng từ 300 lên 1.000 chẳng hạn) mà không phải xoá dữ liệu cũ.
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
    val sequenceOrder: Int,

    // ---- Mở rộng v1.1.0 (tất cả tuỳ chọn, không bắt buộc điền cho 300 từ hiện có) ----
    val romanization: String? = null,          // phiên âm La-tinh hoá (hữu ích cho tiếng Hàn)
    val exampleTranslationVi: String? = null,  // bản dịch tiếng Việt của câu ví dụ
    val difficulty: String? = null,            // độ khó tuỳ chọn do người biên soạn gán (không thay thế mastery)
    val tags: String? = null,                  // danh sách thẻ, phân tách bằng dấu phẩy
    val abbreviation: Boolean? = null,         // ghi đè thủ công phân loại mã viết tắt nếu cần (null = để EntryClassifier tự nhận diện)
    val aliases: String? = null,               // các cách viết/gõ khác được chấp nhận, phân tách bằng dấu phẩy
    val keywords: String? = null,              // từ khoá bắt buộc dùng khi chấm cụm dài, phân tách bằng dấu phẩy

    @ColumnInfo(defaultValue = "v1")
    val sourceVersion: String = "v1"
)
