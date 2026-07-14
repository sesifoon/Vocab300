# Hướng dẫn bổ sung ảnh minh hoạ cho từ vựng

Hiện tại **không có từ nào trong 300 từ có ảnh riêng** (dữ liệu nguồn không có cột `imageKey`).
Ứng dụng đang hiển thị **emoji theo category** làm minh hoạ mặc định (xem
`WordIllustration.kt`), không tải ảnh từ Internet, không dùng ảnh có vấn đề bản quyền.

Muốn thêm ảnh thật cho một từ cụ thể, làm theo 3 bước sau:

## Bước 1 — Đặt file ảnh vào đúng thư mục

Copy ảnh (định dạng `.png` hoặc `.webp`, khuyến nghị vuông, ví dụ 512x512px, dung lượng nhỏ)
vào:

```
app/src/main/res/drawable/<ten_anh>.png
```

**Quy tắc đặt tên bắt buộc của Android:** chỉ chữ thường, số, dấu gạch dưới, không dấu, không khoảng trắng,
không bắt đầu bằng số. Ví dụ hợp lệ: `word_function.png`, `ic_no_power.png`.

## Bước 2 — Gán `imageKey` cho từ trong dữ liệu

Mở `app/src/main/assets/vocabulary.json`, tìm từ cần gán ảnh theo `id`, và điền `imageKey`
đúng bằng tên file KHÔNG có phần đuôi mở rộng:

```json
{
  "id": "EN001",
  "language": "EN",
  "word": "Function",
  "meaningVi": "Chức Năng",
  "pronunciation": null,
  "example": null,
  "category": "Tên lỗi",
  "imageKey": "word_function",
  "sequenceOrder": 1
}
```

## Bước 3 — Build lại ứng dụng

Vì cơ chế import chỉ chạy **một lần duy nhất** (không import trùng), sau khi sửa
`vocabulary.json` bạn cần một trong hai cách sau để dữ liệu mới được nạp lại:

- **Cách đơn giản nhất (khuyến nghị khi đang phát triển):** gỡ cài đặt ứng dụng khỏi
  thiết bị/máy ảo (Uninstall) rồi Run lại từ Android Studio. Việc gỡ cài đặt sẽ xoá luôn
  Room DB và DataStore, khiến app coi như "chạy lần đầu" và import lại toàn bộ 300 từ
  (bao gồm `imageKey` mới).
- **Cách khác:** tăng version Room database và viết migration, hoặc xoá thủ công dữ liệu
  ứng dụng trong Settings > Apps > Vocab300 > Storage > Clear data trên thiết bị thật.

Sau đó chỉ cần build lại (`Run ▶` hoặc `Build > Build APK(s)`), không cần sửa code Kotlin —
tầng hiển thị (`WordIllustration`) đã sẵn sàng đọc `imageKey` từ `res/drawable` khi có.

## Lưu ý quan trọng

- Không dùng ảnh chụp/scan từ sách, tài liệu có bản quyền của bên thứ ba.
- Không dùng bất kỳ ảnh/icon nào liên quan tới Duolingo (logo, linh vật cú xanh, biểu tượng đặc trưng).
- Ảnh nên nhỏ gọn (không phóng to nguyên bản > 1MB) để giữ APK nhẹ, vì ứng dụng hoạt động hoàn toàn offline.
