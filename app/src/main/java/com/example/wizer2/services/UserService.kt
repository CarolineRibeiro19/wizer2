package com.example.wizer2.services

import com.example.wizer2.models.Role
import com.example.wizer2.models.User
import com.example.wizer2.models.UserProfile
import com.example.wizer2.models.UserProfileRow
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
                createUserProfile(supabaseUser.id, username, role)

                User(
                    id = supabaseUser.id,
                    email = email,
                    password = ""
                )
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
            supabaseUser?.let {
                User(
                    id = it.id,
                    email = it.email ?: email,
                    password = "" // Don't store password
                )
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
            supabaseUser?.let {
                User(
                    id = it.id,
                    email = it.email ?: "",
                    password = ""
                )
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
                // Update username in user_profiles table
                postgrest.from("user_profiles")
                    .update(mapOf("username" to newUsername)) {
                        filter {
                            eq("user_id", currentUser.id)
                        }
                    }

                // Return updated user
                User(
                    id = currentUser.id,
                    email = currentUser.email ?: "",
                    password = ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getUserProfile(): UserProfile? {
        return try {
            val currentUser = auth.currentUserOrNull()
            if (currentUser != null) {
                val profileRows = postgrest.from("user_profiles")
                    .select(columns = Columns.list("id", "username", "role", "user_id")) {
                        filter {
                            eq("user_id", currentUser.id)
                        }
                    }
                    .decodeList<UserProfileRow>()

                if (profileRows.isNotEmpty()) {
                    val profileRow = profileRows.first()
                    UserProfile(
                        id = profileRow.id,
                        username = profileRow.username,
                        role = Role.valueOf(profileRow.role)
                    )
                } else {
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

    private suspend fun createUserProfile(userId: String, username: String, role: Role) {
        try {
            postgrest.from("user_profiles")
                .insert(mapOf(
                    "user_id" to userId,
                    "username" to username,
                    "role" to role.toString()
                ))
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun isAuthenticated(): Boolean {
        return auth.currentUserOrNull() != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUserOrNull()?.id
    }

    fun User.toUserProfile(username: String, role: Role): UserProfile {
        return UserProfile(
            id = this.id,
            username = username,
            role = role
        )
    }
}
