package com.vocab300.app.domain

/**
 * Quản lý trạng thái của MỘT lượt học (1 bài): hàng đợi câu hỏi hiện tại, số câu đúng lần đầu,
 * và quy tắc "câu trả lời sai phải xuất hiện lại ở cuối bài".
 *
 * v1.1.0: hỗ trợ 3 trạng thái chấm điểm qua [AnswerResult]. NEARLY_CORRECT được coi là "qua" —
 * không bị đưa xuống cuối để hỏi lại (không phạt), nhưng chỉ tính nửa trọng số khi tính điểm
 * (xem [ScoringEngine]). Chỉ INCORRECT mới bị requeue.
 *
 * Cách dùng:
 *   val session = LessonSession(questions)
 *   val current = session.currentQuestion()
 *   session.submitAnswer(AnswerResult.CORRECT)  // tự động advance + requeue nếu sai
 *   session.isFinished // true khi mọi câu đã được trả lời chấp nhận được ít nhất 1 lần
 */
class LessonSession(initialQuestions: List<Question>) {

    private val queue: MutableList<Question> = initialQuestions.toMutableList()
    private val totalUniqueQuestions: Int = initialQuestions.size

    var correctFirstTry: Int = 0
        private set

    var nearlyCorrectFirstTry: Int = 0
        private set

    /** wordId của các câu đã trả lời sai ít nhất 1 lần (dùng để ưu tiên ôn tập sau này). */
    val wordIdsAnsweredWrong: MutableSet<String> = mutableSetOf()

    val isFinished: Boolean get() = queue.isEmpty()

    val totalQuestions: Int get() = totalUniqueQuestions

    /** Số câu còn lại trong hàng đợi (bao gồm cả câu đã bị đưa xuống cuối để hỏi lại). */
    val remainingInQueue: Int get() = queue.size

    fun currentQuestion(): Question? = queue.firstOrNull()

    /**
     * Ghi nhận kết quả cho câu hỏi hiện tại.
     * - CORRECT: xoá khỏi hàng đợi, nếu là lần đầu tiên (attemptCount == 0) thì tính vào correctFirstTry.
     * - NEARLY_CORRECT: xoá khỏi hàng đợi (không phạt, không bắt trả lời lại), tính vào nearlyCorrectFirstTry
     *   nếu là lần đầu.
     * - INCORRECT: đánh dấu wordId sai, tăng attemptCount, đưa câu hỏi này xuống CUỐI hàng đợi để hỏi lại.
     */
    fun submitAnswer(result: AnswerResult): Question {
        val question = queue.removeAt(0)
        when (result) {
            AnswerResult.CORRECT -> {
                if (question.attemptCount == 0) correctFirstTry += 1
            }
            AnswerResult.NEARLY_CORRECT -> {
                if (question.attemptCount == 0) nearlyCorrectFirstTry += 1
            }
            AnswerResult.INCORRECT -> {
                wordIdsAnsweredWrong.add(question.wordId)
                queue.add(question.copy(attemptCount = question.attemptCount + 1))
            }
        }
        return question
    }

    /** Giữ lại chữ ký cũ (Boolean) để tương thích ngược; true → CORRECT, false → INCORRECT. */
    @Deprecated("Dùng submitAnswer(AnswerResult) để hỗ trợ NEARLY_CORRECT.")
    fun submitAnswer(isCorrect: Boolean): Question {
        return submitAnswer(if (isCorrect) AnswerResult.CORRECT else AnswerResult.INCORRECT)
    }
}
