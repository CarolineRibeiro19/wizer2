package com.example.wizer2.model

import kotlinx.serialization.Serializable

@Serializable
data class Quiz(
    val id: String,
    val title: String,
    val code: String,
    val subjectId: String,
    val maxScore: Int,
    val createdAt: String
)
