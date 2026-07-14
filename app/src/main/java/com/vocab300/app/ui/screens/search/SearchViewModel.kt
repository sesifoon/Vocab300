package com.vocab300.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocab300.app.data.local.entity.VocabularyWordEntity
import com.vocab300.app.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val vocabularyRepository: VocabularyRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<VocabularyWordEntity>>(emptyList())
    val results: StateFlow<List<VocabularyWordEntity>> = _results.asStateFlow()

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        viewModelScope.launch {
            _results.value = if (newQuery.isBlank()) emptyList() else vocabularyRepository.search(newQuery)
        }
    }

    class Factory(private val vocabularyRepository: VocabularyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(vocabularyRepository) as T
        }
    }
}
