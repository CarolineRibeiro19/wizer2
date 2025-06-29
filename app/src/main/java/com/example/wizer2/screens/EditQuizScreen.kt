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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

// TODO: Sepa Corrigir Aqui

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuizScreen(
    quizId: String,
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit,
    onQuizUpdated: () -> Unit,
    onQuizDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quizState by viewModel.quizState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form state
    var title by remember { mutableStateOf("") }
    var subjectId by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var maxScore by remember { mutableIntStateOf(100) }
    var isFormInitialized by remember { mutableStateOf(false) }

    // Dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Validation states
    var titleError by remember { mutableStateOf("") }
    var subjectError by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf("") }

    // Load quiz data when screen opens
    LaunchedEffect(quizId) {
        viewModel.selectQuiz(quizId)
    }

    // Initialize form when quiz is loaded
    LaunchedEffect(quizState.selectedQuiz) {
        quizState.selectedQuiz?.let { quiz ->
            if (!isFormInitialized) {
                title = quiz.title
                subjectId = quiz.subjectId
                code = quiz.code
                maxScore = quiz.maxScore
                isFormInitialized = true
            }
        }
    }

    // Handle success/error states
    LaunchedEffect(quizState.error) {
        quizState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Quiz") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Quiz")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            quizState.isLoading && !isFormInitialized -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading quiz...")
                    }
                }
            }

            quizState.selectedQuiz == null && !quizState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Quiz not found",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Edit Quiz Information",
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
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
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
                                    { Text("Students use this code to join the quiz") }
                                },
                                modifier = Modifier.fillMaxWidth()
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

                    // Quiz Statistics (if available)
                    quizState.selectedQuiz?.let { quiz ->
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Quiz Statistics",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Quiz ID: ${quiz.id}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Created by: ${quiz.createdBy}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
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

                                    quizState.selectedQuiz?.let { currentQuiz ->
                                        val updatedQuiz = currentQuiz.copy(
                                            title = title.trim(),
                                            subjectId = subjectId.trim(),
                                            code = code.trim(),
                                            maxScore = maxScore
                                        )

                                        // Note: You'll need to add an updateQuiz method to your ViewModel
                                        // viewModel.updateQuiz(updatedQuiz)

                                        // For now, we'll just show success and navigate back
                                        onQuizUpdated()
                                    }
                                }
                            },
                            enabled = !quizState.isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (quizState.isLoading) {
                                Text("Updating...")
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Text("Save Changes")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Quiz") },
            text = {
                Text("Are you sure you want to delete this quiz? This action cannot be undone and will also delete all associated questions and submissions.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteQuiz(quizId)
                        onQuizDeleted()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
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