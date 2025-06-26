package com.example.wizer2.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val username: String,
    val role: Role
)

@Serializable
data class UserProfileRow(
    val id: String,
    val username: String,
    val role: String,
    val user_id: String
)
