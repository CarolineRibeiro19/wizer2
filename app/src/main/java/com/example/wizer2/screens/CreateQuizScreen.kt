package com.example.wizer2.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wizer2.models.Quizzes
import com.example.wizer2.vmodels.QuizViewModel
import java.util.UUID

// TODO: Sepa Corrigir Aqui

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuizScreen(
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit,
    onQuizCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quizState by viewModel.quizState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form state
    var title by remember { mutableStateOf("") }
    var subjectId by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var professorId by remember { mutableStateOf("prof1") } // You might want to get this from user session
    var maxScore by remember { mutableIntStateOf(100) }

    // Validation states
    var titleError by remember { mutableStateOf("") }
    var subjectError by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf("") }

    // Generate random quiz code when screen loads
    LaunchedEffect(Unit) {
        code = generateQuizCode()
    }

    // Handle quiz creation success
    LaunchedEffect(quizState.isLoading, quizState.error) {
        if (!quizState.isLoading && quizState.error == null && quizState.quizzes.isNotEmpty()) {
            onQuizCreated()
        }
    }

    // Show error message
    LaunchedEffect(quizState.error) {
        quizState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Quiz") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quiz Information",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Quiz Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            titleError = ""
                        },
                        label = { Text("Quiz Title") },
                        placeholder = { Text("Enter quiz title") },
                        isError = titleError.isNotEmpty(),
                        supportingText = if (titleError.isNotEmpty()) {
                            { Text(titleError) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Subject ID
                    OutlinedTextField(
                        value = subjectId,
                        onValueChange = {
                            subjectId = it
                            subjectError = ""
                        },
                        label = { Text("Subject") },
                        placeholder = { Text("e.g., MATH101, CS201") },
                        isError = subjectError.isNotEmpty(),
                        supportingText = if (subjectError.isNotEmpty()) {
                            { Text(subjectError) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quiz Code
                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            code = it.uppercase()
                            codeError = ""
                        },
                        label = { Text("Quiz Code") },
                        placeholder = { Text("Unique quiz code") },
                        isError = codeError.isNotEmpty(),
                        supportingText = if (codeError.isNotEmpty()) {
                            { Text(codeError) }
                        } else {
                            { Text("Students will use this code to join the quiz") }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Button(
                                onClick = { code = generateQuizCode() }
                            ) {
                                Text("Generate")
                            }
                        }
                    )

                    // Max Score
                    OutlinedTextField(
                        value = maxScore.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { score ->
                                if (score >= 0) maxScore = score
                            }
                        },
                        label = { Text("Maximum Score") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (validateForm(title, subjectId, code) { titleErr, subjectErr, codeErr ->
                                titleError = titleErr
                                subjectError = subjectErr
                                codeError = codeErr
                            }) {
                            val newQuiz = Quizzes(
                                id = UUID.randomUUID().toString(),
                                subjectId = subjectId.trim(),
                                title = title.trim(),
                                code = code.trim(),
                                createdBy = professorId,
                                maxScore = maxScore
                            )
                            viewModel.createQuiz(newQuiz)
                        }
                    },
                    enabled = !quizState.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (quizState.isLoading) {
                        Text("Creating...")
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Text("Create Quiz")
                        }
                    }
                }
            }
        }
    }
}

private fun validateForm(
    title: String,
    subjectId: String,
    code: String,
    onError: (titleError: String, subjectError: String, codeError: String) -> Unit
): Boolean {
    var isValid = true
    var titleError = ""
    var subjectError = ""
    var codeError = ""

    if (title.trim().isEmpty()) {
        titleError = "Quiz title is required"
        isValid = false
    } else if (title.trim().length < 3) {
        titleError = "Title must be at least 3 characters"
        isValid = false
    }

    if (subjectId.trim().isEmpty()) {
        subjectError = "Subject is required"
        isValid = false
    }

    if (code.trim().isEmpty()) {
        codeError = "Quiz code is required"
        isValid = false
    } else if (code.trim().length < 4) {
        codeError = "Code must be at least 4 characters"
        isValid = false
    }

    onError(titleError, subjectError, codeError)
    return isValid
}

private fun generateQuizCode(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..6)
        .map { chars.random() }
        .joinToString("")
}