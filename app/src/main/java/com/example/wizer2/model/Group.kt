package com.example.wizer2.model

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Group(
    val id: String,
    val name: String,
    val subjectId: String? = null,
    val professorId: String,
    val createdAt: String? = null,
    val subjectName: String? = null, // opcional, preenchido manualmente
)
