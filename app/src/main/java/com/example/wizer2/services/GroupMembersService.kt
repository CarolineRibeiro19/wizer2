package com.example.wizer2.services

import com.example.wizer2.models.GroupMembers
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GroupMembersService(private val client: SupabaseClient) {

    suspend fun addMember(groupMember: GroupMembers) {
        client.from("group_members").insert(groupMember)
    }

    suspend fun getMembersByGroup(groupId: String): List<GroupMembers> {
        return client.from("group_members")
            .select()
            .decodeList<GroupMembers>()
            .filter { it.groupId == groupId }
    }

    suspend fun updatePoints(groupId: String, userId: String, newPoints: Int) {
        val updateData = buildJsonObject {
            put("points", newPoints)
        }

        client.from("group_members")
            .update(updateData) {
                filter {
                    eq("group_id", groupId)
                    eq("user_id", userId)
                }
            }
    }

    suspend fun removeMember(groupId: String, userId: String) {
        client.from("group_members")
            .delete {
                filter {
                    eq("group_id", groupId)
                    eq("user_id", userId)
                }
            }
    }

    suspend fun getMember(groupId: String, userId: String): GroupMembers? {
        return client.from("group_members")
            .select()
            .decodeList<GroupMembers>()
            .firstOrNull { it.groupId == groupId && it.userId == userId }
    }
}
