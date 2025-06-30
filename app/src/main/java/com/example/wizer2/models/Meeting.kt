package com.example.wizer2.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Meeting(
    val id: String,
    @SerialName("group_id") val groupId: String,
    val name: String,
    val location: String,
    val date: String, // ISO 8601 string
    @SerialName("created_at") val createdAt: String? = null
)
