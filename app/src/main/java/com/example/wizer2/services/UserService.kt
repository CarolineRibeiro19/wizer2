package com.example.wizer2.services

import com.example.wizer2.models.Role
import com.example.wizer2.models.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class UserService(
    private val supabaseClient: SupabaseClient
) {

    private val auth: Auth = supabaseClient.auth
    private val postgrest: Postgrest = supabaseClient.postgrest

    suspend fun signUp(
        email: String,
        password: String,
        username: String,
        role: Role
    ): User? {
        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val supabaseUser = auth.currentUserOrNull()

            if (supabaseUser != null) {
                val user = User(
                    id = supabaseUser.id,
                    email = email,
                    username = username,
                    role = role.toString(),
                )
                postgrest.from("profiles").insert(
                    user
                )
                user

            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun signIn(email: String, password: String): User? {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val supabaseUser = auth.currentUserOrNull()
            if (supabaseUser != null) {
                val currentUser = postgrest.from("profiles").select {
                    filter {
                        eq("id", supabaseUser.id)
                    }
                }.decodeList<User>()

                if (currentUser.isNotEmpty()) {
                    val user = currentUser.first()
                    User(
                        id = user.id,
                        email = user.email,
                        role = user.role,
                        username = user.username
                    )
                }else {
                    null
                }
            }else{
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val supabaseUser = auth.currentUserOrNull()
            if (supabaseUser != null) {
                val currentUser = postgrest.from("profiles").select {
                    filter {
                        eq("id", supabaseUser.id)
                    }
                }.decodeList<User>()

                if (currentUser.isNotEmpty()) {
                    val user = currentUser.first()
                    User(
                        id = user.id,
                        email = user.email,
                        role = user.role,
                        username = user.username
                    )
                }else {
                    null
                }

            }else{
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateUsername(newUsername: String): User? {
        return try {
            val currentUser = auth.currentUserOrNull()
            if (currentUser != null) {
                postgrest.from("profiles")
                    .update(mapOf("username" to newUsername)) {
                        filter {
                            eq("id", currentUser.id)
                        }
                    }

                val newUser = postgrest.from("profiles").select {
                    filter {
                        eq("id", currentUser.id)
                    }
                }.decodeList<User>()

                if (newUser.isNotEmpty()) {
                    val user = newUser.first()
                    User(
                        id = user.id,
                        email = user.email,
                        role = user.role,
                        username = user.username
                    )
                }else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun isAuthenticated(): Boolean {
        return auth.currentUserOrNull() != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUserOrNull()?.id
    }

}