package com.example.wizer2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.example.wizer2.repository.SupabaseGroupService
import com.example.wizer2.ui.StudentMainScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.wizer2.ui.theme.Wizer2Theme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Cria o cliente Supabase
        val supabase = createSupabaseClient(
            supabaseUrl = "https://jqibivtfpbrzvtcprtsw.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpxaWJpdnRmcGJyenZ0Y3BydHN3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA4MDI0OTEsImV4cCI6MjA2NjM3ODQ5MX0.isV9ifBmL4GOv49fqvM9WGwtbzOaTWvUA29Eh8EGcIg"
        ) {
            install(Postgrest)
        }

        val groupService = SupabaseGroupService(supabase)

        setContent {
            Wizer2Theme {
                // Aqui você já carrega a tela principal do aluno
                StudentMainScreen(groupService = groupService)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
