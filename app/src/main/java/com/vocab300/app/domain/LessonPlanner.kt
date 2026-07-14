package com.vocab300.app.domain

/**
 * Chia danh sách id từ vựng (đã sắp xếp theo sequenceOrder) của MỘT ngôn ngữ thành các bài học.
 * Mặc định 10 từ/bài. Bài cuối cùng có thể có ít hơn 10 từ nếu số từ không chia hết.
 */
object LessonPlanner {

    const val DEFAULT_LESSON_SIZE = 10

    fun buildLessons(
        language: Language,
        orderedWordIds: List<String>,
        lessonSize: Int = DEFAULT_LESSON_SIZE
    ): List<Lesson> {
        require(lessonSize > 0) { "lessonSize phải > 0" }
        if (orderedWordIds.isEmpty()) return emptyList()

        return orderedWordIds
            .chunked(lessonSize)
            .mapIndexed { index, ids -> Lesson(language, index, ids) }
    }
}
