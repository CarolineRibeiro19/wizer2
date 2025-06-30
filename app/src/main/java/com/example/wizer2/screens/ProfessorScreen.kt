package com.example.wizer2.screens

import androidx.compose.runtime.Composable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wizer2.models.*
import com.example.wizer2.services.QuestionService
import com.example.wizer2.services.QuizQuestionService
import com.example.wizer2.services.QuizSubmissionService
import com.example.wizer2.services.QuizzesService
import com.example.wizer2.vmodels.QuizViewModel
import com.example.wizer2.vmodels.QuizViewModelFactory
import io.github.jan.supabase.SupabaseClient

@Composable
fun ProfessorScreen(
    viewModel: QuizViewModel,
    onNavigateToCreateQuiz: () -> Unit,
    onNavigateToEditQuiz: (String) -> Unit,
    onNavigateToQuizResults: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(1) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Quiz") },
                    label = { Text("Quiz") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBox, contentDescription = "Groups") },
                    label = { Text("Groups") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> QuizScreen(
                modifier = Modifier.padding(paddingValues),
                viewModel = viewModel,
                onCreateQuiz = onNavigateToCreateQuiz,
                onEditQuiz = onNavigateToEditQuiz,
                onViewQuizResults = onNavigateToQuizResults
            )
            1 -> HomeScreen(
                modifier = Modifier.padding(paddingValues),
                onNavigateToCreateQuiz = onNavigateToCreateQuiz
            )

            2 -> GroupsScreen(modifier = Modifier.padding(paddingValues))
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToCreateQuiz: () -> Unit
)
 {
    // MOCKED DATA - Home screen statistics and recent activity
    val totalStudents = 142
    val totalGroups = mockGroups.size
    val topPerformingGroups = mockGroups.take(2)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Welcome back, Professor!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Students",
                    value = totalStudents.toString(),
                    icon = Icons.Default.Person,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Active Quizzes",
                    value = "20",
                    icon = Icons.Default.List,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Groups",
                    value = totalGroups.toString(),
                    icon = Icons.Default.AccountBox,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Recent Quizzes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add recent quizzes here
            }
        }

        item {
            Text(
                text = "Top Performing Groups",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(topPerformingGroups) { group ->
            GroupPerformanceCard(group = group)
        }

        item {
            QuickActionsSection(onNavigateToCreateQuiz = onNavigateToCreateQuiz)
        }
    }
}

@Composable
fun QuickActionsSection(onNavigateToCreateQuiz: () -> Unit) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToCreateQuiz,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Quiz")
            }
            OutlinedButton(
                onClick = { /* Navigate to analytics */ },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analytics")
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}




// MOCKED DATA - Sample data for testing
val mockSubjects = listOf(
    Subject("1", "Mathematics", "Advanced calculus and algebra"),
    Subject("2", "Computer Science", "Programming and algorithms"),
    Subject("3", "Physics", "Classical and quantum mechanics")
)



val mockGroups = listOf(
    Group("1", "Advanced Mathematics", "1", "prof1"),
    Group("2", "Programming Fundamentals", "2", "prof1"),
    Group("3", "Physics Research Group", "3", "prof1"),
    Group("4", "Study Group Alpha", "1", "prof1")
)

val mockGroupMembers = listOf(
    GroupMembers("1", "student1", 85),
    GroupMembers("1", "student2", 92),
    GroupMembers("1", "student3", 78),
    GroupMembers("2", "student4", 88),
    GroupMembers("2", "student5", 91),
    GroupMembers("2", "student6", 83),
    GroupMembers("3", "student7", 95),
    GroupMembers("3", "student8", 87),
    GroupMembers("4", "student9", 79),
    GroupMembers("4", "student10", 84)
)