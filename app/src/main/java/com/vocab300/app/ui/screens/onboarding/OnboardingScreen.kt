package com.vocab300.app.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocab300.app.VocabApp

@Composable
fun OnboardingScreen(app: VocabApp, onDone: () -> Unit) {
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.Factory(app.userPrefsManager)
    )
    val selectedGoal by viewModel.selectedGoal.collectAsState()
    val completed by viewModel.completed.collectAsState()

    LaunchedEffect(completed) {
        if (completed) onDone()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Chào mừng đến với Vocab300", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Học 300 từ vựng Anh – Hàn mỗi ngày, theo lộ trình từng bước",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        Text("Bạn muốn học bao nhiêu từ mỗi ngày?", style = MaterialTheme.typography.titleMedium)

        viewModel.availableGoals.forEach { goal ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .selectable(selected = goal == selectedGoal, onClick = { viewModel.selectGoal(goal) }),
                colors = CardDefaults.cardColors(
                    containerColor = if (goal == selectedGoal)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(selected = goal == selectedGoal, onClick = { viewModel.selectGoal(goal) })
                        Text("$goal từ / ngày", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.confirmAndStart() },
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
        ) {
            Text("Bắt đầu học")
        }
    }
}
