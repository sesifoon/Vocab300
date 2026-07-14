package com.vocab300.app.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocab300.app.VocabApp
import com.vocab300.app.ui.components.FullScreenLoading
import com.vocab300.app.ui.components.FullScreenMessage
import com.vocab300.app.ui.components.UiState

@Composable
fun FavoritesScreen(app: VocabApp, onBack: () -> Unit, onOpenWord: (String) -> Unit) {
    val viewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModel.Factory(app.vocabularyRepository, app.progressRepository)
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Từ yêu thích") },
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
            is UiState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(s.data) { word ->
                    Card(modifier = Modifier.fillMaxWidth(), onClick = { onOpenWord(word.id) }) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(word.word, style = MaterialTheme.typography.titleMedium)
                            Text(word.meaningVi, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
