package com.example.wizer2.services

import com.example.wizer2.models.Group
import com.example.wizer2.models.User
import com.example.wizer2.models.GroupMember
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonObject
import android.util.Log

class GroupService(private val client: SupabaseClient) {

    suspend fun getGroupsByProfessor(professorId: String): List<Group> {
        return client.from("groups")
            .select()
            .decodeList<Group>()
            .filter { it.professorId == professorId }
    }
    suspend fun getGroupMembers(groupId: String): List<User> {
        return try {
            val userIds = client.from("group_members")
                .select(Columns.list("user_id")) {
                    filter {
                        eq("group_id", groupId)
                    }
                }
                .decodeList<Map<String, String>>()
                .mapNotNull { it["user_id"] }

            if (userIds.isEmpty()) return emptyList()

            client.from("profiles")
                .select()
                .decodeList<User>()
                .filter { it.id in userIds }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createGroup(group: Group): Group {
        client.from("groups").insert(group)
        return group
    }


    suspend fun getGroupById(groupId: String): Group? {
        Log.d("GroupService", "🔍 Buscando grupo com ID: $groupId")

        return try {
            val result = client.from("groups")
                .select {
                    filter {
                        eq("id", groupId)
                    }
                }
                .decodeList<Group>()
                .firstOrNull()

            if (result != null) {
                Log.d("GroupService", "✅ Grupo encontrado: $result")
            } else {
                Log.d("GroupService", "⚠️ Nenhum grupo encontrado com ID: $groupId")
            }

            result
        } catch (e: Exception) {
            Log.e("GroupService", "❌ Erro ao buscar grupo: ${e.localizedMessage}")
            null
        }
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

    suspend fun updateGroup(group: Group): Group {
        val updateData = buildJsonObject {
            put("name", group.name)
            put("subject_id", group.subjectId)
            put("professor_id", group.professorId)
        }

        client.from("groups")
            .update(updateData) {
                filter {
                    eq("id", group.id)
                }
            }
        return group
    }

    // 1. Buscar grupos que o usuário participa
    suspend fun getGroupsForUser(userId: String): List<Group> {
        return try {
            println("Carregando grupos para $userId")

            // Busca os registros da tabela de associação group_members
            val memberships = client.from("group_members")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<GroupMember>()

            val groupIds = memberships.map { it.group_id }

            if (groupIds.isEmpty()) return emptyList()

            // Busca todos os grupos e filtra localmente os que o usuário participa
            val allGroups = client.from("groups")
                .select()
                .decodeList<Group>()

            val userGroups = allGroups.filter { it.id in groupIds }

            println("Recebido grupos: $userGroups")
            userGroups
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 2. Entrar em grupo usando código
    suspend fun joinGroupWithCode(userId: String, code: String): Boolean {
        return try {
            // Busca o grupo com o código
            val matchingGroups = client.from("groups")
                .select {
                    filter { eq("code", code) }
                }
                .decodeList<Group>()

            if (matchingGroups.isEmpty()) return false
            val group = matchingGroups.first()

            // Verifica se já está no grupo
            val existing = client.from("group_members")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("group_id", group.id)
                    }
                }
                .decodeList<GroupMember>()

            if (existing.isNotEmpty()) return false

            // Adiciona o membro ao grupo
            client.from("group_members")
                .insert(
                    GroupMember(group_id = group.id, user_id = userId)
                )

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}



