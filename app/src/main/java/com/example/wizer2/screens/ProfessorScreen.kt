package com.example.wizer2.screens

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


sealed class ProfessorNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : ProfessorNavItem("home", "Home", Icons.Default.Home)
    object Groups : ProfessorNavItem("groups", "Groups", Icons.Default.AccountBox)
    object Quiz : ProfessorNavItem("quiz", "Quiz", Icons.Default.List)
}

@Composable
fun ProfessorBottomNav(navController: NavHostController) {
    val items = listOf(
        ProfessorNavItem.Home,
        ProfessorNavItem.Groups,
        ProfessorNavItem.Quiz
    )
    NavigationBar {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ProfessorScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { ProfessorBottomNav(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ProfessorNavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(ProfessorNavItem.Home.route) {
                HomeScreen()
            }
            composable(ProfessorNavItem.Groups.route) {
                GroupsScreen()
            }
            composable(ProfessorNavItem.Quiz.route) {
                QuizScreen()
            }
        }
    }
}


@Composable
fun HomeScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Professor Home")
    }
}

@Composable
fun GroupsScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Groups Page")
    }
}

@Composable
fun QuizScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Quiz Page")
    }
}
