# Hướng dẫn Build Vocab300

## ⚠️ Tình trạng build trong môi trường tạo project này

Đã thử thực tế trong sandbox này (không phải chỉ viết hướng dẫn suông):

```
$ apt-get install -y gradle
E: Failed to fetch ... 403 Forbidden

$ curl -I https://services.gradle.org/distributions/gradle-8.7-bin.zip
HTTP/2 403, x-deny-reason: host_not_allowed

$ which gradle / echo $ANDROID_HOME
(rỗng - không có gì)
```

**Kết luận:** sandbox này không có Android SDK, không có Gradle, và không có quyền truy cập
mạng ra ngoài (mọi domain như `dl.google.com`, `services.gradle.org`, `repo.maven.apache.org`
đều bị chặn `host_not_allowed`). Vì vậy **KHÔNG THỂ chạy `gradlew clean` / `gradlew assembleDebug`
và không có APK nào được build ra trong môi trường này.** Đây không phải do thiếu bước cấu hình,
mà là giới hạn cứng của sandbox — không có cách nào khắc phục từ phía chat này.

Toàn bộ source code đã được viết đầy đủ, đúng cấu trúc, sẵn sàng build — bạn cần build APK
trên máy tính của chính mình theo hướng dẫn dưới đây.

## Yêu cầu máy của bạn

- Android Studio (bản Hedgehog/2023.1 trở lên khuyến nghị, hỗ trợ Kotlin 1.9.24 + AGP 8.5.2)
- JDK 17 trở lên (Android Studio thường đã kèm sẵn JBR phù hợp)
- Máy có kết nối Internet ở lần build đầu tiên (để Gradle tải Android Gradle Plugin, Compose,
  Room, Navigation... theo đúng các phiên bản khai báo trong `app/build.gradle.kts`)

## Các bước

1. **Mở project:** Android Studio → `Open` → chọn thư mục `Vocab300` (thư mục chứa
   `settings.gradle.kts`).
2. **Đợi Gradle Sync:** Android Studio sẽ tự động tạo Gradle wrapper (`gradlew`,
   `gradle-wrapper.jar`) nếu thiếu, và tải toàn bộ dependency. Lần đầu có thể mất vài phút.
3. **Chạy unit test:**
   - Trong Android Studio: chuột phải vào `app/src/test/java/com/vocab300/app/domain` → `Run Tests`.
   - Hoặc dòng lệnh: `./gradlew test`
4. **Build debug APK:**
   ```
   ./gradlew clean
   ./gradlew assembleDebug
   ```
5. **Tìm APK:** sau khi build thành công, APK nằm ở:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
   Copy file này vào `dist/Vocab300.apk` nếu bạn muốn giữ đúng quy ước đặt tên của dự án.
6. **Cài lên điện thoại:**
   - Bật "Cài đặt ứng dụng không rõ nguồn gốc" (Install unknown apps) cho trình duyệt/File
     Manager trên điện thoại Android (Cài đặt > Bảo mật, hoặc Android sẽ tự hỏi khi bạn mở file APK).
   - Chuyển file `app-debug.apk` vào điện thoại (cáp USB, Google Drive, email...).
   - Mở file APK trên điện thoại và bấm Cài đặt.
   - Hoặc nếu điện thoại nối với máy tính qua USB (bật USB Debugging): `adb install app-debug.apk`.

## Nếu Gradle Sync báo lỗi phiên bản

Project khai báo cố định các phiên bản sau trong `app/build.gradle.kts`:
AGP 8.5.2, Kotlin 1.9.24, KSP 1.9.24-1.0.20, Compose BOM 2024.06.00, Room 2.6.1,
Navigation Compose 2.7.7, DataStore 1.1.1. Nếu Android Studio của bạn quá cũ để hỗ trợ các
bản này, hãy cập nhật Android Studio lên bản mới, hoặc hạ phiên bản AGP/Kotlin tương ứng với
Android Studio bạn đang dùng (Android Studio sẽ gợi ý phiên bản phù hợp khi Sync báo lỗi).
