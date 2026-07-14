package com.vocab300.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocab300.app.data.datastore.UserStats
import com.vocab300.app.data.repository.ProgressRepository
import com.vocab300.app.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StatsUiState(
    val userStats: UserStats? = null,
    val wordsLearned: Int = 0,
    val totalWords: Int = 0, // v1.1.0: lấy động từ DB, không hard-code 300
    val lessonsCompleted: Int = 0,
    val masteryBreakdown: Map<Int, Int> = emptyMap() // mức 0..5 -> số từ
)

class StatsViewModel(
    private val progressRepository: ProgressRepository,
    private val vocabularyRepository: VocabularyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            progressRepository.userStats.collect { stats ->
                _state.value = _state.value.copy(userStats = stats)
            }
        }
        loadProgressStats()
    }

    private fun loadProgressStats() {
        viewModelScope.launch {
            val allProgress = progressRepository.getAllWordProgress()
            val enLessons = progressRepository.getLessonProgress("EN")
            val koLessons = progressRepository.getLessonProgress("KO")
            val totalWords = vocabularyRepository.totalCount()

            val breakdown = (0..5).associateWith { level -> allProgress.count { it.masteryLevel == level } }

            _state.value = _state.value.copy(
                wordsLearned = allProgress.count { it.lastReviewedAt != null },
                totalWords = totalWords,
                lessonsCompleted = enLessons.count { it.isCompleted } + koLessons.count { it.isCompleted },
                masteryBreakdown = breakdown
            )
        }
    }

    class Factory(
        private val progressRepository: ProgressRepository,
        private val vocabularyRepository: VocabularyRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatsViewModel(progressRepository, vocabularyRepository) as T
        }
    }
}
