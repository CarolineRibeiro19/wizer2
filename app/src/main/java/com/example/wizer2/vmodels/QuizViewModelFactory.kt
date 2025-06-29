package com.example.wizer2.vmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wizer2.services.*


class QuizViewModelFactory(
    private val quizzesService: QuizzesService,
    private val questionService: QuestionService,
    private val quizQuestionService: QuizQuestionService,
    private val quizSubmissionService: QuizSubmissionService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(
                quizzesService,
                questionService,
                quizQuestionService,
                quizSubmissionService
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
