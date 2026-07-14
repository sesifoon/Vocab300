#!/usr/bin/env python3
"""
validate_vocabulary.py — Kiểm tra chất lượng dữ liệu từ vựng trước khi import vào Vocab300.

Hỗ trợ input: .json (list các object), .csv, .tsv, .xlsx (cần cột: id, language, word, meaningVi,
tối thiểu; các cột khác tuỳ chọn).

Cách dùng:
    python3 scripts/validate_vocabulary.py path/to/data.xlsx
    python3 scripts/validate_vocabulary.py path/to/data.json

Không sửa/xoá file nguồn. Chỉ in báo cáo ra màn hình (và trả về exit code khác 0 nếu có lỗi
nghiêm trọng: thiếu cột bắt buộc, thiếu word/meaningVi, hoặc ID trùng).
"""
import json
import sys
from collections import Counter, defaultdict
from pathlib import Path

REQUIRED_FIELDS = ["id", "language", "word", "meaningVi"]
VALID_LANGUAGES = {"EN", "KO"}

ABBREV_MIN_LEN, ABBREV_MAX_LEN = 2, 6
SHORT_WORD_MAX_LEN = 12
LONG_PHRASE_MIN_WORDS = 5
LONG_PHRASE_MIN_CHARS = 30


def load_records(path: Path):
    suffix = path.suffix.lower()
    if suffix == ".json":
        with open(path, encoding="utf-8") as f:
            data = json.load(f)
        if not isinstance(data, list):
            raise ValueError("File JSON phải là một danh sách (list) các bản ghi.")
        return data

    if suffix in (".csv", ".tsv"):
        import csv
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
        records = []
        for row in rows[1:]:
            record = {header[i]: row[i] for i in range(len(header)) if i < len(row)}
            records.append(record)
        return records

    raise ValueError(f"Định dạng không hỗ trợ: {suffix}. Dùng .json/.csv/.tsv/.xlsx")


def classify_entry_type(word: str) -> str:
    trimmed = (word or "").strip()
    tokens = trimmed.split()
    if len(tokens) <= 1:
        if ABBREV_MIN_LEN <= len(trimmed) <= ABBREV_MAX_LEN and trimmed == trimmed.upper() and trimmed.isalnum():
            return "ABBREVIATION"
        return "LONG_WORD" if len(trimmed) > SHORT_WORD_MAX_LEN else "SHORT_WORD"
    if len(tokens) >= LONG_PHRASE_MIN_WORDS or len(trimmed) > LONG_PHRASE_MIN_CHARS:
        return "LONG_PHRASE"
    return "MEDIUM_PHRASE"


def main():
    if len(sys.argv) != 2:
        print("Cách dùng: python3 validate_vocabulary.py <file>")
        sys.exit(2)

    path = Path(sys.argv[1])
    if not path.exists():
        print(f"Không tìm thấy file: {path}")
        sys.exit(2)

    records = load_records(path)
    total = len(records)

    empty_rows = 0
    missing_word = 0
    missing_meaning = 0
    invalid_language = 0
    id_counter = Counter()
    exact_duplicates = Counter()  # (language, word_norm, meaning_norm)
    same_word_diff_meaning = defaultdict(set)  # (language, word_norm) -> {meanings}
    lang_counter = Counter()
    category_counter = Counter()
    entry_type_counter = Counter()
    valid_count = 0

    for rec in records:
        word = (rec.get("word") or "").strip() if rec.get("word") is not None else ""
        meaning = (rec.get("meaningVi") or "").strip() if rec.get("meaningVi") is not None else ""
        language = (rec.get("language") or "").strip().upper() if rec.get("language") is not None else ""
        rec_id = rec.get("id")
        category = rec.get("category") or "(không có)"

        if not word and not meaning and not rec_id:
            empty_rows += 1
            continue

        if not word:
            missing_word += 1
        if not meaning:
            missing_meaning += 1
        if language and language not in VALID_LANGUAGES:
            invalid_language += 1

        if rec_id:
            id_counter[str(rec_id)] += 1

        if word and meaning:
            key = (language, word.lower(), meaning.lower())
            exact_duplicates[key] += 1
            same_word_diff_meaning[(language, word.lower())].add(meaning)
            lang_counter[language] += 1
            category_counter[category] += 1
            entry_type_counter[classify_entry_type(word)] += 1
            valid_count += 1

    duplicate_ids = {k: v for k, v in id_counter.items() if v > 1}
    exact_dupes = {k: v for k, v in exact_duplicates.items() if v > 1}
    similar_diff_meaning = {k: v for k, v in same_word_diff_meaning.items() if len(v) > 1}

    print(f"=== BÁO CÁO VALIDATION: {path.name} ===")
    print(f"Tổng dòng đọc được       : {total}")
    print(f"Dòng rỗng                : {empty_rows}")
    print(f"Thiếu 'word'             : {missing_word}")
    print(f"Thiếu 'meaningVi'        : {missing_meaning}")
    print(f"Ngôn ngữ không hợp lệ    : {invalid_language} (chỉ chấp nhận EN/KO)")
    print(f"ID trùng                 : {len(duplicate_ids)}")
    if duplicate_ids:
        for k, v in list(duplicate_ids.items())[:20]:
            print(f"    - id={k} xuất hiện {v} lần")
    print(f"Từ trùng hoàn toàn       : {len(exact_dupes)} (cùng ngôn ngữ + từ + nghĩa)")
    print(f"Từ giống nhau khác nghĩa : {len(similar_diff_meaning)} (KHÔNG bị coi là lỗi, chỉ để bạn rà soát thủ công)")
    print(f"Số từ theo ngôn ngữ      : {dict(lang_counter)}")
    print(f"Số từ theo category      : {dict(category_counter)}")
    print(f"Số mã viết tắt (ABBREVIATION) : {entry_type_counter['ABBREVIATION']}")
    print(f"Số cụm dài (LONG_PHRASE)      : {entry_type_counter['LONG_PHRASE']}")
    print(f"Tổng bản ghi hợp lệ      : {valid_count}")

    has_blocking_error = missing_word > 0 or missing_meaning > 0 or len(duplicate_ids) > 0
    if has_blocking_error:
        print("\n❌ CÓ LỖI CẦN SỬA trước khi import (xem chi tiết ở trên).")
        sys.exit(1)
    else:
        print("\n✅ Dữ liệu hợp lệ, có thể chạy import_vocabulary.py.")


if __name__ == "__main__":
    main()
