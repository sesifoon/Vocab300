# Hướng dẫn nhập & mở rộng dữ liệu từ vựng — Vocab300

## 1. Cột bắt buộc

| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | string | Tuỳ chọn khi dùng `scripts/import_vocabulary.py` — script tự sinh ID ổn định nếu thiếu (xem mục 4). Nếu tự cung cấp, phải duy nhất. |
| `language` | string | Chỉ chấp nhận `EN` hoặc `KO`. |
| `word` | string | Từ/cụm từ gốc. |
| `meaningVi` | string | Nghĩa tiếng Việt. |

## 2. Cột tuỳ chọn (được phép null/rỗng, không bắt buộc điền)

`pronunciation`, `example`, `category`, `imageKey`, `sequenceOrder`, `romanization`,
`exampleTranslationVi`, `difficulty`, `tags`, `abbreviation`, `aliases`, `keywords`, `sourceVersion`.

Nếu không cung cấp `sequenceOrder`, script sẽ dùng thứ tự dòng trong file làm giá trị tạm — không
ảnh hưởng tới tiến độ vì tiến độ liên kết theo `id`, không theo `sequenceOrder`.

## 3. Ví dụ bản ghi tối thiểu

```json
{
  "language": "EN",
  "word": "Function",
  "meaningVi": "Chức Năng"
}
```

Ví dụ bản ghi đầy đủ:

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
  "sequenceOrder": 1,
  "romanization": null,
  "exampleTranslationVi": null,
  "difficulty": null,
  "tags": null,
  "abbreviation": null,
  "aliases": null,
  "keywords": null,
  "sourceVersion": "v1"
}
```

## 4. Cách chuyển CSV/XLSX sang JSON chuẩn

Chạy trên máy tính (KHÔNG chạy trong app — app không đọc Excel trực tiếp):

```bash
pip install openpyxl --break-system-packages   # chỉ cần nếu dùng file .xlsx

# Bước 1: kiểm tra chất lượng dữ liệu trước
python3 scripts/validate_vocabulary.py data/1000_tu_moi.xlsx

# Bước 2: chuyển sang JSON chuẩn (chỉ chạy khi bước 1 báo ✅)
python3 scripts/import_vocabulary.py data/1000_tu_moi.xlsx \
    --source-version v2-1000 \
    --output app/src/main/assets/vocabulary.json
```

Hỗ trợ input: `.json`, `.csv`, `.tsv`, `.xlsx`.

## 5. Cách validation hoạt động

`scripts/validate_vocabulary.py` báo cáo:
- Tổng dòng, dòng rỗng
- Thiếu `word` / thiếu `meaningVi`
- ID trùng (lỗi chặn import)
- Từ trùng hoàn toàn (cùng ngôn ngữ + từ + nghĩa)
- Từ giống nhau nhưng khác nghĩa (chỉ cảnh báo để bạn tự rà soát, **KHÔNG tự xoá** — có thể là 2
  nghĩa hợp lệ khác nhau của cùng 1 từ)
- Ngôn ngữ không hợp lệ
- Số từ theo ngôn ngữ / theo category
- Số mã viết tắt, số cụm dài (ước tính theo cùng quy tắc `EntryClassifier` trong app)
- Tổng bản ghi hợp lệ

Script trả về exit code khác 0 nếu có lỗi chặn import (thiếu `word`/`meaningVi`, ID trùng).

## 6. Cách xử lý trùng

- **ID trùng trong file nguồn**: `import_vocabulary.py` tự thêm hậu tố (`-2`, `-3`...) để tránh ghi
  đè, đồng thời in cảnh báo — bạn nên sửa lại ID gốc thay vì để script tự xử lý lâu dài.
- **Từ trùng hoàn toàn** (cùng ngôn ngữ, cùng từ, cùng nghĩa): nên xoá thủ công 1 trong 2 dòng trước
  khi import, vì đây gần như chắc chắn là lỗi nhập liệu.
- **Từ giống nhau nhưng khác nghĩa** (VD 2 nghĩa khác nhau của cùng 1 từ): **KHÔNG xoá** — đây có
  thể là 2 mục từ hợp lệ, hãy tự quyết định gộp hay giữ riêng.

## 7. Cách tăng `sourceVersion`

Mỗi lần bạn xuất một bộ dữ liệu mới (thêm từ, sửa nội dung hàng loạt), đặt một `sourceVersion` MỚI
qua tham số `--source-version` của `import_vocabulary.py` (ví dụ `v1` → `v2-1000` → `v3-2500`).
Ứng dụng so sánh `sourceVersion` để quyết định có cần import bổ sung hay không — xem mục 9.

## 8. Cách thay 300 từ bằng 1.000 từ (hoặc nhiều hơn)

1. Chuẩn bị file nguồn mới (có thể gồm cả 300 từ cũ + 700 từ mới, hoặc chỉ 700 từ mới — xem mục 9
   để biết vì sao cả 2 cách đều an toàn).
2. Chạy `validate_vocabulary.py` rồi `import_vocabulary.py` với `--source-version` mới, ví dụ
   `v2-1000`.
3. Copy file JSON xuất ra đè vào `app/src/main/assets/vocabulary.json`.
4. Build lại app. KHÔNG cần sửa code Kotlin — toàn bộ tầng nghiệp vụ (Thống kê, lộ trình bài học,
   tìm kiếm, Question Generator...) đã tính động theo số từ thực tế trong database.

## 9. Cách bảo toàn tiến độ khi cập nhật dữ liệu

- `VocabularyImporter` dùng `OnConflictStrategy.IGNORE` khi insert: nếu `id` đã tồn tại trong
  database, bản ghi MỚI bị bỏ qua, bản ghi CŨ được giữ nguyên. Vì vậy file nguồn mới có thể chứa
  lại toàn bộ 300 từ cũ (cùng `id`) + từ mới — ứng dụng sẽ chỉ thêm phần mới, không ghi đè.
- Bảng tiến độ (`word_progress`, `lesson_progress`) hoàn toàn tách biệt khỏi bảng từ vựng, liên kết
  qua `id` ổn định — không bị đụng tới khi import lại.
- **Điều kiện bắt buộc để bảo toàn tiến độ đúng**: các từ cũ trong file mới phải giữ NGUYÊN `id` cũ.
  Nếu bạn để `import_vocabulary.py` tự sinh ID (không cung cấp cột `id`), ID được sinh ổn định từ
  `language + word + meaningVi` — nghĩa là chỉ cần nội dung 3 trường này không đổi, ID sẽ giống hệt
  lần trước dù thứ tự dòng trong file có thay đổi.
- XP, streak, mastery, lịch ôn tập không bị reset khi cập nhật dữ liệu (chỉ bị reset khi người dùng
  chủ động bấm "Đặt lại tiến độ" trong Cài đặt).

## 10. Cách kiểm tra tổng số từ sau import

Mở app → **Trang chủ** → thẻ tiến độ từng ngôn ngữ, hoặc **Thống kê cá nhân** → dòng "Từ đã học"
sẽ hiển thị `x/N` với `N` là tổng số từ thực tế trong database (tính động, không hard-code).
Có thể kiểm tra nhanh ngoài app bằng:

```bash
python3 -c "import json; print(len(json.load(open('app/src/main/assets/vocabulary.json', encoding='utf-8'))))"
```
