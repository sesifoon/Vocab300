package com.vocab300.app.ui.screens.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocab300.app.data.datastore.UserPrefsManager
import com.vocab300.app.data.repository.ProgressRepository
import com.vocab300.app.data.repository.VocabularyRepository
import com.vocab300.app.domain.AnswerResult
import com.vocab300.app.domain.AnswerValidator
import com.vocab300.app.domain.EntryClassifier
import com.vocab300.app.domain.Language
import com.vocab300.app.domain.LessonPlanner
import com.vocab300.app.domain.LessonSession
import com.vocab300.app.domain.Question
import com.vocab300.app.domain.QuestionGenerator
import com.vocab300.app.domain.ScoringEngine
import com.vocab300.app.tts.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AnswerFeedback {
    object None : AnswerFeedback()
    object Correct : AnswerFeedback()
    data class NearlyCorrect(val correctAnswer: String) : AnswerFeedback()
    data class Incorrect(val userAnswer: String, val correctAnswer: String) : AnswerFeedback()
}

/**
 * v1.1.0: KHÔNG còn `hearts`/`outOfHearts`. Vocab300 đã bỏ hoàn toàn hệ thống tim khỏi trải nghiệm
 * người dùng — người dùng luôn được phép bắt đầu bài và tiếp tục sau khi trả lời sai.
 * Thay vào đó hiển thị tiến độ câu hiện tại trong bài (VD: 3/10) ở LessonScreen.
 */
data class LessonUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val question: Question? = null,
    val questionNumber: Int = 0,
    val totalQuestions: Int = 0,
    val feedback: AnswerFeedback = AnswerFeedback.None,
    val ttsAvailable: Boolean = true,
    val ttsMessage: String? = null,
    val matchedPairIndexes: Set<Int> = emptySet(),
    val selectedLeftIndex: Int? = null,
    val wrongMatchPulse: Int = 0, // tăng lên mỗi lần ghép sai để trigger animation
    val finished: Boolean = false,
    val result: com.vocab300.app.domain.LessonResult? = null,
    val startTimeMillis: Long = 0L
)

class LessonViewModel(
    private val language: Language,
    private val lessonIndex: Int,
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: ProgressRepository,
    private val userPrefsManager: UserPrefsManager,
    private val ttsManager: TtsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    private var session: LessonSession? = null
    private var matchingHadMistake = false

    init {
        loadLesson()
    }

    private fun loadLesson() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, startTimeMillis = System.currentTimeMillis())

            val allWords = vocabularyRepository.getByLanguage(language.name)
            val lessons = LessonPlanner.buildLessons(language, allWords.map { it.id })
            val lesson = lessons.getOrNull(lessonIndex)

            if (lesson == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Không tìm thấy bài học.")
                return@launch
            }

            val lessonWords = allWords.filter { it.id in lesson.wordIds }
            val masteryMap = progressRepository.getMasteryMap(lessonWords.map { it.id })
            val questions = QuestionGenerator.generate(
                lessonWords = lessonWords,
                allWordsSamePool = allWords,
                masteryByWordId = masteryMap,
                ttsAvailableForLanguage = { lang -> ttsManager.isLanguageAvailable(lang) }
            )
            session = LessonSession(questions)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                question = session?.currentQuestion(),
                questionNumber = 1,
                totalQuestions = questions.size
            )
        }
    }

    fun playTts() {
        val question = _uiState.value.question ?: return
        val text = question.ttsText ?: return
        val lang = question.ttsLanguage ?: return
        val available = ttsManager.isLanguageAvailable(lang)
        if (!available) {
            _uiState.value = _uiState.value.copy(
                ttsAvailable = false,
                ttsMessage = "Không phát được âm thanh trên thiết bị này. Bạn có thể bỏ qua câu này."
            )
            return
        }
        ttsManager.speak(text, lang)
        _uiState.value = _uiState.value.copy(ttsAvailable = true, ttsMessage = null)
    }

    fun dismissTtsMessage() {
        _uiState.value = _uiState.value.copy(ttsMessage = null)
    }

    /** Dùng cho WORD_TO_MEANING, MEANING_TO_WORD, LISTENING (trắc nghiệm — chỉ đúng/sai, không gần đúng). */
    fun submitChoiceAnswer(answer: String) {
        val currentSession = session ?: return
        val question = currentSession.currentQuestion() ?: return
        if (_uiState.value.feedback != AnswerFeedback.None) return

        val result = AnswerValidator.validateChoice(answer, question.correctAnswer)
        evaluateAndAdvance(question.wordId, result, answer, question.correctAnswer)
    }

    /** Dùng riêng cho TYPE_ANSWER — chấm theo EntryType (hỗ trợ NEARLY_CORRECT với từ đơn dài). */
    fun submitTypedAnswer(answer: String) {
        val currentSession = session ?: return
        val question = currentSession.currentQuestion() ?: return
        if (_uiState.value.feedback != AnswerFeedback.None) return

        val entryType = EntryClassifier.classify(question.correctAnswer)
        val result = AnswerValidator.validate(answer, question.correctAnswer, entryType)
        evaluateAndAdvance(question.wordId, result, answer, question.correctAnswer)
    }

    /** Người dùng chọn 1 từ bên trái trong dạng MATCHING. */
    fun selectMatchingLeft(index: Int) {
        _uiState.value = _uiState.value.copy(selectedLeftIndex = index)
    }

    /** Người dùng chọn 1 nghĩa bên phải trong dạng MATCHING, khớp với từ đã chọn bên trái. */
    fun selectMatchingRight(rightIndex: Int, leftWord: String, rightMeaning: String, correctMeaning: String) {
        val leftIndex = _uiState.value.selectedLeftIndex ?: return
        viewModelScope.launch {
            if (rightMeaning == correctMeaning) {
                val newMatched = _uiState.value.matchedPairIndexes + leftIndex
                _uiState.value = _uiState.value.copy(
                    matchedPairIndexes = newMatched,
                    selectedLeftIndex = null
                )
                val question = session?.currentQuestion()
                if (question != null && newMatched.size >= question.matchingPairs.size) {
                    // Hoàn thành ghép cặp -> tính là 1 câu (đúng nếu không ghép sai lần nào)
                    val result = if (matchingHadMistake) AnswerResult.NEARLY_CORRECT else AnswerResult.CORRECT
                    evaluateAndAdvance(question.wordId, result, "", "")
                    matchingHadMistake = false
                }
            } else {
                matchingHadMistake = true
                _uiState.value = _uiState.value.copy(
                    selectedLeftIndex = null,
                    wrongMatchPulse = _uiState.value.wrongMatchPulse + 1
                )
            }
        }
    }

    fun skipListeningQuestion() {
        val currentSession = session ?: return
        currentSession.currentQuestion() ?: return
        // Bỏ qua câu nghe vì thiết bị không hỗ trợ TTS: không phải lỗi kiến thức, không phạt.
        currentSession.submitAnswer(AnswerResult.CORRECT)
        goToNextQuestionOrFinish()
    }

    private fun evaluateAndAdvance(wordId: String, result: AnswerResult, userAnswer: String, correctAnswerDisplay: String) {
        viewModelScope.launch {
            progressRepository.recordAnswer(wordId, result, System.currentTimeMillis())

            val feedback = when (result) {
                AnswerResult.CORRECT -> AnswerFeedback.Correct
                AnswerResult.NEARLY_CORRECT -> AnswerFeedback.NearlyCorrect(correctAnswerDisplay)
                AnswerResult.INCORRECT -> AnswerFeedback.Incorrect(userAnswer, correctAnswerDisplay)
            }
            _uiState.value = _uiState.value.copy(feedback = feedback)

            session?.submitAnswer(result)
        }
    }

    /** Gọi khi người dùng bấm "Tiếp tục" sau khi xem phản hồi. Không còn điều kiện chặn nào (đã bỏ tim). */
    fun continueToNext() {
        goToNextQuestionOrFinish()
    }

    private fun goToNextQuestionOrFinish() {
        val currentSession = session ?: return
        if (currentSession.isFinished) {
            finishLesson()
            return
        }
        _uiState.value = _uiState.value.copy(
            question = currentSession.currentQuestion(),
            questionNumber = _uiState.value.questionNumber + 1,
            feedback = AnswerFeedback.None,
            matchedPairIndexes = emptySet(),
            selectedLeftIndex = null
        )
    }

    private fun finishLesson() {
        viewModelScope.launch {
            val currentSession = session ?: return@launch
            val timeSpent = System.currentTimeMillis() - _uiState.value.startTimeMillis
            val result = ScoringEngine.calculateResult(
                totalQuestions = currentSession.totalQuestions,
                correctFirstTry = currentSession.correctFirstTry,
                timeSpentMillis = timeSpent,
                nearlyCorrectFirstTry = currentSession.nearlyCorrectFirstTry
            )
            progressRepository.completeLesson(
                language = language.name,
                lessonIndex = lessonIndex,
                scorePercent = result.scorePercent,
                nowMillis = System.currentTimeMillis()
            )
            _uiState.value = _uiState.value.copy(finished = true, result = result)
        }
    }

    class Factory(
        private val language: Language,
        private val lessonIndex: Int,
        private val vocabularyRepository: VocabularyRepository,
        private val progressRepository: ProgressRepository,
        private val userPrefsManager: UserPrefsManager,
        private val ttsManager: TtsManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LessonViewModel(
                language, lessonIndex, vocabularyRepository, progressRepository, userPrefsManager, ttsManager
            ) as T
        }
    }
}
