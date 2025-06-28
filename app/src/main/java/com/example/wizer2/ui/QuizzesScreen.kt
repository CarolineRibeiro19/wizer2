package com.example.wizer2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Quiz(
    val id: String,
    val title: String,
    val score: Int,
    val total: Int
)

@Composable
fun QuizzesScreen() {
    val quizzes = listOf(
        Quiz("1", "Quiz de Álgebra", 8, 10),
        Quiz("2", "Quiz de História", 6, 10),
        Quiz("3", "Quiz de Redes", 10, 10)
    )

    var showDialog by remember { mutableStateOf(false) }
    var quizCode by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Quiz")
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Quizzes Respondidos",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(quizzes) { quiz ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(quiz.title, style = MaterialTheme.typography.titleMedium)
                            Text("Nota: ${quiz.score}/${quiz.total}")
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Entrar em Quiz") },
            text = {
                Column {
                    Text("Insira o código fornecido pelo professor:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quizCode,
                        onValueChange = { quizCode = it },
                        placeholder = { Text("Ex: QZ123") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Aqui você poderá verificar o código com a Supabase no futuro
                    println("Código inserido: $quizCode")
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
