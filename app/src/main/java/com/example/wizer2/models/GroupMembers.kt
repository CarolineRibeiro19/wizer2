package com.example.wizer2.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupMembers(
    @SerialName("group_id") val groupId: String,
    @SerialName("user_id") val userId: String,
    val points: Int
)
