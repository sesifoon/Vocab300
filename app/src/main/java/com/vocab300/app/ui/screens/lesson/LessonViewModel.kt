package com.vocab300.app.ui.screens.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocab300.app.data.datastore.UserPrefsManager
import com.vocab300.app.data.repository.ProgressRepository
import com.vocab300.app.data.repository.VocabularyRepository
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AnswerFeedback {
    object None : AnswerFeedback()
    object Correct : AnswerFeedback()
    data class Incorrect(val correctAnswer: String) : AnswerFeedback()
}

data class LessonUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val question: Question? = null,
    val questionNumber: Int = 0,
    val totalQuestions: Int = 0,
    val hearts: Int = 5,
    val feedback: AnswerFeedback = AnswerFeedback.None,
    val ttsAvailable: Boolean = true,
    val ttsMessage: String? = null,
    val matchedPairIndexes: Set<Int> = emptySet(),
    val selectedLeftIndex: Int? = null,
    val wrongMatchPulse: Int = 0, // tăng lên mỗi lần ghép sai để trigger animation
    val outOfHearts: Boolean = false,
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
            val questions = QuestionGenerator.generate(lessonWords, allWords)
            session = LessonSession(questions)

            val stats = userPrefsManager.userStats.first()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                question = session?.currentQuestion(),
                questionNumber = 1,
                totalQuestions = questions.size,
                hearts = stats.hearts,
                outOfHearts = stats.hearts <= 0
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

    /** Dùng cho WORD_TO_MEANING, MEANING_TO_WORD, LISTENING, TYPE_ANSWER. */
    fun submitChoiceOrTypedAnswer(answer: String) {
        val currentSession = session ?: return
        val question = currentSession.currentQuestion() ?: return
        if (_uiState.value.feedback != AnswerFeedback.None) return // đã trả lời câu này, đang chờ bấm Tiếp tục

        val isCorrect = normalize(answer) == normalize(question.correctAnswer)
        evaluateAndAdvance(question.wordId, isCorrect, question.correctAnswer)
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
                    evaluateAndAdvance(question.wordId, !matchingHadMistake, "")
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
        val question = currentSession.currentQuestion() ?: return
        // Bỏ qua câu nghe: không trừ tim, không cộng XP, coi như đã xử lý, chuyển câu tiếp theo
        currentSession.submitAnswer(isCorrect = true) // tránh phạt người dùng vì lỗi thiết bị, không phải lỗi kiến thức
        goToNextQuestionOrFinish()
    }

    private fun evaluateAndAdvance(wordId: String, isCorrect: Boolean, correctAnswerDisplay: String) {
        viewModelScope.launch {
            progressRepository.recordAnswer(wordId, isCorrect, System.currentTimeMillis())
            val stats = userPrefsManager.userStats.first()

            _uiState.value = _uiState.value.copy(
                feedback = if (isCorrect) AnswerFeedback.Correct else AnswerFeedback.Incorrect(correctAnswerDisplay),
                hearts = stats.hearts
            )

            session?.submitAnswer(isCorrect)

            if (stats.hearts <= 0) {
                _uiState.value = _uiState.value.copy(outOfHearts = true)
            }
        }
    }

    /** Gọi khi người dùng bấm "Tiếp tục" sau khi xem phản hồi đúng/sai. */
    fun continueToNext() {
        if (_uiState.value.outOfHearts) return
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
                timeSpentMillis = timeSpent
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

    private fun normalize(text: String): String = text.trim().lowercase()

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
