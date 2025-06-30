package com.example.wizer2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wizer2.models.QuizSubmission
import com.example.wizer2.services.QuizSubmissionService
import com.example.wizer2.services.QuizzesService
import kotlinx.coroutines.launch

@Composable
fun QuizzesScreen(
    userId: String,
    quizSubmissionService: QuizSubmissionService,
    quizzesService: QuizzesService
) {
    val scope = rememberCoroutineScope()
    var submissions by remember { mutableStateOf<List<QuizSubmission>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var quizCode by remember { mutableStateOf("") }

    fun loadSubmissions() {
        scope.launch {
            loading = true
            submissions = quizSubmissionService.getSubmissionsByUserId(userId)
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        loadSubmissions()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Inserir código do quiz")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Text("Quizzes Respondidos", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (submissions.isEmpty()) {
                    Text("Você ainda não respondeu nenhum quiz.")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(submissions) { submission ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Quiz ID: ${submission.quizId}", style = MaterialTheme.typography.titleMedium)
                                    Text("Nota: ${submission.score}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Inserir Código do Quiz") },
                text = {
                    OutlinedTextField(
                        value = quizCode,
                        onValueChange = { quizCode = it },
                        placeholder = { Text("Código do quiz") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        // TODO: Navegar para responder quiz com o código
                        showDialog = false
                        quizCode = ""
                    }) {
                        Text("Entrar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        quizCode = ""
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}







