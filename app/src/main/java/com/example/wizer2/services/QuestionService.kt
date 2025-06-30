package com.example.wizer2.services

import com.example.wizer2.models.Question
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from


class QuestionService(private val client: SupabaseClient) {

    suspend fun addQuestion(question: Question) {
        client.from("questions").insert(question)
    }

    suspend fun getQuestionsBySubject(subjectId: String): List<Question> {
        return client.from("questions")
            .select()
            .decodeList<Question>()
            .filter { it.subjectId == subjectId }
    }

    suspend fun getQuestionById(id: String): Question? {
        return client.from("questions")
            .select()
            .decodeList<Question>()
            .firstOrNull { it.id == id }
    }

    suspend fun getQuestionsByIds(ids: List<String>): List<Question> {
        return client.from("questions")
            .select {
                filter {
                    "id" in ids
                }
            }

            .decodeList<Question>()
    }

    suspend fun updateQuestion(question: Question) {
        client.from("questions")
            .update(question) {
                filter {
                    eq("id", question.id)
                }
            }
    }

    suspend fun deleteQuestion(id: String) {
        client.from("questions")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }
    
    suspend fun getRandomQuestionsBySubject(subjectId: String, count: Int = 10): List<Question> {
    return try {
        client.from("questions")
            .select {
                filter { eq("subject_id", subjectId) }
            }
            .decodeList<Question>()
            .shuffled()
            .take(count)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

}
