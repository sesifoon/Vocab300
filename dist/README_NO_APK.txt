Không có file APK nào trong thư mục này.

Sandbox dùng để tạo project này không có Android SDK, không có Gradle, và không có quyền
truy cập mạng (đã kiểm tra và ghi chi tiết trong docs/BUILD_GUIDE.md). Vì vậy không thể chạy
gradlew assembleDebug và không có APK nào được build ra ở đây.

Hãy làm theo docs/BUILD_GUIDE.md để build APK trên máy của bạn bằng Android Studio.
Sau khi build xong, APK debug sẽ nằm ở: app/build/outputs/apk/debug/app-debug.apk
Bạn có thể copy nó vào đây (dist/Vocab300.apk) nếu muốn.
