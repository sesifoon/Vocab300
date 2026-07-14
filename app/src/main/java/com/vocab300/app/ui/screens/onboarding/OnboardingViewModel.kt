package com.vocab300.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocab300.app.data.datastore.UserPrefsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(private val userPrefsManager: UserPrefsManager) : ViewModel() {

    val availableGoals = listOf(5, 10, 15, 20)

    private val _selectedGoal = MutableStateFlow(10)
    val selectedGoal: StateFlow<Int> = _selectedGoal.asStateFlow()

    private val _completed = MutableStateFlow(false)
    val completed: StateFlow<Boolean> = _completed.asStateFlow()

    fun selectGoal(goal: Int) {
        _selectedGoal.value = goal
    }

    fun confirmAndStart() {
        viewModelScope.launch {
            userPrefsManager.setOnboardingDone(dailyGoal = _selectedGoal.value)
            _completed.value = true
        }
    }

    class Factory(private val userPrefsManager: UserPrefsManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(userPrefsManager) as T
        }
    }
}
