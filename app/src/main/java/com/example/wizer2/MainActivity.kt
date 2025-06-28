package com.example.wizer2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.wizer2.services.UserService
import com.example.wizer2.ui.theme.Wizer2Theme

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import com.example.wizer2.screens.AuthScreen


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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Wizer2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AuthScreen(
                        modifier = Modifier.padding(innerPadding),
                        userService = userService,
                        onAuthSuccess = { user ->
                            // TODO: Handle successful authentication here.
                            // navigate to a different screen (e.g., home screen)
                            // For now, let's just log it or show a simple message.
                            println("Authentication successful for user: ${user.email}, Role: ${user.role}")
                        }
                    )
                }
            }
        }
    }
}


