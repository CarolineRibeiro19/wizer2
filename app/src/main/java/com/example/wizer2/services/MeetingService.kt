package com.example.wizer2.services

import com.example.wizer2.models.Meeting
import com.example.wizer2.models.MeetingMedia
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class MeetingService(private val client: SupabaseClient) {

    suspend fun getMeetingsByGroup(groupId: String): List<Meeting> {
        return client.from("meetings")
            .select()
            .decodeList<Meeting>()
            .filter { it.groupId == groupId }
    }

    suspend fun createMeeting(meeting: Meeting) {
        client.from("meetings").insert(meeting)
    }

    suspend fun getMediaForMeeting(meetingId: String): List<MeetingMedia> {
        return client.from("meeting_media")
            .select()
            .decodeList<MeetingMedia>()
            .filter { it.meetingId == meetingId }
    }
}
