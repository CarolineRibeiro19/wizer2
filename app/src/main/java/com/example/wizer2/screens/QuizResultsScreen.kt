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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wizer2.models.QuizSubmission
import com.example.wizer2.models.Quizzes
import com.example.wizer2.vmodels.QuizViewModel
import kotlin.math.roundToInt

// TODO: Sepa Corrigir Aqui

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultsScreen(
    quizId: String,
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quizState by viewModel.quizState.collectAsState()
    val submissionState by viewModel.submissionState.collectAsState()
    val questionState by viewModel.questionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Submissions", "Analytics")

    // Load quiz and submissions when screen opens
    LaunchedEffect(quizId) {
        viewModel.selectQuiz(quizId)
        viewModel.loadQuizSubmissions(quizId)
    }

    // Handle errors
    LaunchedEffect(quizState.error, submissionState.error) {
        quizState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
        submissionState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = quizState.selectedQuiz?.title ?: "Quiz Results",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadQuizSubmissions(quizId) }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(
                        onClick = {
                            // Export functionality - could be implemented later
                            // For now, just refresh the data
                            viewModel.loadQuizSubmissions(quizId)
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Export Results")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.loadQuizSubmissions(quizId) }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(text = title)
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> OverviewTab(
                    quiz = quizState.selectedQuiz,
                    submissions = submissionState.submissions,
                    questionCount = questionState.questions.size,
                    isLoading = submissionState.isLoading
                )
                1 -> SubmissionsTab(
                    submissions = submissionState.submissions,
                    isLoading = submissionState.isLoading
                )
                2 -> AnalyticsTab(
                    submissions = submissionState.submissions,
                    isLoading = submissionState.isLoading
                )
            }
        }
    }
}

@Composable
private fun OverviewTab(
    quiz: com.example.wizer2.models.Quizzes?,
    submissions: List<QuizSubmission>,
    questionCount: Int,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quiz Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Quiz Information",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    quiz?.let {
                        Text("Title: ${it.title}")
                        Text("Subject: ${it.subjectId}")
                        Text("Questions: $questionCount")
                    }
                }
            }
        }

        // Statistics Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val totalSubmissions = submissions.size
                    val averageScore = if (submissions.isNotEmpty()) {
                        submissions.map { it.score }.average()
                    } else 0.0
                    val passRate = if (submissions.isNotEmpty()) {
                        submissions.count { it.score >= 70 } / submissions.size.toDouble() * 100
                    } else 0.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatisticItem(
                            icon = Icons.Default.Person,
                            value = totalSubmissions.toString(),
                            label = "Submissions"
                        )
                        StatisticItem(
                            icon = Icons.Default.KeyboardArrowUp,
                            value = "${averageScore.roundToInt()}%",
                            label = "Average Score"
                        )
                        StatisticItem(
                            icon = Icons.Default.KeyboardArrowUp,
                            value = "${passRate.roundToInt()}%",
                            label = "Pass Rate"
                        )
                    }
                }
            }
        }

        // Recent Submissions
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Recent Submissions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (submissions.isEmpty()) {
                        Text(
                            text = "No submissions yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        submissions.take(5).forEach { submission ->
                            SubmissionListItem(submission = submission)
                            if (submission != submissions.take(5).last()) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmissionsTab(
    submissions: List<QuizSubmission>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (submissions.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No submissions found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(submissions) { submission ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                SubmissionListItem(
                    submission = submission,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsTab(
    submissions: List<QuizSubmission>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Score Distribution
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Score Distribution",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (submissions.isNotEmpty()) {
                        val scoreRanges = listOf(
                            "0-20%" to submissions.count { it.score < 20 },
                            "21-40%" to submissions.count { it.score in 21..40 },
                            "41-60%" to submissions.count { it.score in 41..60 },
                            "61-80%" to submissions.count { it.score in 61..80 },
                            "81-100%" to submissions.count { it.score > 80 }
                        )

                        scoreRanges.forEach { (range, count) ->
                            ScoreRangeItem(
                                range = range,
                                count = count,
                                total = submissions.size
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        Text(
                            text = "No data available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Performance Metrics
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Performance Metrics",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (submissions.isNotEmpty()) {
                        val scores = submissions.map { it.score }
                        val minScore = scores.minOrNull() ?: 0
                        val maxScore = scores.maxOrNull() ?: 0
                        val medianScore = scores.sorted().let {
                            if (it.size % 2 == 0) {
                                (it[it.size / 2 - 1] + it[it.size / 2]) / 2
                            } else {
                                it[it.size / 2]
                            }
                        }

                        MetricItem("Minimum Score", "${minScore}%")
                        MetricItem("Maximum Score", "${maxScore}%")
                        MetricItem("Median Score", "${medianScore}%")
                    } else {
                        Text(
                            text = "No data available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SubmissionListItem(
    submission: QuizSubmission,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "User: ${submission.userId}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Group: ${submission.groupId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        }

        Text(
            text = "${submission.score}%",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = when {
                submission.score >= 80 -> Color.Green
                submission.score >= 60 -> Color(0xFFFF9800) // Orange
                else -> Color.Red
            }
        )
    }
}

@Composable
private fun ScoreRangeItem(
    range: String,
    count: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val percentage = if (total > 0) count.toFloat() / total else 0f

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = range,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$count students",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}