package com.vocab300.app.ui.screens.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocab300.app.VocabApp
import com.vocab300.app.ui.components.FullScreenLoading
import com.vocab300.app.ui.components.FullScreenMessage
import com.vocab300.app.ui.components.UiState

@Composable
fun ReviewScreen(app: VocabApp, onBack: () -> Unit) {
    val viewModel: ReviewViewModel = viewModel(
        factory = ReviewViewModel.Factory(app.vocabularyRepository, app.progressRepository)
    )
    val state by viewModel.state.collectAsState()
    val session by viewModel.session.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ôn tập") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        val currentSession = session
        when {
            currentSession != null && !currentSession.finished -> FlashcardBody(
                currentSession,
                onReveal = { viewModel.reveal() },
                onAnswer = { viewModel.answer(it) },
                modifier = Modifier.padding(padding)
            )
            currentSession != null && currentSession.finished -> ReviewDoneBody(
                currentSession,
                onBack = onBack,
                modifier = Modifier.padding(padding)
            )
            else -> when (val s = state) {
                is UiState.Loading -> FullScreenLoading(modifier = Modifier.padding(padding))
                is UiState.Empty -> ReviewEmptyBody(s.message, onGoHome = onBack, modifier = Modifier.padding(padding))
                is UiState.Error -> FullScreenMessage(s.message, modifier = Modifier.padding(padding))
                is UiState.Success -> ReviewListBody(
                    words = s.data,
                    onStart = { viewModel.startSession(s.data) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun ReviewEmptyBody(message: String, onGoHome: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onGoHome, modifier = Modifier.fillMaxWidth()) {
            Text("Về trang chủ để học từ mới")
        }
    }
}

@Composable
private fun ReviewListBody(
    words: List<com.vocab300.app.data.local.entity.VocabularyWordEntity>,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("${words.size} từ đang chờ ôn tập", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text("Bắt đầu ôn tập")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(words) { word ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(word.word, style = MaterialTheme.typography.titleMedium)
                        Text(word.meaningVi, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashcardBody(
    session: ReviewSessionState,
    onReveal: () -> Unit,
    onAnswer: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val word = session.words.getOrNull(session.currentIndex) ?: return
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "${session.currentIndex + 1}/${session.words.size}",
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !session.revealed) { onReveal() }
        ) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(word.word, style = MaterialTheme.typography.headlineMedium)
                if (session.revealed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(word.meaningVi, style = MaterialTheme.typography.titleMedium)
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Chạm để xem nghĩa", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (session.revealed) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onAnswer(false) }) { Text("Chưa nhớ") }
                Button(onClick = { onAnswer(true) }) { Text("Đã nhớ") }
            }
        }
    }
}

@Composable
private fun ReviewDoneBody(session: ReviewSessionState, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hoàn thành ôn tập!", style = MaterialTheme.typography.headlineMedium)
        Text("Đã nhớ: ${session.correctCount} · Chưa nhớ: ${session.wrongCount}")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Về trang chủ")
        }
    }
}
