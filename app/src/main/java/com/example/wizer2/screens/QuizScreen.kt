package com.example.wizer2.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wizer2.models.Quizzes
import com.example.wizer2.vmodels.QuizViewModel

// TODO: Sepa Corrigir Aqui

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onCreateQuiz: () -> Unit = {},
    onEditQuiz: (String) -> Unit = {},
    onViewQuizResults: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val quizState by viewModel.quizState.collectAsState()

    // Load quizzes when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadQuizzes()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with title and create button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Quizzes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            FloatingActionButton(
                onClick = onCreateQuiz,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Quiz")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on state
        when {
            quizState.isLoading -> {
                LoadingContent()
            }
            quizState.error != null -> {
                ErrorContent(
                    error = quizState.error!!,
                    onRetry = { viewModel.loadQuizzes() }
                )
            }
            quizState.quizzes.isEmpty() -> {
                EmptyQuizzesContent(onCreateQuiz = onCreateQuiz)
            }
            else -> {
                QuizList(
                    quizzes = quizState.quizzes,
                    onEditQuiz = onEditQuiz,
                    onDeleteQuiz = { viewModel.deleteQuiz(it) },
                    onViewResults = onViewQuizResults,
                    onShareQuiz = { /* Implement share functionality */ }
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading quizzes...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error: $error",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            FloatingActionButton(
                onClick = onRetry,
                modifier = Modifier.size(48.dp)
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyQuizzesContent(onCreateQuiz: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No quizzes yet",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create your first quiz to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            FloatingActionButton(
                onClick = onCreateQuiz
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Quiz")
            }
        }
    }
}

@Composable
private fun QuizList(
    quizzes: List<Quizzes>,
    onEditQuiz: (String) -> Unit,
    onDeleteQuiz: (String) -> Unit,
    onViewResults: (String) -> Unit,
    onShareQuiz: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(quizzes) { quiz ->
            QuizListItem(
                quiz = quiz,
                onEdit = { onEditQuiz(quiz.id) },
                onDelete = { onDeleteQuiz(quiz.id) },
                onViewResults = { onViewResults(quiz.id) },
                onShare = { onShareQuiz(quiz.id) }
            )
        }
    }
}

@Composable
fun QuizListItem(
    quiz: Quizzes,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onViewResults: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quiz.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Subject: ${quiz.subjectId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Quiz Code: ${quiz.code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Max Score: ${quiz.maxScore} points",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { showDropdownMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }

                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Quiz") },
                            onClick = {
                                showDropdownMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("View Results") },
                            onClick = {
                                showDropdownMenu = false
                                onViewResults()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Quiz") },
                            onClick = {
                                showDropdownMenu = false
                                onShare()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Share, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showDropdownMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizCard(quiz: Quizzes) {
    Card(
        modifier = Modifier.width(200.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = quiz.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Subject: ${quiz.subjectId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Code: ${quiz.code}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Max Score: ${quiz.maxScore}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}