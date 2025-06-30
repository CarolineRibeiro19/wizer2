package com.example.wizer2.models

import kotlinx.serialization.Serializable

@Serializable
data class GroupMember(
    val user_id: String,
    val group_id: String
)


