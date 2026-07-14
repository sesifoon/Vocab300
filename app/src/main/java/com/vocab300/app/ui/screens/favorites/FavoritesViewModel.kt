package com.vocab300.app.ui.screens.favorites

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

class FavoritesViewModel(
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<VocabularyWordEntity>>>(UiState.Loading)
    val state: StateFlow<UiState<List<VocabularyWordEntity>>> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            progressRepository.observeFavorites().collect { favorites ->
                if (favorites.isEmpty()) {
                    _state.value = UiState.Empty("Bạn chưa đánh dấu từ yêu thích nào")
                } else {
                    val words = vocabularyRepository.getByIds(favorites.map { it.wordId })
                    _state.value = UiState.Success(words)
                }
            }
        }
    }

    class Factory(
        private val vocabularyRepository: VocabularyRepository,
        private val progressRepository: ProgressRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoritesViewModel(vocabularyRepository, progressRepository) as T
        }
    }
}
