package com.vocab300.app.ui.screens.worddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocab300.app.data.local.entity.VocabularyWordEntity
import com.vocab300.app.data.local.entity.WordProgressEntity
import com.vocab300.app.data.repository.ProgressRepository
import com.vocab300.app.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WordDetailUiState(
    val isLoading: Boolean = true,
    val word: VocabularyWordEntity? = null,
    val progress: WordProgressEntity? = null
)

class WordDetailViewModel(
    private val wordId: String,
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WordDetailUiState())
    val state: StateFlow<WordDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val word = vocabularyRepository.getById(wordId)
            val progress = progressRepository.getWordProgress(wordId)
            _state.value = WordDetailUiState(isLoading = false, word = word, progress = progress)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            progressRepository.toggleFavorite(wordId)
            load()
        }
    }

    fun toggleKnown() {
        viewModelScope.launch {
            progressRepository.toggleKnown(wordId)
            load()
        }
    }

    class Factory(
        private val wordId: String,
        private val vocabularyRepository: VocabularyRepository,
        private val progressRepository: ProgressRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WordDetailViewModel(wordId, vocabularyRepository, progressRepository) as T
        }
    }
}
