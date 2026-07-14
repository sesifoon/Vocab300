# Báo cáo cấu trúc dữ liệu — 300 từ Anh/Hàn

## 1. File nguồn đã kiểm tra

Người dùng cung cấp 3 định dạng cùng nội dung:
- `300_tu_Anh_Han_da_chuan_hoa.json`
- `300_tu_Anh_Han_da_chuan_hoa.csv`
- `300_tu_Anh_Han_da_chuan_hoa.xlsx`

**File dùng làm nguồn chính cho import: JSON** (sạch nhất, có kiểu dữ liệu rõ ràng: số, boolean, chuỗi).

## 2. Cấu trúc XLSX

4 sheet:
| Sheet | Nội dung | Ghi chú |
|---|---|---|
| `300 từ` | Toàn bộ 300 dòng dữ liệu | Có 2 dòng tiêu đề/mô tả ở đầu trước khi vào bảng dữ liệu thật (dòng 1–2 là text mô tả, không phải header cột) |
| `English 200` | 200 từ tiếng Anh | Cùng cấu trúc |
| `Korean 100` | 100 từ tiếng Hàn | Cùng cấu trúc |
| `Lịch sử chuẩn hóa` | 27 dòng ghi log các từ đã sửa chính tả/chuẩn hoá | Không dùng cho import, chỉ mang tính lịch sử |

## 3. Cột dữ liệu thực tế (JSON/CSV, đã đối chiếu khớp nhau)

| Cột JSON | Cột CSV | Kiểu | Mô tả |
|---|---|---|---|
| `id` | `ID` | string | Mã định danh, VD `EN001`, `KO100` |
| `language` | `Ngôn ngữ` | string | `en`/`ko` (JSON) hoặc `Tiếng Anh`/`Tiếng Hàn` (CSV) |
| `category` | `Chủ đề` | string | 8 giá trị (xem mục 4) |
| `term` | `Từ/Cụm từ` | string | Từ vựng gốc |
| `meaning_vi` | `Nghĩa tiếng Việt` | string | Nghĩa tiếng Việt |
| `sequence_order` | `STT` | int | Thứ tự 1..200 (EN) và 1..100 (KO) |
| `is_active` | `Trạng thái` | bool/string | Luôn `true`/`"Hoạt động"` cho cả 300 dòng |

## 4. Thống kê đã xác minh

- Tổng số dòng: **300** (khớp cả 3 file)
- Tiếng Anh (`en`): **200** từ, `sequence_order` 1–200
- Tiếng Hàn (`ko`): **100** từ, `sequence_order` 1–100
- `id` duy nhất, không trùng: **300/300**
- `is_active`: 100% là `true` — không có từ nào bị đánh dấu ngừng dùng
- 8 giá trị `category` xuất hiện: Tên lỗi (115), Tên vị trí kiểm tra (44), Động từ hay dùng (38), Danh từ hay dùng (27), Tính từ hay dùng (26), Các thứ trong tuần (22), Các tháng trong năm (14), Tên hạng mục kiểm tra (14)

**Lưu ý về nội dung:** đây là bộ từ vựng chuyên ngành kỹ thuật/kiểm thử (lỗi sản phẩm, vị trí kiểm tra, hạng mục kiểm tra...), không phải từ vựng giao tiếp thông thường. Category được dùng làm nhãn chủ đề/emoji minh hoạ, không phải cấp độ khó.

## 5. Cột KHÔNG có trong dữ liệu nguồn (không tự bịa)

Theo đúng yêu cầu "nếu một cột không rõ hoặc không có thì ghi lại trong báo cáo thay vì tự suy đoán":

- **`pronunciation` (phiên âm):** KHÔNG có trong bất kỳ file nào → để `null` trong Entity/asset.
- **`example` (câu ví dụ):** KHÔNG có → để `null`.
- **`imageKey` (ảnh riêng cho từ):** KHÔNG có → để `null`. Toàn bộ 300 từ hiện dùng emoji theo category làm minh hoạ mặc định (xem `ADD_IMAGES_GUIDE.md` để bổ sung ảnh thật sau này).

## 6. Ánh xạ sang schema ứng dụng (Room Entity)

File `app/src/main/assets/vocabulary.json` (dùng để import) được chuyển đổi 1-1 từ JSON nguồn:

```json
{
  "id": "EN001",
  "language": "EN",
  "word": "Function",
  "meaningVi": "Chức Năng",
  "pronunciation": null,
  "example": null,
  "category": "Tên lỗi",
  "imageKey": null,
  "sequenceOrder": 1
}
```

Không có bước suy đoán/bịa dữ liệu nào được thực hiện trong quá trình chuyển đổi này — chỉ đổi tên trường (`term`→`word`, `meaning_vi`→`meaningVi`, `sequence_order`→`sequenceOrder`) và chuẩn hoá `language` thành chữ hoa (`en`→`EN`, `ko`→`KO`).
