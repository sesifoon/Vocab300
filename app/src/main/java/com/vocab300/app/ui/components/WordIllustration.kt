package com.vocab300.app.ui.components

/**
 * Chọn hình ảnh minh hoạ cho một từ theo thứ tự ưu tiên:
 * 1. Nếu có imageKey -> (dự kiến) đọc ảnh từ res/drawable cục bộ theo tên imageKey (xem docs/ADD_IMAGES_GUIDE.md).
 *    Hiện bộ dữ liệu 300 từ KHÔNG có từ nào có imageKey, nên nhánh này chưa có ảnh thật để hiển thị,
 *    nhưng cơ chế tra cứu đã sẵn sàng — chỉ cần bổ sung ảnh + gán imageKey là dùng được ngay.
 * 2. Nếu chưa có ảnh riêng -> dùng emoji phù hợp theo category.
 * 3. Nếu không xác định được category -> dùng emoji minh hoạ mặc định.
 *
 * Không bao giờ tải ảnh từ Internet, không dùng asset có vấn đề bản quyền.
 */
object WordIllustration {

    private const val DEFAULT_EMOJI = "📘"

    // Khớp với 8 category thực tế có trong bộ dữ liệu 300 từ đã chuẩn hoá.
    private val categoryEmoji: Map<String, String> = mapOf(
        "Tên lỗi" to "⚠️",
        "Tên vị trí kiểm tra" to "📍",
        "Động từ hay dùng" to "🏃",
        "Danh từ hay dùng" to "🔤",
        "Tính từ hay dùng" to "✨",
        "Các thứ trong tuần" to "📅",
        "Các tháng trong năm" to "🗓️",
        "Tên hạng mục kiểm tra" to "🧪"
    )

    /**
     * Trả về emoji hoặc tên resource ảnh để hiển thị.
     * imageKey hiện luôn null trong dữ liệu 300 từ gốc; hàm này vẫn nhận tham số để sẵn sàng
     * cho việc bổ sung ảnh riêng sau này mà không cần đổi API ở tầng UI.
     */
    fun emojiFor(category: String?, imageKey: String?): String {
        if (category == null) return DEFAULT_EMOJI
        return categoryEmoji[category] ?: DEFAULT_EMOJI
    }
}
