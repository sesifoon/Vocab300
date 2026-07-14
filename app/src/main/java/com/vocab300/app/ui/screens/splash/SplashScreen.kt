package com.vocab300.app.ui.screens.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocab300.app.VocabApp

@Composable
fun SplashScreen(
    app: VocabApp,
    onGoToOnboarding: () -> Unit,
    onGoToHome: () -> Unit
) {
    val viewModel: SplashViewModel = viewModel(
        factory = SplashViewModel.Factory(app.vocabularyImporter, app.userPrefsManager)
    )
    val destination by viewModel.destination.collectAsState()

    LaunchedEffect(destination) {
        when (destination) {
            is SplashDestination.GoToOnboarding -> onGoToOnboarding()
            is SplashDestination.GoToHome -> onGoToHome()
            else -> Unit
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📘", style = MaterialTheme.typography.headlineLarge)
        Text("Vocab300", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Học 300 từ vựng Anh – Hàn mỗi ngày",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        when (val dest = destination) {
            is SplashDestination.Loading -> CircularProgressIndicator()
            is SplashDestination.Error -> {
                Text(dest.message, style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { viewModel.retry() }, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Thử lại")
                }
            }
            else -> Unit
        }
    }
}
