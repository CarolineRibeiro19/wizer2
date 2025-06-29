package com.example.wizer2.services


import com.example.wizer2.models.Group
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GroupService(private val client: SupabaseClient) {

    suspend fun getGroupsByProfessor(professorId: String): List<Group> {
        return client.from("groups")
            .select()
            .decodeList<Group>()
            .filter { it.professorId == professorId }
    }

    suspend fun createGroup(group: Group) {
        client.from("groups").insert(group)
    }

    suspend fun getGroupById(groupId: String): Group? {
        return client.from("groups")
            .select()
            .decodeList<Group>()
            .firstOrNull { it.id == groupId }
    }

    suspend fun deleteGroup(groupId: String) {
        client.from("groups")
            .delete {
                filter {
                    eq("id", groupId)
                }
            }
    }

    suspend fun updateGroupName(groupId: String, newName: String) {
        val updateData = buildJsonObject {
            put("name", newName)
        }

        client.from("groups")
            .update(updateData) {
                filter {
                    eq("id", groupId)
                }
            }
    }
}
