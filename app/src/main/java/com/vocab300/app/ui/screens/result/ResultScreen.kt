package com.vocab300.app.ui.screens.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResultScreen(
    total: Int,
    correct: Int,
    xp: Int,
    timeMs: Long,
    passed: Boolean,
    onGoHome: () -> Unit,
    onRetry: () -> Unit
) {
    val percent = if (total == 0) 0 else (correct * 100) / total
    val seconds = timeMs / 1000

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (passed) "🎉" else "😥", style = MaterialTheme.typography.headlineLarge)
        Text(
            if (passed) "Bạn đã hoàn thành bài học!" else "Chưa đạt, hãy thử lại nhé",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer1()

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ResultRow("Điểm", "$percent%")
                ResultRow("Số câu đúng", "$correct/$total")
                ResultRow("XP nhận được", "+$xp XP")
                ResultRow("Thời gian học", "${seconds}s")
            }
        }

        Spacer1()

        Button(onClick = onGoHome, modifier = Modifier.fillMaxWidth()) {
            Text("Về trang chủ")
        }
        if (!passed) {
            OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Text("Học lại")
            }
        }
    }
}

@Composable
private fun Spacer1() {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 16.dp))
}

@Composable
private fun ResultRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
