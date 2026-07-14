package com.vocab300.app.ui.components

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Empty(val message: String) : UiState<Nothing>()
    data class Error(val message: String) : UiState<Nothing>()
}
