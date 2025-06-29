package com.example.wizer2.services

import com.example.wizer2.models.Subject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class SubjectService(private val client: SupabaseClient) {

    suspend fun createSubject(subject: Subject) {
        client.from("subjects").insert(subject)
    }

    suspend fun getAllSubjects(): List<Subject> {
        return client.from("subjects")
            .select()
            .decodeList<Subject>()
    }

    suspend fun getSubjectById(id: String): Subject? {
        return client.from("subjects")
            .select()
            .decodeList<Subject>()
            .find { it.id == id }
    }

    suspend fun updateSubject(subject: Subject) {
        client.from("subjects").update(subject) {
            filter { eq("id", subject.id) }
        }
    }

    suspend fun deleteSubject(id: String) {
        client.from("subjects").delete {
            filter { eq("id", id) }
        }
    }
}
