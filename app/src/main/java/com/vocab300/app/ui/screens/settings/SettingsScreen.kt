package com.vocab300.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocab300.app.VocabApp
import com.vocab300.app.data.datastore.DarkModeOption
import com.vocab300.app.ui.components.ConfirmDialog

@Composable
fun SettingsScreen(app: VocabApp, onBack: () -> Unit) {
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(app.userPrefsManager, app.progressRepository)
    )
    val stats by viewModel.userStats.collectAsState()
    val resetDone by viewModel.resetDone.collectAsState()
    var showResetConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(resetDone) {
        if (resetDone) viewModel.clearResetFlag()
    }

    if (showResetConfirm) {
        ConfirmDialog(
            title = "Đặt lại tiến độ",
            message = "Toàn bộ tiến độ học, XP, tim và chuỗi ngày sẽ bị xóa vĩnh viễn. Bạn có chắc chắn?",
            confirmText = "Đặt lại",
            cancelText = "Hủy",
            onConfirm = { showResetConfirm = false; viewModel.resetProgress() },
            onDismiss = { showResetConfirm = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                Text("Mục tiêu học mỗi ngày", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15, 20).forEach { goal ->
                        FilterChip(
                            selected = stats?.dailyGoal == goal,
                            onClick = { viewModel.setDailyGoal(goal) },
                            label = { Text("$goal từ") }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Âm thanh", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = stats?.soundEnabled ?: true,
                    onCheckedChange = { viewModel.setSoundEnabled(it) }
                )
            }

            Column {
                Text("Chế độ tối", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = stats?.darkMode == DarkModeOption.SYSTEM,
                        onClick = { viewModel.setDarkMode(DarkModeOption.SYSTEM) },
                        label = { Text("Theo hệ thống") }
                    )
                    FilterChip(
                        selected = stats?.darkMode == DarkModeOption.LIGHT,
                        onClick = { viewModel.setDarkMode(DarkModeOption.LIGHT) },
                        label = { Text("Sáng") }
                    )
                    FilterChip(
                        selected = stats?.darkMode == DarkModeOption.DARK,
                        onClick = { viewModel.setDarkMode(DarkModeOption.DARK) },
                        label = { Text("Tối") }
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Đặt lại tiến độ", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Xóa toàn bộ XP, tim, chuỗi ngày và tiến độ học đã lưu.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                    androidx.compose.material3.OutlinedButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Đặt lại tiến độ")
                    }
                }
            }
        }
    }
}
