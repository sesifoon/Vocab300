package com.vocab300.app.ui.screens.search

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
import androidx.compose.material3.OutlinedTextField
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
import com.vocab300.app.ui.components.FullScreenMessage

@Composable
fun SearchScreen(app: VocabApp, onBack: () -> Unit, onOpenWord: (String) -> Unit) {
    val viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory(app.vocabularyRepository))
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tìm kiếm từ vựng") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nhập từ hoặc nghĩa cần tìm…") }
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 12.dp))

            if (query.isNotBlank() && results.isEmpty()) {
                FullScreenMessage("Không tìm thấy từ phù hợp")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(results) { word ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onOpenWord(word.id) }
                        ) {
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
}
