package com.example.wizer2.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    @SerialName("subject_id") val subjectId: String,
    @SerialName("created_by") val createdBy: String,
    val type: String,
    val text: String,
    val options: List<String>,
    @SerialName("correct_index") val correctIndex: Int,
    @SerialName("expected_answer") val expectedAnswer: String,
    val score: Int
)
