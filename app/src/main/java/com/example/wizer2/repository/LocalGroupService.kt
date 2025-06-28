package com.example.wizer2.repository

import com.example.wizer2.model.Group

object LocalGroupService : GroupService {

    override suspend fun getGroupsForUser(userId: String): List<Group> {
        return listOf(
            Group(
                id = "group1",
                name = "Grupo Matemática 1",
                subjectId = "matematica",
                professorId = "prof123",
                subjectName = "Matemática"
            ),
            Group(
                id = "group2",
                name = "Grupo Física 2",
                subjectId = "fisica",
                professorId = "prof456",
                subjectName = "Física"
            )
        )
    }

    override suspend fun joinGroupWithCode(userId: String, code: String): Boolean {
        // Simulação de código válido
        return code == "GRP123"
    }

    override suspend fun getGroupDetails(groupId: String): Group? {
        return getGroupsForUser("mock").find { it.id == groupId }
    }
}
