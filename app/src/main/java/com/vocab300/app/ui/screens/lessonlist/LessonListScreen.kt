package com.vocab300.app.ui.screens.lessonlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocab300.app.VocabApp
import com.vocab300.app.domain.Language
import com.vocab300.app.domain.LessonNode
import com.vocab300.app.domain.LessonNodeState
import com.vocab300.app.ui.components.FullScreenLoading
import com.vocab300.app.ui.components.FullScreenMessage
import com.vocab300.app.ui.components.UiState

@Composable
fun LessonListScreen(
    app: VocabApp,
    language: Language,
    onBack: () -> Unit,
    onOpenLesson: (Int) -> Unit
) {
    val viewModel: LessonListViewModel = viewModel(
        factory = LessonListViewModel.Factory(language, app.vocabularyRepository, app.progressRepository)
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (language == Language.EN) "Lộ trình Tiếng Anh" else "Lộ trình Tiếng Hàn") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> FullScreenLoading(modifier = Modifier.padding(padding))
            is UiState.Empty -> FullScreenMessage(s.message, modifier = Modifier.padding(padding))
            is UiState.Error -> FullScreenMessage(s.message, modifier = Modifier.padding(padding))
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(s.data) { node ->
                        LessonNodeRow(node, onClick = {
                            if (node.state != LessonNodeState.LOCKED) onOpenLesson(node.lesson.lessonIndex)
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonNodeRow(node: LessonNode, onClick: () -> Unit) {
    val locked = node.state == LessonNodeState.LOCKED
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !locked, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when (node.state) {
                LessonNodeState.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                LessonNodeState.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
                LessonNodeState.LOCKED -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (node.state) {
                            LessonNodeState.COMPLETED -> MaterialTheme.colorScheme.primary
                            LessonNodeState.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
                            LessonNodeState.LOCKED -> MaterialTheme.colorScheme.outline
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (node.state) {
                    LessonNodeState.COMPLETED -> Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    LessonNodeState.IN_PROGRESS -> Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
                    LessonNodeState.LOCKED -> Icon(Icons.Filled.Lock, contentDescription = null)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(node.lesson.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = when (node.state) {
                        LessonNodeState.LOCKED -> "Đã khóa"
                        LessonNodeState.IN_PROGRESS -> "Đang học · Điểm tốt nhất: ${node.bestScorePercent}%"
                        LessonNodeState.COMPLETED -> "Hoàn thành · ${node.bestScorePercent}%"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
