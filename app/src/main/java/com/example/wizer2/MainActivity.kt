package com.example.wizer2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.wizer2.models.User
import com.example.wizer2.screens.AuthScreen
import com.example.wizer2.screens.StudentMainScreen
import com.example.wizer2.services.*
import com.example.wizer2.ui.theme.Wizer2Theme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val supabaseClient = createSupabaseClient(
            supabaseUrl = "https://jqibivtfpbrzvtcprtsw.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpxaWJpdnRmcGJyenZ0Y3BydHN3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA4MDI0OTEsImV4cCI6MjA2NjM3ODQ5MX0.isV9ifBmL4GOv49fqvM9WGwtbzOaTWvUA29Eh8EGcIg"
        ) {
            install(Postgrest)
            install(Auth)
        }

        val userService = UserService(supabaseClient)
        val groupService = GroupService(supabaseClient)
        val quizSubmissionService = QuizSubmissionService(supabaseClient)
        val quizzesService = QuizzesService(supabaseClient)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Wizer2Theme {
                var currentUser by remember { mutableStateOf<User?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (currentUser == null) {
                        AuthScreen(
                            modifier = Modifier.padding(innerPadding),
                            userService = userService,
                            onAuthSuccess = { user ->
                                currentUser = user
                            }
                        )
                    } else {
                        if (currentUser!!.role == "STUDENT") {
                            StudentMainScreen(
                                groupService = groupService,
                                userService = userService,
                                quizSubmissionService = quizSubmissionService,
                                quizzesService = quizzesService,
                                userId = currentUser!!.id,
                                onLogout = {
                                    currentUser = null // Faz logout local
                                }
                            )
                        } else {
                            throw IllegalStateException("Ainda não há suporte para o role: ${currentUser!!.role}")
                        }
                    }
                }
            }
        }
    }
}



