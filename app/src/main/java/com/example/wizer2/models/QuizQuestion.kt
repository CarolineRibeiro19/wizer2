package com.example.wizer2.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizQuestion(
    @SerialName("quiz_id") val quizId: String,
    @SerialName("question_id") val questionId: String,
    val position: Int
)
