package com.example.wizer2.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: String,
    val name: String,
    @SerialName("subject_id") val subjectId : String,
    @SerialName("professor_id") val professorId: String,
    val description: String? = null
)
