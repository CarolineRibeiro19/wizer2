package com.example.wizer2.repository

import com.example.wizer2.model.Quiz

interface QuizService {
    suspend fun getQuizzesForSubject(subjectId: String): List<Quiz>
    suspend fun joinQuizWithCode(code: String): Quiz?
}
