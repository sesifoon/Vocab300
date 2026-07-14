package com.vocab300.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocab300.app.data.datastore.DarkModeOption
import com.vocab300.app.data.datastore.UserPrefsManager
import com.vocab300.app.data.datastore.UserStats
import com.vocab300.app.data.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPrefsManager: UserPrefsManager,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    val userStats: StateFlow<UserStats?> = MutableStateFlow<UserStats?>(null).also { flow ->
        viewModelScope.launch {
            userPrefsManager.userStats.collect { flow.value = it }
        }
    }.asStateFlow()

    private val _resetDone = MutableStateFlow(false)
    val resetDone: StateFlow<Boolean> = _resetDone.asStateFlow()

    fun setDailyGoal(goal: Int) {
        viewModelScope.launch { userPrefsManager.setDailyGoal(goal) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { userPrefsManager.setSoundEnabled(enabled) }
    }

    fun setDarkMode(option: DarkModeOption) {
        viewModelScope.launch { userPrefsManager.setDarkMode(option) }
    }

    fun resetProgress() {
        viewModelScope.launch {
            progressRepository.resetAllProgress()
            _resetDone.value = true
        }
    }

    fun clearResetFlag() {
        _resetDone.value = false
    }

    class Factory(
        private val userPrefsManager: UserPrefsManager,
        private val progressRepository: ProgressRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(userPrefsManager, progressRepository) as T
        }
    }
}
