package com.vocab300.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocab300.app.data.datastore.UserPrefsManager
import com.vocab300.app.data.importer.VocabularyImporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed class SplashDestination {
    object Loading : SplashDestination()
    object GoToOnboarding : SplashDestination()
    object GoToHome : SplashDestination()
    data class Error(val message: String) : SplashDestination()
}

/**
 * Khi Splash hiển thị: chạy import 300 từ vào Room nếu chưa import (chỉ chạy 1 lần),
 * sau đó quyết định điều hướng tới Onboarding (lần đầu) hoặc Home (đã onboarding trước đó).
 */
class SplashViewModel(
    private val importer: VocabularyImporter,
    private val userPrefsManager: UserPrefsManager
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        runImportAndDecideNext()
    }

    private fun runImportAndDecideNext() {
        viewModelScope.launch {
            try {
                importer.importIfNeeded()
                val onboardingDone = userPrefsManager.userStats.map { it.onboardingDone }.first()
                _destination.value = if (onboardingDone) {
                    SplashDestination.GoToHome
                } else {
                    SplashDestination.GoToOnboarding
                }
            } catch (t: Throwable) {
                _destination.value = SplashDestination.Error(
                    t.message ?: "Không thể khởi tạo dữ liệu từ vựng."
                )
            }
        }
    }

    fun retry() {
        _destination.value = SplashDestination.Loading
        runImportAndDecideNext()
    }

    class Factory(
        private val importer: VocabularyImporter,
        private val userPrefsManager: UserPrefsManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SplashViewModel(importer, userPrefsManager) as T
        }
    }
}
