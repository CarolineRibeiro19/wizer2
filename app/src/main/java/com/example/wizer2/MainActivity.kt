package com.example.wizer2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wizer2.services.UserService
import com.example.wizer2.ui.theme.Wizer2Theme
import com.example.wizer2.vmodels.QuizViewModel
import com.example.wizer2.vmodels.QuizViewModelFactory
import com.example.wizer2.vmodels.GroupViewModel
import com.example.wizer2.vmodels.GroupViewModelFactory

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import com.example.wizer2.screens.AuthScreen
import com.example.wizer2.screens.CreateQuizScreen
import com.example.wizer2.screens.EditQuizScreen
import com.example.wizer2.screens.ProfessorScreen
import com.example.wizer2.screens.QuizResultsScreen
import com.example.wizer2.screens.CreateGroupScreen
import com.example.wizer2.screens.EditGroupScreen
import com.example.wizer2.screens.GroupDetailsScreen
import com.example.wizer2.services.QuestionService
import com.example.wizer2.services.QuizQuestionService
import com.example.wizer2.services.QuizSubmissionService
import com.example.wizer2.services.QuizzesService
import com.example.wizer2.services.GroupService
import com.example.wizer2.services.GroupMembersService
import com.example.wizer2.services.SubjectService
import io.github.jan.supabase.auth.auth


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val supabaseclient = createSupabaseClient(
        supabaseUrl = "https://jqibivtfpbrzvtcprtsw.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpxaWJpdnRmcGJyenZ0Y3BydHN3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA4MDI0OTEsImV4cCI6MjA2NjM3ODQ5MX0.isV9ifBmL4GOv49fqvM9WGwtbzOaTWvUA29Eh8EGcIg"
    ) {
        install(Postgrest)
        install(Auth)
    }


        val userService = UserService(supabaseclient)
        val quizzesService = QuizzesService(supabaseclient)
        val questionService = QuestionService(supabaseclient)
        val quizQuestionService = QuizQuestionService(supabaseclient)
        val quizSubmissionService = QuizSubmissionService(supabaseclient)
        val groupService = GroupService(supabaseclient)
        val groupMembersService = GroupMembersService(supabaseclient)
        val subjectService = SubjectService(supabaseclient)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {            val navController = rememberNavController()

            // Create QuizViewModel factory and instance
            val quizViewModelFactory = QuizViewModelFactory(
                quizzesService = quizzesService,
                questionService = questionService,
                quizQuestionService = quizQuestionService,
                quizSubmissionService = quizSubmissionService
            )

            // Create GroupViewModel factory and instance
            val groupViewModelFactory = GroupViewModelFactory(
                groupService = groupService,
                groupMembersService = groupMembersService,
                subjectService = subjectService,
                userService = userService
            )

            Wizer2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "auth",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("auth") {
                            AuthScreen(
                                userService = userService,
                                onAuthSuccess = { user ->
                                    println("Authentication successful for user: ${user.email}, Role: ${user.role}")
                                    when (user.role.uppercase()) {
                                        "TEACHER" -> {
                                            navController.navigate("professor") {
                                                popUpTo("auth") { inclusive = true }
                                            }
                                        }
                                        "STUDENT" -> {
                                            // navigate to student screen if you want
                                        }
                                    }
                                }
                            )
                        }
                        composable("professor") {
                            val quizViewModel: QuizViewModel = viewModel(factory = quizViewModelFactory)
                            val groupViewModel: GroupViewModel = viewModel(factory = groupViewModelFactory)

                            // Get current user ID from auth
                            val currentUserId = supabaseclient.auth.currentUserOrNull()?.id

                            // Set current user ID in ViewModels
                            LaunchedEffect(currentUserId) {
                                currentUserId?.let { userId ->
                                    groupViewModel.setCurrentUserId(userId)
                                }
                            }

                            ProfessorScreen(
                                supabaseClient = supabaseclient,
                                quizViewModel = quizViewModel,
                                groupViewModel = groupViewModel,
                                onCreateQuiz = { navController.navigate("create_quiz") },
                                onEditQuiz = { quizId -> navController.navigate("edit_quiz/$quizId") },
                                onViewQuizResults = { quizId -> navController.navigate("quiz_results/$quizId") },
                                onCreateGroup = { navController.navigate("create_group") },
                                onEditGroup = { groupId -> navController.navigate("edit_group/$groupId") },
                                onViewGroupDetails = { groupId -> navController.navigate("group_details/$groupId") }
                            )
                        }

                        composable("create_quiz") {
                            val quizViewModel: QuizViewModel = viewModel(factory = quizViewModelFactory)
                            CreateQuizScreen(
                                viewModel = quizViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onQuizCreated = { navController.popBackStack() }
                            )
                        }

                        composable("edit_quiz/{quizId}") { backStackEntry ->
                            val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
                            val quizViewModel: QuizViewModel = viewModel(factory = quizViewModelFactory)
                            EditQuizScreen(
                                quizId = quizId,
                                viewModel = quizViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onQuizUpdated = { navController.popBackStack() },
                                onQuizDeleted = { navController.popBackStack() }
                            )
                        }

                        composable("quiz_results/{quizId}") { backStackEntry ->
                            val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
                            val quizViewModel: QuizViewModel = viewModel(factory = quizViewModelFactory)

                            QuizResultsScreen(
                                quizId = quizId,
                                viewModel = quizViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Group routes
                        composable("create_group") {
                            val groupViewModel: GroupViewModel = viewModel(factory = groupViewModelFactory)

                            val currentUserId = supabaseclient.auth.currentUserOrNull()?.id
                            LaunchedEffect(currentUserId) {
                                currentUserId?.let { userId ->
                                    groupViewModel.setCurrentUserId(userId)
                                }
                            }

                            CreateGroupScreen(
                                viewModel = groupViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onGroupCreated = { navController.popBackStack() }
                            )
                        }

                        composable("edit_group/{groupId}") { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                            val groupViewModel: GroupViewModel = viewModel(factory = groupViewModelFactory)

                            val currentUserId = supabaseclient.auth.currentUserOrNull()?.id
                            LaunchedEffect(currentUserId) {
                                currentUserId?.let { userId ->
                                    groupViewModel.setCurrentUserId(userId)
                                }
                            }

                            EditGroupScreen(
                                groupId = groupId,
                                viewModel = groupViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onGroupUpdated = { navController.popBackStack() },
                                onGroupDeleted = { navController.popBackStack() }
                            )
                        }

                        composable("group_details/{groupId}") { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                            val groupViewModel: GroupViewModel = viewModel(factory = groupViewModelFactory)

                            val currentUserId = supabaseclient.auth.currentUserOrNull()?.id
                            LaunchedEffect(currentUserId) {
                                currentUserId?.let { userId ->
                                    groupViewModel.setCurrentUserId(userId)
                                }
                            }

                            GroupDetailsScreen(
                                groupId = groupId,
                                viewModel = groupViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onEditGroup = { navController.navigate("edit_group/$groupId") }
                            )
                        }                        }
                }
            }
        }
    }
}
