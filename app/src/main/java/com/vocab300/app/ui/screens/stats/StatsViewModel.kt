package com.vocab300.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocab300.app.data.datastore.UserStats
import com.vocab300.app.data.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StatsUiState(
    val userStats: UserStats? = null,
    val wordsLearned: Int = 0,
    val lessonsCompleted: Int = 0,
    val masteryBreakdown: Map<Int, Int> = emptyMap() // mức 0..5 -> số từ
)

class StatsViewModel(private val progressRepository: ProgressRepository) : ViewModel() {

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

            val breakdown = (0..5).associateWith { level -> allProgress.count { it.masteryLevel == level } }

            _state.value = _state.value.copy(
                wordsLearned = allProgress.count { it.lastReviewedAt != null },
                lessonsCompleted = enLessons.count { it.isCompleted } + koLessons.count { it.isCompleted },
                masteryBreakdown = breakdown
            )
        }
    }

    class Factory(private val progressRepository: ProgressRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatsViewModel(progressRepository) as T
        }
    }
}
