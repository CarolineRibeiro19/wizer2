package com.example.wizer2.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.wizer2.services.GroupService
import com.example.wizer2.services.QuizSubmissionService
import com.example.wizer2.services.QuizzesService
import com.example.wizer2.services.UserService

@Composable
fun StudentMainScreen(
    groupService: GroupService,
    userService: UserService,
    quizSubmissionService: QuizSubmissionService,
    quizzesService: QuizzesService,
    userId: String,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val tabs = listOf(
        ScreenItem("quizzes", Icons.Default.List),
        ScreenItem("grupos", Icons.Default.Home),
        ScreenItem("perfil", Icons.Default.AccountCircle)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = currentRoute(navController)
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.route) },
                        label = { Text(tab.route.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = "groups", Modifier.padding(padding)) {
            composable("groups") {
                GroupsScreen(navController, groupService, userId)
            }
            composable("quizzes") {
                QuizzesScreen(
                    userId = userId,
                    quizSubmissionService = quizSubmissionService,
                    quizzesService = quizzesService
                )
            }
            composable("profile") {
                ProfileScreen(userService, onLogout, navController)
            }
            composable(
                route = "groupdetail/{groupId}",
                arguments = listOf(
                    navArgument("groupId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: ""

                GroupDetailScreen(groupId = groupId, groupService = groupService, userService = userService)
            }
            composable(
                "exercises/{subjectId}",
                arguments = listOf(navArgument("subjectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
                ExercisesScreen(
                    subjectId = subjectId,
                    questionService = questionService,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

data class ScreenItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route?.substringBefore("/")
}



