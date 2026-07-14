package com.vocab300.app.ui.screens.worddetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.vocab300.app.domain.Language
import com.vocab300.app.ui.components.FullScreenLoading
import com.vocab300.app.ui.components.FullScreenMessage
import com.vocab300.app.ui.components.WordIllustration

@Composable
fun WordDetailScreen(app: VocabApp, wordId: String, onBack: () -> Unit) {
    val viewModel: WordDetailViewModel = viewModel(
        factory = WordDetailViewModel.Factory(wordId, app.vocabularyRepository, app.progressRepository)
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết từ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> FullScreenLoading(modifier = Modifier.padding(padding))
            state.word == null -> FullScreenMessage("Không tìm thấy từ này", modifier = Modifier.padding(padding))
            else -> {
                val word = state.word!!
                val progress = state.progress
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        WordIllustration.emojiFor(word.category, word.imageKey),
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(word.word, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            app.ttsManager.speak(
                                word.word,
                                if (word.language == "EN") Language.EN else Language.KO
                            )
                        }) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = "Nghe phát âm")
                        }
                    }
                    Text(word.meaningVi, style = MaterialTheme.typography.titleLarge)
                    if (!word.pronunciation.isNullOrBlank()) {
                        Text("Phiên âm: ${word.pronunciation}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (!word.example.isNullOrBlank()) {
                        Text("Ví dụ: ${word.example}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("Chủ đề: ${word.category}", style = MaterialTheme.typography.bodyMedium)

                    if (progress != null) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Mức thành thạo: ${progress.masteryLevel}/5")
                                Text("Đúng: ${progress.timesCorrect} · Sai: ${progress.timesWrong}")
                            }
                        }
                    }

                    Button(onClick = { viewModel.toggleFavorite() }, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            if (progress?.isFavorite == true) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null
                        )
                        Text(
                            if (progress?.isFavorite == true) "Bỏ yêu thích" else "Thêm vào yêu thích",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    OutlinedButton(onClick = { viewModel.toggleKnown() }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Text(
                            if (progress?.isKnown == true) "Bỏ đánh dấu đã thuộc" else "Đánh dấu đã thuộc",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
