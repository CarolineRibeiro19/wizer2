package com.example.wizer2.services


import com.example.wizer2.models.QuizQuestion
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class QuizQuestionService(private val client: SupabaseClient) {

    suspend fun addQuizQuestion(quizQuestion: QuizQuestion) {
        client.from("quiz_question").insert(quizQuestion)
    }

    suspend fun getQuizQuestions(quizId: String): List<QuizQuestion> {
        return client.from("quiz_question")
            .select()
            .decodeList<QuizQuestion>()
            .filter { it.quizId == quizId }
            .sortedBy { it.position }
    }


    suspend fun deleteQuizQuestion(quizId: String, questionId: String) {
        client.from("quiz_question")
            .delete {
                filter {
                    eq("quiz_id", quizId)
                    eq("question_id", questionId)
                }
            }
    }

    suspend fun updatePosition(quizId: String, questionId: String, newPosition: Int) {
        client.from("quiz_question")
            .update({
                QuizQuestion(quizId, questionId, newPosition)
            }) {
                filter {
                    eq("quiz_id", quizId)
                    eq("question_id", questionId)
                }
            }
    }
}
