package com.vocab300.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocab300.app.VocabApp
import com.vocab300.app.domain.Language

@Composable
fun HomeScreen(
    app: VocabApp,
    onOpenLessonList: (Language) -> Unit,
    onOpenReview: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenFavorites: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(app.vocabularyRepository, app.progressRepository)
    )
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vocab300") },
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Filled.Search, contentDescription = "Tìm kiếm từ vựng")
                    }
                    IconButton(onClick = onOpenFavorites) {
                        Icon(Icons.Filled.Star, contentDescription = "Từ yêu thích")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Cài đặt")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { StatsRow(state) }

            item {
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = state.selectedFilter == null,
                        onClick = { viewModel.setFilter(null) },
                        label = { Text("Tất cả") }
                    )
                    FilterChip(
                        selected = state.selectedFilter == Language.EN,
                        onClick = { viewModel.setFilter(Language.EN) },
                        label = { Text("Tiếng Anh") }
                    )
                    FilterChip(
                        selected = state.selectedFilter == Language.KO,
                        onClick = { viewModel.setFilter(Language.KO) },
                        label = { Text("Tiếng Hàn") }
                    )
                }
            }

            if (state.selectedFilter == null || state.selectedFilter == Language.EN) {
                item {
                    state.enProgress?.let {
                        LanguageProgressCard(it, "Tiếng Anh", onOpenLessonList = { onOpenLessonList(Language.EN) })
                    }
                }
            }
            if (state.selectedFilter == null || state.selectedFilter == Language.KO) {
                item {
                    state.koProgress?.let {
                        LanguageProgressCard(it, "Tiếng Hàn", onOpenLessonList = { onOpenLessonList(Language.KO) })
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Từ cần ôn", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${state.reviewWordCount} từ đang chờ ôn tập",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                        Button(onClick = onOpenReview, modifier = Modifier.fillMaxWidth()) {
                            Text("Ôn tập ngay")
                        }
                    }
                }
            }

            item {
                Button(onClick = onOpenStats, modifier = Modifier.fillMaxWidth()) {
                    Text("Xem thống kê cá nhân")
                }
            }
        }
    }
}

@Composable
private fun StatsRow(state: HomeUiState) {
    val stats = state.userStats
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatChip(label = "XP hôm nay", value = "${stats?.xpToday ?: 0}/${(stats?.dailyGoal ?: 10) * 10}", modifier = Modifier.weight(1f))
        StatChip(label = "Chuỗi ngày", value = "🔥 ${stats?.streakDays ?: 0}", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(72.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(label, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun LanguageProgressCard(
    progress: LanguageProgressUi,
    title: String,
    onOpenLessonList: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text("${progress.completedLessons}/${progress.totalLessons} bài")
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.progressFraction },
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onOpenLessonList, modifier = Modifier.fillMaxWidth()) {
                Text(if (progress.completedLessons == 0) "Bắt đầu học" else "Tiếp tục bài học")
            }
        }
    }
}
