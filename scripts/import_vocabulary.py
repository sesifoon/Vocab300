#!/usr/bin/env python3
"""
import_vocabulary.py — Chuyển dữ liệu từ vựng (CSV/TSV/XLSX/JSON) sang đúng schema
app/src/main/assets/vocabulary.json mà Vocab300 dùng để import vào Room.

KHÔNG chạy trong ứng dụng Android — đây là công cụ ngoài, chạy thủ công trên máy bạn trước khi
build APK. Ứng dụng không đọc trực tiếp Excel.

Cách dùng:
    python3 scripts/validate_vocabulary.py path/to/data.xlsx     # kiểm tra trước
    python3 scripts/import_vocabulary.py path/to/data.xlsx --source-version v2-1000 \
        --output app/src/main/assets/vocabulary.json

Nguyên tắc ID ổn định (KHÔNG dùng vị trí dòng làm ID):
    1. Nếu bản ghi đã có cột `id` khác rỗng -> dùng nguyên.
    2. Nếu không có -> sinh ID ổn định từ language + word + meaningVi (hash ngắn, không đổi
       nếu nội dung không đổi, kể cả khi thứ tự dòng trong file nguồn thay đổi).

Toàn bộ bản ghi trong 1 lần export sẽ được gán cùng `sourceVersion` (tham số --source-version).
"""
import argparse
import csv
import hashlib
import json
import sys
from pathlib import Path


def stable_id(language: str, word: str, meaning: str) -> str:
    raw = f"{language.strip().upper()}|{word.strip().lower()}|{meaning.strip().lower()}"
    digest = hashlib.sha1(raw.encode("utf-8")).hexdigest()[:10].upper()
    prefix = language.strip().upper() or "XX"
    return f"{prefix}-{digest}"


def load_records(path: Path):
    suffix = path.suffix.lower()
    if suffix == ".json":
        with open(path, encoding="utf-8") as f:
            return json.load(f)

    if suffix in (".csv", ".tsv"):
        delimiter = "\t" if suffix == ".tsv" else ","
        with open(path, encoding="utf-8") as f:
            reader = csv.DictReader(f, delimiter=delimiter)
            return [dict(row) for row in reader]

    if suffix == ".xlsx":
        try:
            import openpyxl
        except ImportError:
            raise SystemExit("Cần cài openpyxl: pip install openpyxl --break-system-packages")
        wb = openpyxl.load_workbook(path, data_only=True)
        ws = wb.active
        rows = list(ws.iter_rows(values_only=True))
        if not rows:
            return []
        header = [str(h).strip() if h is not None else "" for h in rows[0]]
        return [
            {header[i]: row[i] for i in range(len(header)) if i < len(row)}
            for row in rows[1:]
        ]

    raise ValueError(f"Định dạng không hỗ trợ: {suffix}. Dùng .json/.csv/.tsv/.xlsx")


def normalize_optional(value):
    if value is None:
        return None
    text = str(value).strip()
    return text if text else None


def main():
    parser = argparse.ArgumentParser(description="Chuyển dữ liệu từ vựng sang schema Vocab300")
    parser.add_argument("input", help="File nguồn (.json/.csv/.tsv/.xlsx)")
    parser.add_argument("--source-version", required=True, help="VD: v1, v2-1000, 2026-07-15")
    parser.add_argument("--output", default="vocabulary.json", help="Đường dẫn file JSON xuất ra")
    args = parser.parse_args()

    path = Path(args.input)
    if not path.exists():
        print(f"Không tìm thấy file: {path}")
        sys.exit(2)

    records = load_records(path)
    seen_ids = set()
    output = []
    skipped = 0

    for idx, rec in enumerate(records, start=1):
        word = normalize_optional(rec.get("word") or rec.get("Từ/Cụm từ"))
        meaning = normalize_optional(rec.get("meaningVi") or rec.get("Nghĩa tiếng Việt"))
        language = normalize_optional(rec.get("language") or rec.get("Ngôn ngữ"))

        if not word or not meaning or not language:
            skipped += 1
            print(f"⚠️  Bỏ qua dòng {idx}: thiếu word/meaningVi/language")
            continue

        language = language.strip().upper()
        if language not in ("EN", "KO"):
            skipped += 1
            print(f"⚠️  Bỏ qua dòng {idx}: ngôn ngữ không hợp lệ '{language}'")
            continue

        rec_id = normalize_optional(rec.get("id"))
        if not rec_id:
            rec_id = stable_id(language, word, meaning)

        if rec_id in seen_ids:
            print(f"⚠️  Dòng {idx}: ID trùng '{rec_id}', tự thêm hậu tố để tránh ghi đè")
            suffix = 2
            new_id = f"{rec_id}-{suffix}"
            while new_id in seen_ids:
                suffix += 1
                new_id = f"{rec_id}-{suffix}"
            rec_id = new_id
        seen_ids.add(rec_id)

        sequence_order = rec.get("sequenceOrder")
        try:
            sequence_order = int(sequence_order) if sequence_order not in (None, "") else idx
        except (ValueError, TypeError):
            sequence_order = idx

        output.append({
            "id": rec_id,
            "language": language,
            "word": word,
            "meaningVi": meaning,
            "pronunciation": normalize_optional(rec.get("pronunciation")),
            "example": normalize_optional(rec.get("example")),
            "category": normalize_optional(rec.get("category")) or "(chưa phân loại)",
            "imageKey": normalize_optional(rec.get("imageKey")),
            "sequenceOrder": sequence_order,
            "romanization": normalize_optional(rec.get("romanization")),
            "exampleTranslationVi": normalize_optional(rec.get("exampleTranslationVi")),
            "difficulty": normalize_optional(rec.get("difficulty")),
            "tags": normalize_optional(rec.get("tags")),
            "abbreviation": rec.get("abbreviation") if isinstance(rec.get("abbreviation"), bool) else None,
            "aliases": normalize_optional(rec.get("aliases")),
            "keywords": normalize_optional(rec.get("keywords")),
            "sourceVersion": args.source_version
        })

    with open(args.output, "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)

    print(f"\n✅ Đã ghi {len(output)} bản ghi vào {args.output} (sourceVersion={args.source_version})")
    if skipped:
        print(f"⚠️  Đã bỏ qua {skipped} dòng lỗi (xem chi tiết ở trên).")
    print("Bước tiếp theo: copy file này đè vào app/src/main/assets/vocabulary.json rồi build lại app.")


if __name__ == "__main__":
    main()
