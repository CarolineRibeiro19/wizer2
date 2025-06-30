package com.example.wizer2.services

import com.example.wizer2.models.Group
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GroupService(private val client: SupabaseClient) {

    suspend fun getGroupsByProfessor(professorId: String): List<Group> {
        return client.from("groups")
            .select {
                filter {
                    eq("professor_id", professorId)
                }
            }
            .decodeList<Group>()
    }

    suspend fun createGroup(group: Group): Group {
        return client.from("groups")
            .insert(group)
            .decodeSingle<Group>()
    }

    suspend fun getGroupById(groupId: String): Group? {
        return client.from("groups")
            .select {
                filter {
                    eq("id", groupId)
                }
            }
            .decodeList<Group>()
            .firstOrNull()
    }

    suspend fun deleteGroup(groupId: String) {
        client.from("groups")
            .delete {
                filter {
                    eq("id", groupId)
                }
            }
    }

    suspend fun updateGroup(group: Group): Group {
        val updateData = buildJsonObject {
            put("name", group.name)
            put("subject_id", group.subjectId)
            put("description", group.description)
        }

        return client.from("groups")
            .update(updateData) {
                filter {
                    eq("id", group.id)
                }
            }
            .decodeSingle<Group>()
    }

    suspend fun getAllGroups(): List<Group> {
        return client.from("groups")
            .select()
            .decodeList<Group>()
    }
}