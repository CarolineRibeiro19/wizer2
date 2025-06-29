package com.example.wizer2.screens

import androidx.compose.runtime.Composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.example.wizer2.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessorScreen() {
    var selectedTab by remember { mutableStateOf(1) } // Start with Home tab

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
            0 -> QuizScreen(modifier = Modifier.padding(paddingValues))
            1 -> HomeScreen(modifier = Modifier.padding(paddingValues))
            2 -> GroupsScreen(modifier = Modifier.padding(paddingValues))
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    // MOCKED DATA - Home screen statistics and recent activity
    val totalStudents = 142
    val activeQuizzes = mockQuizzes.size
    val totalGroups = mockGroups.size
    val recentQuizzes = mockQuizzes.take(3)
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
                    value = activeQuizzes.toString(),
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
                items(recentQuizzes) { quiz ->
                    QuizCard(quiz = quiz)
                }
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
            QuickActionsSection()
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
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
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

@Composable
fun GroupPerformanceCard(group: Group) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountBox,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Avg Performance: 87%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = Color.Green
            )
        }
    }
}

@Composable
fun QuickActionsSection() {
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
                onClick = { /* Navigate to create quiz */ },
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
fun QuizScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
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
                    onClick = { /* Create new quiz */ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Quiz")
                }
            }
        }

        // MOCKED DATA - Display all quiz data
        items(mockQuizzes) { quiz ->
            QuizListItem(quiz = quiz)
        }
    }
}

@Composable
fun QuizListItem(quiz: Quizzes) {
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
                IconButton(onClick = { /* More options */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
            }
        }
    }
}

@Composable
fun GroupsScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Groups",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                FloatingActionButton(
                    onClick = { /* Create new group */ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Group")
                }
            }
        }

        // MOCKED DATA - Display all group data with member information
        items(mockGroups) { group ->
            GroupListItem(group = group)
        }
    }
}

@Composable
fun GroupListItem(group: Group) {
    // MOCKED DATA - Get members for this group
    val groupMembers = mockGroupMembers.filter { it.groupId == group.id }
    val memberCount = groupMembers.size
    val averagePoints = if (groupMembers.isNotEmpty()) {
        groupMembers.map { it.points }.average().toInt()
    } else 0

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
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$memberCount members",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Average Points: $averagePoints",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { /* More options */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
            }
        }
    }
}

// MOCKED DATA - Sample data for testing
private val mockSubjects = listOf(
    Subject("1", "Mathematics", "Advanced calculus and algebra"),
    Subject("2", "Computer Science", "Programming and algorithms"),
    Subject("3", "Physics", "Classical and quantum mechanics")
)

private val mockQuizzes = listOf(
    Quizzes("1", "MATH101", "Calculus Midterm", "1", "prof1", 100),
    Quizzes("2", "CS201", "Data Structures Quiz", "2", "prof1", 75),
    Quizzes("3", "PHY301", "Quantum Physics Test", "3", "prof1", 120),
    Quizzes("4", "MATH201", "Linear Algebra Final", "1", "prof1", 150),
    Quizzes("5", "CS301", "Algorithm Analysis", "2", "prof1", 90)
)

private val mockGroups = listOf(
    Group("1", "Advanced Mathematics", "1", "prof1"),
    Group("2", "Programming Fundamentals", "2", "prof1"),
    Group("3", "Physics Research Group", "3", "prof1"),
    Group("4", "Study Group Alpha", "1", "prof1")
)

private val mockGroupMembers = listOf(
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