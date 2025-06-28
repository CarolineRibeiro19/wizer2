package com.example.wizer2.repository

import com.example.wizer2.model.Group
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

class SupabaseGroupService(
    private val client: SupabaseClient
) : GroupService {

    override suspend fun getGroupsForUser(userId: String): List<Group> {
        // Passo 1: obter todos os group_ids onde user_id = X
        val memberships = client.from("group_members")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<GroupMemberRow>()

        val groupIds = memberships.map { it.group_id }

        if (groupIds.isEmpty()) return emptyList()

        // Passo 2: buscar os grupos um por um (sem usar `in`)
        val groups = mutableListOf<Group>()
        for (id in groupIds) {
            try {
                val group = client.from("groups")
                    .select {
                        filter {
                            eq("id", id)
                        }
                        single()
                    }
                    .decodeSingle<Group>()
                groups.add(group)
            } catch (_: Exception) {
                // Pular caso não encontre o grupo
            }
        }

        return groups
    }

    override suspend fun joinGroupWithCode(userId: String, code: String): Boolean {
        val group = client.from("groups")
            .select {
                filter {
                    eq("code", code)
                }
                single()
            }
            .decodeSingleOrNull<Group>() ?: return false

        client.from("group_members").insert(
            mapOf(
                "group_id" to group.id,
                "user_id" to userId
            )
        )
        return true
    }

    override suspend fun getGroupDetails(groupId: String): Group? {
        return try {
            client.from("groups")
                .select {
                    filter {
                        eq("id", groupId.toInt())
                    }
                    single()
                }
                .decodeSingle<Group>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Serializable
data class GroupMemberRow(
    val user_id: String,
    val group_id: Int
)
