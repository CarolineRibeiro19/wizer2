package com.example.wizer2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.wizer2.repository.GroupService
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController



@Composable
fun StudentMainScreen(
    groupService: GroupService
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val currentRoute = currentRoute(navController)
            val tabs = listOf(
                ScreenItem("quizzes", Icons.Default.Person),
                ScreenItem("groups", Icons.Default.Person),
                ScreenItem("profile", Icons.Default.AccountCircle)
            )
            NavigationBar {
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
    ) { paddingValues -> // ✅ Correção aplicada aqui
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            NavHost(
                navController = navController,
                startDestination = "groups"
            ) {
                composable("quizzes") { QuizzesScreen() }
                composable("groups") {
                    GroupsScreen(navController = navController, groupService = groupService)
                }
                composable("profile") { ProfileScreen() }

                composable(
                    route = "groupDetail/{groupId}/{groupName}",
                    arguments = listOf(
                        navArgument("groupId") { type = NavType.StringType },
                        navArgument("groupName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                    val groupName = backStackEntry.arguments?.getString("groupName") ?: ""
                    GroupDetailScreen(groupId = groupId, groupName = groupName, navController = navController)
                }
            }
        }
    }
}

data class ScreenItem(val route: String, val icon: ImageVector)

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route?.substringBefore("/")
}
