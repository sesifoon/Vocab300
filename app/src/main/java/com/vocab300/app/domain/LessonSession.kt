package com.vocab300.app.domain

/**
 * Quản lý trạng thái của MỘT lượt học (1 bài): hàng đợi câu hỏi hiện tại, số câu đúng lần đầu,
 * và quy tắc "câu trả lời sai phải xuất hiện lại ở cuối bài".
 *
 * Cách dùng:
 *   val session = LessonSession(questions)
 *   val current = session.currentQuestion()
 *   session.submitAnswer(isCorrect = true/false)  // tự động advance + requeue nếu sai
 *   session.isFinished // true khi mọi câu đã được trả lời đúng ít nhất 1 lần
 */
class LessonSession(initialQuestions: List<Question>) {

    private val queue: MutableList<Question> = initialQuestions.toMutableList()
    private val totalUniqueQuestions: Int = initialQuestions.size

    var correctFirstTry: Int = 0
        private set

    /** wordId của các câu đã trả lời sai ít nhất 1 lần (dùng để ưu tiên ôn tập sau này). */
    val wordIdsAnsweredWrong: MutableSet<String> = mutableSetOf()

    private var currentIndex: Int = 0

    val isFinished: Boolean get() = queue.isEmpty()

    val totalQuestions: Int get() = totalUniqueQuestions

    /** Số câu còn lại trong hàng đợi (bao gồm cả câu đã bị đưa xuống cuối để hỏi lại). */
    val remainingInQueue: Int get() = queue.size

    fun currentQuestion(): Question? = queue.firstOrNull()

    /**
     * Ghi nhận kết quả cho câu hỏi hiện tại.
     * - Đúng: xoá khỏi hàng đợi, nếu là lần đầu tiên (attemptCount == 0) thì tính vào correctFirstTry.
     * - Sai: đánh dấu wordId sai, tăng attemptCount, đưa câu hỏi này xuống CUỐI hàng đợi để hỏi lại.
     */
    fun submitAnswer(isCorrect: Boolean): Question {
        val question = queue.removeAt(0)
        if (isCorrect) {
            if (question.attemptCount == 0) {
                correctFirstTry += 1
            }
        } else {
            wordIdsAnsweredWrong.add(question.wordId)
            queue.add(question.copy(attemptCount = question.attemptCount + 1))
        }
        return question
    }
}
