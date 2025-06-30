package com.example.wizer2.services

import com.example.wizer2.models.QuizSubmission
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class QuizSubmissionService(private val client: SupabaseClient) {

    suspend fun submitQuiz(submission: QuizSubmission) {
        client.from("quiz_submissions").insert(submission)
    }

    suspend fun getSubmissionsForQuiz(quizId: String): List<QuizSubmission> {
        return client.from("quiz_submissions")
            .select()
            .decodeList<QuizSubmission>()
            .filter { it.quizId == quizId }
    }

    suspend fun getUserSubmission(userId: String, quizId: String): QuizSubmission? {
        return client.from("quiz_submissions")
            .select()
            .decodeList<QuizSubmission>()
            .find { it.userId == userId && it.quizId == quizId }
    }

    suspend fun getSubmissionsByUserId(userId: String): List<QuizSubmission> {
        return client.from("quiz_submissions")
            .select()
            .decodeList<QuizSubmission>()
            .filter { it.userId == userId }
    }

    suspend fun deleteSubmission(submissionId: String) {
        client.from("quiz_submissions")
            .delete {
                filter {
                    eq("id", submissionId)
                }
            }
    }
}


