package com.example.wizer2.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizSubmission(
    val id: String,
    @SerialName("quiz_id") val quizId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("group_id") val groupId: String,
    val score: Int
)
