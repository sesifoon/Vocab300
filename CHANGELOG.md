# Changelog

## v1.1.0

### Loại bỏ hệ thống tim
- Xóa hoàn toàn tim khỏi giao diện (biểu tượng, số đếm, dialog "hết tim", các thông báo liên quan).
- Xóa mọi điều kiện khóa dựa trên tim: không còn chặn bắt đầu bài học, trả lời, tiếp tục, hay điều
  hướng vào Ôn tập/Học tự do/Daily lesson vì lý do hết tim.
- Ngừng gọi logic cộng/trừ/reset/khôi phục tim ở mọi nơi trong ứng dụng.
- Các field/key tim cũ trong DataStore được **giữ nguyên** (đánh dấu `@Deprecated`) để không phá vỡ
  dữ liệu người dùng đã cài bản trước; chỉ không còn được đọc/ghi bởi UI hay nghiệp vụ.
- Không thay tim bằng hệ thống điểm mới — XP, chuỗi ngày, mastery, spaced repetition đã đủ tạo động lực.

### Tối ưu bài tập theo loại mục từ
- Thêm bộ phân loại tập trung (`EntryClassifier`/`EntryType`): mã viết tắt, từ ngắn, từ dài, cụm
  trung bình, cụm dài — dựa trên số ký tự, số từ, có phải toàn chữ hoa không.
- Thêm bộ chọn dạng bài tập trung (`ExerciseSelector`): xét loại mục từ, mastery, TTS có khả dụng
  không, dạng bài vừa xuất hiện — tránh lặp dạng bài/từ liên tiếp, không tạo Listening khi thiếu TTS,
  không bắt gõ toàn bộ cụm dài ở chế độ mặc định.

### Thêm trạng thái "Gần đúng"
- Cơ chế chấm 3 trạng thái: `CORRECT` / `NEARLY_CORRECT` / `INCORRECT` (`AnswerValidator`).
- Mã viết tắt luôn yêu cầu chính xác tuyệt đối (không áp dụng gần đúng).
- Từ đơn dài cho phép lệch tối đa 1 ký tự (Levenshtein) → Gần đúng.
- Cụm dài (chế độ nâng cao) dùng ngưỡng tỉ lệ từ khóa khớp ~80%.
- Gần đúng: không bị phạt, không bị đưa xuống cuối bài hỏi lại, cộng nửa XP, mastery giữ nguyên
  (nhẹ hơn hẳn so với đúng hoàn toàn).

### Cải thiện Ôn tập
- Chọn từ ôn theo đúng thứ tự bậc thang: từ đã đến hạn ôn → từ sai nhiều nhất → từ mastery thấp nhất.
- Nếu người dùng chưa học từ nào, hiển thị thông báo rõ ràng kèm nút hành động thay vì màn hình
  trống hoặc bị kẹt.

### Tổng từ tính động
- Trang Thống kê hiển thị `số từ đã học/tổng số từ` lấy động từ database, không còn hard-code 300.
- Làm rõ nhãn mastery (Chưa học / Mới làm quen / Đang nhớ / Khá nhớ / Ghi nhớ tốt / Thành thạo)
  mà không đổi thuật toán tính mastery.

### Chuẩn bị hỗ trợ 1.000+ từ
- `VocabularyWordEntity` bổ sung các trường tuỳ chọn (romanization, exampleTranslationVi,
  difficulty, tags, abbreviation, aliases, keywords, sourceVersion) — tất cả nullable, không bắt
  buộc điền cho 300 từ hiện có.
- Room migration cộng thêm cột (KHÔNG xoá bảng, không mất dữ liệu) — xem `MIGRATION_1_2`.
- Cơ chế import chuyển từ đếm số lượng cố định sang so sánh `sourceVersion`, hỗ trợ import bổ sung
  khi dữ liệu tăng lên mà không ghi đè/mất tiến độ từ cũ.
- `QuestionGenerator` không giả định luôn đủ 4 đáp án; giảm số lựa chọn khi thiếu distractor thay
  vì crash hoặc lặp đáp án.

### Cải thiện responsive
- Thêm `imePadding` + cuộn dọc cho màn Học từ để bàn phím không che nút Kiểm tra/Tiếp tục.
- Thêm cuộn dọc cho Thống kê, Cài đặt, Chi tiết từ, Kết quả, Onboarding.
- Các hàng chip (mục tiêu ngày, chế độ tối, bộ lọc ngôn ngữ) chuyển sang `FlowRow` để tự xuống hàng
  trên màn hình hẹp hoặc font scale lớn.

### Cải thiện TTS và trạng thái lỗi
- Thêm nút "Nghe lại" khi trả lời sai (nếu TTS khả dụng cho câu hỏi đó).
- Màn Ôn tập trống hiển thị hành động rõ ràng thay vì màn hình chết.

### Version
- versionName: `1.0.0` → `1.1.0`
- versionCode: `1` → `2`
- Tên APK: `OQC_Vocab300_v1.1.0.apk`
