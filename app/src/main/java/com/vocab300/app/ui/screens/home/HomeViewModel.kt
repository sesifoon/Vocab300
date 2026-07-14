package com.vocab300.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocab300.app.data.datastore.UserStats
import com.vocab300.app.data.repository.ProgressRepository
import com.vocab300.app.data.repository.VocabularyRepository
import com.vocab300.app.domain.LessonPlanner
import com.vocab300.app.domain.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LanguageProgressUi(
    val language: Language,
    val totalLessons: Int,
    val completedLessons: Int,
    val currentLessonIndex: Int
) {
    val progressFraction: Float
        get() = if (totalLessons == 0) 0f else completedLessons.toFloat() / totalLessons
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val userStats: UserStats? = null,
    val enProgress: LanguageProgressUi? = null,
    val koProgress: LanguageProgressUi? = null,
    val reviewWordCount: Int = 0,
    val selectedFilter: Language? = null // null = xem cả 2 ngôn ngữ
)

class HomeViewModel(
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeStats()
    }

    private fun observeStats() {
        viewModelScope.launch {
            progressRepository.userStats.collect { stats ->
                _uiState.value = _uiState.value.copy(userStats = stats, isLoading = false)
            }
        }
    }

    fun setFilter(language: Language?) {
        _uiState.value = _uiState.value.copy(selectedFilter = language)
    }

    fun loadData() {
        viewModelScope.launch {
            val enWords = vocabularyRepository.getByLanguage("EN")
            val koWords = vocabularyRepository.getByLanguage("KO")

            val enLessons = LessonPlanner.buildLessons(Language.EN, enWords.map { it.id })
            val koLessons = LessonPlanner.buildLessons(Language.KO, koWords.map { it.id })

            val enLessonProgress = progressRepository.getLessonProgress("EN")
            val koLessonProgress = progressRepository.getLessonProgress("KO")

            val enCompleted = enLessonProgress.count { it.isCompleted }
            val koCompleted = koLessonProgress.count { it.isCompleted }

            val enCurrent = (enLessonProgress.filter { it.isCompleted }.maxOfOrNull { it.lessonIndex + 1 } ?: 0)
                .coerceAtMost((enLessons.size - 1).coerceAtLeast(0))
            val koCurrent = (koLessonProgress.filter { it.isCompleted }.maxOfOrNull { it.lessonIndex + 1 } ?: 0)
                .coerceAtMost((koLessons.size - 1).coerceAtLeast(0))

            val reviewCount = progressRepository.selectWordsForReview(
                limit = Int.MAX_VALUE,
                nowMillis = System.currentTimeMillis()
            ).size

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                enProgress = LanguageProgressUi(Language.EN, enLessons.size, enCompleted, enCurrent),
                koProgress = LanguageProgressUi(Language.KO, koLessons.size, koCompleted, koCurrent),
                reviewWordCount = reviewCount
            )
        }
    }

    class Factory(
        private val vocabularyRepository: VocabularyRepository,
        private val progressRepository: ProgressRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(vocabularyRepository, progressRepository) as T
        }
    }
}
