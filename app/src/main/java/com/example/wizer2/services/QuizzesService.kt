package com.example.wizer2.services

import com.example.wizer2.models.Quizzes
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class QuizzesService(private val client: SupabaseClient) {

    suspend fun createQuiz(quiz: Quizzes) {
        client.from("quizzes").insert(quiz)
    }

    suspend fun getAllQuizzes(): List<Quizzes> {
        return client.from("quizzes")
            .select()
            .decodeList<Quizzes>()
    }

    suspend fun getQuizById(id: String): Quizzes? {
        return client.from("quizzes")
            .select()
            .decodeList<Quizzes>()
            .find { it.id == id }
    }

    suspend fun getQuizzesBySubject(subjectId: String): List<Quizzes> {
        return client.from("quizzes")
            .select()
            .decodeList<Quizzes>()
            .filter { it.subjectId == subjectId }
    }

    suspend fun deleteQuiz(id: String) {
        client.from("quizzes").delete {
            filter {
                eq("id", id)
            }
        }
    }
}


