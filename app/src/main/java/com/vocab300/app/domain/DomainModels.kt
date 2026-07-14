package com.vocab300.app.domain

/** Ngôn ngữ hỗ trợ trong ứng dụng. */
enum class Language { EN, KO }

/** Một bài học gồm danh sách id của các từ (thường 10 từ/bài, bài cuối có thể ít hơn). */
data class Lesson(
    val language: Language,
    val lessonIndex: Int, // 0-based
    val wordIds: List<String>
) {
    val title: String get() = "Bài ${lessonIndex + 1}"
}

enum class LessonNodeState { LOCKED, IN_PROGRESS, COMPLETED }

data class LessonNode(
    val lesson: Lesson,
    val state: LessonNodeState,
    val bestScorePercent: Int
)

/** Kết quả một lượt học/bài, dùng để hiển thị màn Kết quả và cập nhật tiến độ. */
data class LessonResult(
    val totalQuestions: Int,
    val correctFirstTry: Int,
    val xpEarned: Int,
    val timeSpentMillis: Long,
    val scorePercent: Int,
    val passed: Boolean
)
