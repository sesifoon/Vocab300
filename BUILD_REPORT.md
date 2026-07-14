# BUILD_REPORT.md — Vocab300

## 1. Trạng thái GitHub Actions

**Không chạy được.** Đã xác minh: sandbox này không có quyền truy cập mạng ra ngoài.

```
$ curl -I https://github.com        → HTTP 403, x-deny-reason: host_not_allowed
$ curl -I https://codeload.github.com → HTTP 403, x-deny-reason: host_not_allowed
```

Không có remote GitHub nào được cấu hình, không commit/push nào được đẩy lên GitHub, và do đó
**không có GitHub Actions run nào được kích hoạt hoặc quan sát**. Đây là giới hạn cứng của môi
trường — không phải do workflow sai hay do tôi bỏ qua bước này.

## 2. Những gì ĐÃ làm được trong sandbox này

### a) Static review (Bước 1) — đã rà soát toàn bộ 64 file Kotlin + Gradle + XML + JSON

| Hạng mục kiểm tra | Kết quả | Hành động |
|---|---|---|
| `TopAppBar` dùng ở 8 màn hình (Home, LessonList, Lesson-list liên quan, Review, Stats, Settings, Search, Favorites, WordDetail) không có opt-in `@ExperimentalMaterial3Api` | **Rủi ro compile thật** | **Đã sửa**: thêm `-opt-in=androidx.compose.material3.ExperimentalMaterial3Api` vào `kotlinOptions.freeCompilerArgs` trong `app/build.gradle.kts` — 1 chỗ sửa, áp dụng cho toàn bộ module, không cần annotate từng file |
| `LinearProgressIndicator(progress = { ... })` (3 nơi dùng: `CommonComponents.kt`, `StatsScreen.kt`, `HomeScreen.kt`) | Đã dùng đúng overload dạng lambda `progress: () -> Float`, tương thích Material3 1.2.x đi kèm Compose BOM `2024.06.00` | Không cần sửa |
| Import thiếu (`Icons.Filled.*`, `Modifier.background`, `clip`, `CircleShape`, `clickable`, `navArgument`, `NavType`, `viewModel`) | Quét toàn bộ 64 file, không phát hiện thiếu import nào | Không cần sửa |
| Navigation argument types (`Screen.kt`, `Vocab300NavGraph.kt`) | `NavType.StringType/IntType/LongType/BoolType` khai báo khớp với kiểu dữ liệu truyền qua route | Không cần sửa |
| Room/KSP config | Kotlin `1.9.24` + KSP `1.9.24-1.0.20` là cặp phiên bản hợp lệ; `@Entity`/`@Dao`/`@Database` khai báo đủ 3 entity, 3 DAO | Không cần sửa |
| AndroidManifest / resource references | `.VocabApp`, `.MainActivity`, `@style/Theme.Vocab300`, `@mipmap/ic_launcher` đều tồn tại đúng vị trí | Không cần sửa |
| Kotlin/Compose compiler compatibility | Compose compiler extension `1.5.14` khớp với Kotlin `1.9.24` theo bảng tương thích chính thức của Google | Không cần sửa |

**Kết luận static review:** phát hiện và sửa đúng 1 rủi ro compile thật sự cụ thể
(`TopAppBar` opt-in). Không có sửa đổi kiến trúc nào ngoài phạm vi, không thay đổi bộ dữ liệu
300 từ, không đổi cấu trúc thư mục hiện có.

### b) CI workflow (Bước 2) — đã tạo, đã kiểm tra cú pháp YAML

File: `.github/workflows/build-apk.yml`

- Trigger: `workflow_dispatch` + `push` nhánh `main` ✅
- `ubuntu-latest`, `actions/checkout@v6`, `actions/setup-java@v5` (Temurin 17),
  `android-actions/setup-android@v4`, `gradle/actions/setup-gradle@v6` (Gradle `8.7`),
  `actions/upload-artifact@v7` ✅
- Cài `platforms;android-34`, `build-tools;34.0.0`, `platform-tools` qua `sdkmanager` ✅
- Chạy `gradle wrapper --gradle-version 8.7` (vì wrapper cố tình không commit vào repo) rồi
  verify đủ 4 file: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`,
  `gradle/wrapper/gradle-wrapper.properties` — fail rõ ràng (`::error::`) nếu thiếu file nào ✅
- `chmod +x gradlew` ✅
- Chạy đúng thứ tự: `assembleDebug` → `testDebugUnitTest` → `lintDebug`, đều có `--stacktrace` ✅
- Upload APK bước `if: always()`, path
  `app/build/outputs/apk/debug/app-debug.apk`, tên artifact `Vocab300-debug-apk`,
  `if-no-files-found: error` ✅
- Upload riêng báo cáo test + lint (`if-no-files-found: warn`, không chặn build nếu thiếu) ✅
- Đã validate cú pháp bằng `python3 -c "import yaml; yaml.safe_load(...)"` → hợp lệ, đủ 13 bước.

**Đã xác nhận đây chỉ là kiểm tra tĩnh (cú pháp + logic từng bước), CHƯA có lần chạy thực tế
nào trên GitHub Actions**, vì lý do nêu ở mục 1.

### c) Git (Bước 3 — một phần, không push được)

- Đã `git init`, tạo branch **`main`**, commit toàn bộ project (86 file).
- **Commit hash:** `2e6d23f1f71891ecb962d4abfe17ead2176d6fbe`
- Không có remote nào được thêm (không thể `git remote add` tới GitHub thật vì không xác thực
  được và mạng bị chặn). Bạn cần tự `git remote add origin <your-repo-url>` rồi `git push -u
  origin main` trên máy bạn hoặc trong một GitHub Codespace/Actions runner có mạng.

## 3. Vòng lặp tự sửa lỗi (Bước 3 đầy đủ) — KHÔNG thực hiện được

Do không có quyền truy cập GitHub, không thể:
- Push code lên remote thật
- Kích hoạt hoặc quan sát GitHub Actions run
- Đọc log lỗi thật từ CI
- Lặp lại sửa lỗi dựa trên log thật

**Điều này có nghĩa: workflow file chưa từng được chạy thử bằng trình runner CI thật.** Có thể
vẫn còn lỗi mà chỉ một lần chạy CI thật mới phát hiện được (ví dụ: phiên bản action cụ thể
`android-actions/setup-android@v4` hoặc `gradle/actions/setup-gradle@v6` có thể có thay đổi
API kể từ lúc viết báo cáo này).

## 4. Việc bạn cần làm để hoàn tất vòng lặp CI thật

1. Giải nén `Vocab300-ci-ready.zip`.
2. Tạo repo mới trên GitHub (hoặc dùng repo có sẵn).
3. Trong thư mục project:
   ```
   git remote add origin https://github.com/<ban>/<ten-repo>.git
   git push -u origin main
   ```
4. Vào tab **Actions** trên GitHub → workflow "Build Vocab300 APK" sẽ tự chạy do trigger
   `push` nhánh `main` (hoặc bấm **Run workflow** thủ công nhờ `workflow_dispatch`).
5. Nếu có bước nào đỏ (fail), copy nguyên văn log lỗi gửi lại cho tôi — tôi sẽ đọc log thật và
   sửa đúng nguyên nhân gốc, không đoán mò, không nâng cấp toàn bộ project chỉ vì 1 API lỗi.

## 5. Tóm tắt trung thực

- **Không có APK nào được build ra ở bước này** — không có GitHub Actions run nào diễn ra.
- Đã sửa đúng 1 rủi ro compile thật (Material3 `TopAppBar` opt-in), xác minh không còn rủi ro
  compile rõ ràng nào khác qua rà soát tĩnh thủ công (không thay thế được trình biên dịch thật).
- Workflow CI đã viết xong, hợp lệ về cú pháp, nhưng **chưa được một lần chạy CI thật nào xác
  nhận**.
- Bộ dữ liệu 300 từ (200 EN + 100 KO) không bị thay đổi.

---

## 6. Cập nhật v1.1.0 (bản ghi mới nhất)

**Bối cảnh:** sau báo cáo ở mục 1-5, người dùng đã tự push code lên GitHub thật và xác nhận
`assembleDebug` + `testDebugUnitTest` chạy xanh, chỉ `lintDebug` đỏ với lỗi
`FlowOperatorInvokedInComposition` tại `MainActivity.kt:26`. Lỗi đó đã được sửa (bọc `remember`)
và người dùng xác nhận **APK đã cài và chạy OK trên điện thoại thật**.

Từ trạng thái đã build-thành-công đó, v1.1.0 thực hiện một loạt thay đổi nghiệp vụ lớn (bỏ tim,
chấm điểm 3 trạng thái, phân loại mục từ, chọn dạng bài thông minh, hỗ trợ >300 từ, responsive).
**Sandbox hiện tại vẫn không có Gradle/Android SDK/mạng** (đã kiểm tra lại, xem mục 1) — vì vậy
toàn bộ thay đổi v1.1.0 dưới đây **chỉ được rà soát tĩnh thủ công**, chưa có một lần
`assembleDebug`/`testDebugUnitTest`/`lintDebug` thật nào xác nhận lại sau đợt sửa lớn này.

**Rủi ro compile đã được rà soát cho v1.1.0:**
- Cân bằng ngoặc `{}`/`()` trên toàn bộ 73 file `.kt` (bao gồm file mới) — OK.
- Import cho các API mới dùng (FlowRow, verticalScroll, imePadding, Icons...) — không thiếu import nào.
- Package declaration khớp thư mục cho toàn bộ file mới — OK.
- Không có định nghĩa trùng lặp class/object.
- Chữ ký hàm thay đổi (`recordAnswer`, `submitAnswer`, `QuestionGenerator.generate`) đều giữ tham
  số cũ dạng deprecated hoặc default value để không phá vỡ lời gọi hiện có.

**Rủi ro CHƯA thể loại trừ hoàn toàn (chỉ trình biên dịch thật mới xác nhận được):**
- `android.applicationVariants.all { outputs.all { ... BaseVariantOutputImpl ... } }` trong
  `app/build.gradle.kts` dùng API nội bộ của AGP để đổi tên file APK — đây là cách làm phổ biến,
  nhiều năm vẫn hoạt động qua các bản AGP, nhưng có thể phát sinh cảnh báo deprecated hoặc lỗi tuỳ
  phiên bản AGP cụ thể (đang dùng AGP 8.5.2). Nếu build lỗi ở bước này, đây là nghi phạm đầu tiên.
- Room `MIGRATION_1_2` (thêm 8 cột cho `vocabulary_words`) chưa được Room thật chạy thử qua migration
  test — cú pháp SQL đã kiểm tra thủ công (ALTER TABLE ADD COLUMN, kiểu dữ liệu khớp Kotlin type),
  nhưng chưa có bằng chứng chạy thật.
- KSP/Room compiler chưa thực sự sinh mã cho `VocabularyWordEntity` với 8 field mới.

**Việc bạn cần làm:** push nhánh chứa các thay đổi v1.1.0 này lên GitHub thật (qua Termux/web editor
như đã làm ở bản trước), để workflow `.github/workflows/build-apk.yml` (đã đổi tên artifact/APK
thành `OQC_Vocab300_v1.1.0`) chạy thật. Gửi lại log nếu có bước đỏ.

