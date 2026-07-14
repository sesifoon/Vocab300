package com.vocab300.app.ui.screens.lessonlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocab300.app.data.repository.ProgressRepository
import com.vocab300.app.data.repository.VocabularyRepository
import com.vocab300.app.domain.Language
import com.vocab300.app.domain.LessonNode
import com.vocab300.app.domain.LessonPlanner
import com.vocab300.app.domain.LessonUnlockChecker
import com.vocab300.app.ui.components.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LessonListViewModel(
    private val language: Language,
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<LessonNode>>>(UiState.Loading)
    val state: StateFlow<UiState<List<LessonNode>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val words = vocabularyRepository.getByLanguage(language.name)
            if (words.isEmpty()) {
                _state.value = UiState.Empty("Chưa có dữ liệu từ vựng cho ngôn ngữ này.")
                return@launch
            }
            val lessons = LessonPlanner.buildLessons(language, words.map { it.id })
            val lessonProgress = progressRepository.getLessonProgress(language.name)
            val scores = lessonProgress.associate { it.lessonIndex to it.bestScorePercent }
            val completed = lessonProgress.filter { it.isCompleted }.map { it.lessonIndex }.toSet()

            val nodes = LessonUnlockChecker.buildLessonNodes(lessons, scores, completed)
            _state.value = UiState.Success(nodes)
        }
    }

    class Factory(
        private val language: Language,
        private val vocabularyRepository: VocabularyRepository,
        private val progressRepository: ProgressRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LessonListViewModel(language, vocabularyRepository, progressRepository) as T
        }
    }
}
