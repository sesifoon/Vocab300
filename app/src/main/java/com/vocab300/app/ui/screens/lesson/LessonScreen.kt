package com.vocab300.app.ui.screens.lesson

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocab300.app.VocabApp
import com.vocab300.app.domain.Language
import com.vocab300.app.domain.QuestionType
import com.vocab300.app.ui.components.ConfirmDialog
import com.vocab300.app.ui.components.FullScreenLoading
import com.vocab300.app.ui.components.FullScreenMessage
import com.vocab300.app.ui.components.LessonTopProgressBar

@Composable
fun LessonScreen(
    app: VocabApp,
    language: Language,
    lessonIndex: Int,
    onExit: () -> Unit,
    onFinished: (
        total: Int, correct: Int, xp: Int, timeMs: Long, passed: Boolean
    ) -> Unit
) {
    val viewModel: LessonViewModel = viewModel(
        factory = LessonViewModel.Factory(
            language, lessonIndex,
            app.vocabularyRepository, app.progressRepository, app.userPrefsManager, app.ttsManager
        )
    )
    val state by viewModel.uiState.collectAsState()
    var showExitConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.finished) {
        if (state.finished) {
            val r = state.result
            if (r != null) {
                onFinished(r.totalQuestions, r.correctFirstTry, r.xpEarned, r.timeSpentMillis, r.passed)
            }
        }
    }

    if (showExitConfirm) {
        ConfirmDialog(
            title = "Thoát bài học?",
            message = "Tiến độ của câu hiện tại sẽ không được lưu. Bạn có chắc muốn thoát?",
            confirmText = "Thoát",
            cancelText = "Tiếp tục học",
            onConfirm = { showExitConfirm = false; onExit() },
            onDismiss = { showExitConfirm = false }
        )
    }

    if (state.outOfHearts) {
        ConfirmDialog(
            title = "Bạn đã hết tim",
            message = "Hãy quay lại vào ngày mai hoặc ôn tập để lấy lại tim.",
            confirmText = "Về trang chủ",
            cancelText = "Đóng",
            onConfirm = onExit,
            onDismiss = onExit
        )
    }

    Scaffold(
        topBar = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showExitConfirm = true }) {
                    Icon(Icons.Filled.Close, contentDescription = "Thoát bài học")
                }
                LessonTopProgressBar(
                    progress = if (state.totalQuestions == 0) 0f else state.questionNumber.toFloat() / state.totalQuestions,
                    hearts = state.hearts,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { padding ->
        when {
            state.isLoading -> FullScreenLoading(modifier = Modifier.padding(padding))
            state.errorMessage != null -> FullScreenMessage(state.errorMessage!!, modifier = Modifier.padding(padding))
            state.question != null -> QuestionContent(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun QuestionContent(state: LessonUiState, viewModel: LessonViewModel, modifier: Modifier = Modifier) {
    val question = state.question ?: return

    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = when (question.type) {
                QuestionType.WORD_TO_MEANING -> "Chọn nghĩa đúng của từ"
                QuestionType.MEANING_TO_WORD -> "Chọn từ đúng với nghĩa sau"
                QuestionType.TYPE_ANSWER -> "Nhập từ bạn nhìn thấy"
                QuestionType.MATCHING -> "Ghép từ với nghĩa tương ứng"
                QuestionType.LISTENING -> "Nghe và chọn đáp án đúng"
            },
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (question.type) {
            QuestionType.WORD_TO_MEANING, QuestionType.MEANING_TO_WORD ->
                ChoiceQuestionBody(question.prompt, question.options, state, viewModel, showSpeaker = false)

            QuestionType.LISTENING ->
                ChoiceQuestionBody(question.prompt, question.options, state, viewModel, showSpeaker = true)

            QuestionType.TYPE_ANSWER -> TypeAnswerBody(state, viewModel)

            QuestionType.MATCHING -> MatchingBody(state, viewModel)
        }

        Spacer(modifier = Modifier.weight(1f))

        FeedbackBar(state, viewModel)
    }
}

@Composable
private fun ChoiceQuestionBody(
    prompt: String,
    options: List<String>,
    state: LessonUiState,
    viewModel: LessonViewModel,
    showSpeaker: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(prompt, style = MaterialTheme.typography.headlineMedium)
            if (showSpeaker) {
                IconButton(onClick = { viewModel.playTts() }) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = "Nghe phát âm")
                }
            }
        }
    }

    if (state.ttsMessage != null) {
        Text(
            state.ttsMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        if (showSpeaker) {
            OutlinedButton(onClick = { viewModel.skipListeningQuestion() }, modifier = Modifier.fillMaxWidth()) {
                Text("Bỏ qua câu này")
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    options.forEach { option ->
        val isAnswered = state.feedback != AnswerFeedback.None
        val isCorrectOption = isAnswered && option == correctAnswerOf(state)
        val isThisWrongPicked = isAnswered && state.feedback is AnswerFeedback.Incorrect &&
            option != correctAnswerOf(state)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable(enabled = !isAnswered) { viewModel.submitChoiceOrTypedAnswer(option) },
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isCorrectOption -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Text(
                option,
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun correctAnswerOf(state: LessonUiState): String = state.question?.correctAnswer ?: ""

@Composable
private fun TypeAnswerBody(state: LessonUiState, viewModel: LessonViewModel) {
    var text by remember(state.question?.id) { mutableStateOf("") }
    val isAnswered = state.feedback != AnswerFeedback.None

    Text(state.question?.prompt.orEmpty(), style = MaterialTheme.typography.headlineMedium)
    Spacer(modifier = Modifier.height(20.dp))
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        enabled = !isAnswered,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Nhập từ bạn nghe/nhìn thấy") }
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { viewModel.submitChoiceOrTypedAnswer(text) },
        enabled = !isAnswered && text.isNotBlank(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Kiểm tra")
    }
}

@Composable
private fun MatchingBody(state: LessonUiState, viewModel: LessonViewModel) {
    val question = state.question ?: return
    val leftItems = question.matchingPairs.map { it.first }
    val rightItems = remember(question.id) { question.matchingPairs.map { it.second }.shuffled() }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            leftItems.forEachIndexed { index, word ->
                val matched = index in state.matchedPairIndexes
                val selected = index == state.selectedLeftIndex
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !matched) { viewModel.selectMatchingLeft(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            matched -> MaterialTheme.colorScheme.tertiaryContainer
                            selected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text(word, modifier = Modifier.padding(12.dp))
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            rightItems.forEach { meaning ->
                val matchedMeaning = state.matchedPairIndexes.any { question.matchingPairs[it].second == meaning }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !matchedMeaning && state.selectedLeftIndex != null) {
                            val leftIdx = state.selectedLeftIndex!!
                            val correctMeaning = question.matchingPairs[leftIdx].second
                            viewModel.selectMatchingRight(
                                rightIndex = 0,
                                leftWord = question.matchingPairs[leftIdx].first,
                                rightMeaning = meaning,
                                correctMeaning = correctMeaning
                            )
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (matchedMeaning) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(meaning, modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}

@Composable
private fun FeedbackBar(state: LessonUiState, viewModel: LessonViewModel) {
    val feedback = state.feedback
    AnimatedVisibility(visible = feedback != AnswerFeedback.None, enter = fadeIn(), exit = fadeOut()) {
        val isCorrect = feedback is AnswerFeedback.Correct
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(
                    if (isCorrect) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                )
                .padding(20.dp)
        ) {
            Text(
                if (isCorrect) "Chính xác! 🎉" else "Chưa đúng",
                style = MaterialTheme.typography.titleMedium
            )
            if (feedback is AnswerFeedback.Incorrect && feedback.correctAnswer.isNotBlank()) {
                Text("Đáp án đúng là: ${feedback.correctAnswer}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { viewModel.continueToNext() }, modifier = Modifier.fillMaxWidth()) {
                Text("Tiếp tục")
            }
        }
    }
}
