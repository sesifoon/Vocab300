package com.vocab300.app.domain

/**
 * Quy tắc mở khóa bài học:
 * - Bài đầu tiên (lessonIndex == 0) của mỗi ngôn ngữ luôn mở khóa.
 * - Bài thứ N (N > 0) chỉ mở khóa khi bài N-1 đạt tối thiểu [PASS_THRESHOLD_PERCENT]%.
 */
object LessonUnlockChecker {

    const val PASS_THRESHOLD_PERCENT = 80

    fun isUnlocked(lessonIndex: Int, previousLessonBestPercent: Int): Boolean {
        if (lessonIndex <= 0) return true
        return previousLessonBestPercent >= PASS_THRESHOLD_PERCENT
    }

    /**
     * Tính trạng thái node hiển thị trên đường học, cho toàn bộ danh sách bài
     * theo thứ tự lessonIndex tăng dần. [scoresByLessonIndex] chứa điểm tốt nhất đã đạt (0 nếu chưa học).
     */
    fun buildLessonNodes(
        lessons: List<Lesson>,
        scoresByLessonIndex: Map<Int, Int>,
        completedIndexes: Set<Int>
    ): List<LessonNode> {
        val sorted = lessons.sortedBy { it.lessonIndex }
        var previousScore = 0
        val result = mutableListOf<LessonNode>()
        for (lesson in sorted) {
            val score = scoresByLessonIndex[lesson.lessonIndex] ?: 0
            val unlocked = isUnlocked(lesson.lessonIndex, previousScore)
            val state = when {
                lesson.lessonIndex in completedIndexes -> LessonNodeState.COMPLETED
                !unlocked -> LessonNodeState.LOCKED
                else -> LessonNodeState.IN_PROGRESS
            }
            result.add(LessonNode(lesson, state, score))
            previousScore = score
        }
        return result
    }
}
