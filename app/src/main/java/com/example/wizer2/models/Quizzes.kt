package com.example.wizer2.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Quizzes(
    val id: String,
    val code: String,
    val title: String,
    @SerialName("subject_id") val subjectId: String,
    @SerialName("created_by") val createdBy: String,
    val maxScore: Int
)


