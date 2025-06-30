package com.example.wizer2.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MeetingMedia(
    val id: String,
    @SerialName("meeting_id") val meetingId: String,
    @SerialName("media_url") val mediaUrl: String,
    @SerialName("uploaded_by") val uploadedBy: String?,
    @SerialName("uploaded_at") val uploadedAt: String?
)
