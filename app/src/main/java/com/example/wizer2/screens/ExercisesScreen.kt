package com.example.wizer2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wizer2.models.Question
import com.example.wizer2.services.QuestionService
import kotlinx.coroutines.launch

@Composable
fun ExercisesScreen(
    subjectId: String,
    questionService: QuestionService,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(subjectId) {
        scope.launch {
            try {
                questions = questionService.getRandomQuestionsBySubject(subjectId, count = 10)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loading = false
            }
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {

            Text("Random Exercises", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(questions) { question ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(question.text, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            question.options.forEachIndexed { index, option ->
                                Text("• $option")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "✔ Correct Answer: ${question.options[question.correctIndex]}",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Back")
            }
        }
    }
}
