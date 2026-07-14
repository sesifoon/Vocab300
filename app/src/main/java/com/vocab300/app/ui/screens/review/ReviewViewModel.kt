package com.vocab300.app.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocab300.app.data.local.entity.VocabularyWordEntity
import com.vocab300.app.data.repository.ProgressRepository
import com.vocab300.app.data.repository.VocabularyRepository
import com.vocab300.app.ui.components.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val REVIEW_WORD_LIMIT = 15

data class ReviewSessionState(
    val words: List<VocabularyWordEntity> = emptyList(),
    val currentIndex: Int = 0,
    val revealed: Boolean = false,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val finished: Boolean = false
)

class ReviewViewModel(
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<VocabularyWordEntity>>>(UiState.Loading)
    val state: StateFlow<UiState<List<VocabularyWordEntity>>> = _state.asStateFlow()

    private val _session = MutableStateFlow<ReviewSessionState?>(null)
    val session: StateFlow<ReviewSessionState?> = _session.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val progresses = progressRepository.selectWordsForReview(REVIEW_WORD_LIMIT, System.currentTimeMillis())
            if (progresses.isEmpty()) {
                _state.value = UiState.Empty("Chưa có từ nào cần ôn tập. Hãy học thêm bài mới!")
                return@launch
            }
            val words = vocabularyRepository.getByIds(progresses.map { it.wordId })
            _state.value = UiState.Success(words)
        }
    }

    fun startSession(words: List<VocabularyWordEntity>) {
        _session.value = ReviewSessionState(words = words)
    }

    fun reveal() {
        _session.value = _session.value?.copy(revealed = true)
    }

    fun answer(known: Boolean) {
        val current = _session.value ?: return
        val word = current.words.getOrNull(current.currentIndex) ?: return
        viewModelScope.launch {
            progressRepository.recordAnswer(
                word.id,
                if (known) com.vocab300.app.domain.AnswerResult.CORRECT else com.vocab300.app.domain.AnswerResult.INCORRECT,
                System.currentTimeMillis()
            )
        }
        val nextIndex = current.currentIndex + 1
        _session.value = current.copy(
            currentIndex = nextIndex,
            revealed = false,
            correctCount = current.correctCount + if (known) 1 else 0,
            wrongCount = current.wrongCount + if (!known) 1 else 0,
            finished = nextIndex >= current.words.size
        )
    }

    class Factory(
        private val vocabularyRepository: VocabularyRepository,
        private val progressRepository: ProgressRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReviewViewModel(vocabularyRepository, progressRepository) as T
        }
    }
}
