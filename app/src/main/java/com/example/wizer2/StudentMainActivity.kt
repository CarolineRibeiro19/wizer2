package com.example.wizer2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.wizer2.repository.SupabaseGroupService
import com.example.wizer2.ui.StudentMainScreen
import com.example.wizer2.ui.theme.Wizer2Theme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class StudentMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val supabase = createSupabaseClient(
            supabaseUrl = "https://jqibivtfpbrzvtcprtsw.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpxaWJpdnRmcGJyenZ0Y3BydHN3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA4MDI0OTEsImV4cCI6MjA2NjM3ODQ5MX0.isV9ifBmL4GOv49fqvM9WGwtbzOaTWvUA29Eh8EGcIg"
        ) {
            install(io.github.jan.supabase.postgrest.Postgrest)
        }

        val groupService = SupabaseGroupService(supabase)

        setContent {
            Wizer2Theme {
                StudentMainScreen(groupService = groupService)
            }
        }
    }
}
