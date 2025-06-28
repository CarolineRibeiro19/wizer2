package com.example.wizer2.repository

import com.example.wizer2.model.Group

interface GroupService {
    suspend fun getGroupsForUser(userId: String): List<Group>
    suspend fun joinGroupWithCode(userId: String, code: String): Boolean
    suspend fun getGroupDetails(groupId: String): Group?
}
