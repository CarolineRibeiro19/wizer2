package com.example.wizer2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.wizer2.repository.SupabaseGroupService
import com.example.wizer2.services.UserService
import com.example.wizer2.ui.StudentMainScreen
import com.example.wizer2.ui.theme.Wizer2Theme
import com.example.wizer2.screens.AuthScreen
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("MainActivity", "Iniciando SupabaseClient...")

        val supabaseClient = try {
            createSupabaseClient(
                supabaseUrl = "https://jqibivtfpbrzvtcprtsw.supabase.co",
                supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpxaWJpdnRmcGJyenZ0Y3BydHN3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA4MDI0OTEsImV4cCI6MjA2NjM3ODQ5MX0.isV9ifBmL4GOv49fqvM9WGwtbzOaTWvUA29Eh8EGcIg"
            ) {
                install(Postgrest)
                install(Auth)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro ao criar SupabaseClient", e)
            throw e
        }

        val userService = UserService(supabaseClient)
        val groupService = SupabaseGroupService(supabaseClient)

        setContent {
            Wizer2Theme {
                var isAuthenticated by remember { mutableStateOf(false) }
                var isStudent by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (!isAuthenticated) {
                        AuthScreen(
                            modifier = Modifier.padding(innerPadding),
                            userService = userService,
                            onAuthSuccess = { user ->
                                isAuthenticated = true
                                isStudent = user.role == "student"
                                println("Login bem-sucedido: ${user.email} com role: ${user.role}")
                            }
                        )
                    } else {
                        if (isStudent) {
                            StudentMainScreen(groupService = groupService)
                        } else {
                            println("Não existe tela para o role teacher")
                        }
                    }
                }
            }
        }
    }
}
