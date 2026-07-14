package com.vocab300.app.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocab300.app.VocabApp

/** Nhãn mô tả cho từng mức mastery 0-5 (chỉ đổi nhãn hiển thị, KHÔNG đổi thuật toán mastery). */
private val MASTERY_LABELS = mapOf(
    0 to "Chưa học",
    1 to "Mới làm quen",
    2 to "Đang nhớ",
    3 to "Khá nhớ",
    4 to "Ghi nhớ tốt",
    5 to "Thành thạo"
)

@Composable
fun StatsScreen(app: VocabApp, onBack: () -> Unit) {
    val viewModel: StatsViewModel = viewModel(
        factory = StatsViewModel.Factory(app.progressRepository, app.vocabularyRepository)
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê cá nhân") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatRow("Tổng XP", "${state.userStats?.totalXp ?: 0}")
                    StatRow("Chuỗi ngày học", "🔥 ${state.userStats?.streakDays ?: 0}")
                    // v1.1.0: tổng số từ lấy động từ DB (vocabularyRepository.totalCount()), không hard-code 300.
                    StatRow("Từ đã học", "${state.wordsLearned}/${state.totalWords}")
                    StatRow("Bài đã hoàn thành", "${state.lessonsCompleted}")
                }
            }

            Text("Mức độ thành thạo", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    (0..5).forEach { level ->
                        val count = state.masteryBreakdown[level] ?: 0
                        val total = state.masteryBreakdown.values.sum().coerceAtLeast(1)
                        Column {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Mức $level — ${MASTERY_LABELS[level]}",
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Text("$count từ")
                            }
                            LinearProgressIndicator(
                                progress = { count.toFloat() / total },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, modifier = Modifier.weight(1f, fill = false))
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
