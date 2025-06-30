package com.example.wizer2.vmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizer2.models.Question
import com.example.wizer2.models.QuizQuestion
import com.example.wizer2.models.QuizSubmission
import com.example.wizer2.models.Quizzes
import com.example.wizer2.services.QuestionService
import com.example.wizer2.services.QuizQuestionService
import com.example.wizer2.services.QuizSubmissionService
import com.example.wizer2.services.QuizzesService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine

class QuizViewModel(
    private val quizzesService: QuizzesService,
    private val questionService: QuestionService,
    private val quizQuestionService: QuizQuestionService,
    private val quizSubmissionService: QuizSubmissionService
) : ViewModel() {

    // UI State data classes
    data class QuizUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val quizzes: List<Quizzes> = emptyList(),
        val selectedQuiz: Quizzes? = null
    )

    data class QuestionUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val questions: List<Question> = emptyList(),
        val currentQuestionIndex: Int = 0,
        val userAnswers: Map<String, Any> = emptyMap(), // questionId to answer
        val isQuizCompleted: Boolean = false
    )

    data class SubmissionUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val submissions: List<QuizSubmission> = emptyList(),
        val currentSubmission: QuizSubmission? = null,
        val isSubmitting: Boolean = false
    )

    // State flows
    private val _quizState = MutableStateFlow(QuizUiState())
    val quizState: StateFlow<QuizUiState> = _quizState.asStateFlow()

    private val _questionState = MutableStateFlow(QuestionUiState())
    val questionState: StateFlow<QuestionUiState> = _questionState.asStateFlow()

    private val _submissionState = MutableStateFlow(SubmissionUiState())
    val submissionState: StateFlow<SubmissionUiState> = _submissionState.asStateFlow()

    // Current quiz questions with their positions
    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    // Combined state for easier UI consumption
    val currentQuestion: Flow<Question?> = combine(
        _questionState,
        _quizQuestions
    ) { questionState, quizQuestions ->
        if (questionState.questions.isNotEmpty() &&
            questionState.currentQuestionIndex < questionState.questions.size) {
            questionState.questions[questionState.currentQuestionIndex]
        } else null
    }



    fun answerQuestion(questionId: String, answer: Any) {
        val currentAnswers = _questionState.value.userAnswers.toMutableMap()
        currentAnswers[questionId] = answer

        _questionState.value = _questionState.value.copy(
            userAnswers = currentAnswers
        )
    }

    fun nextQuestion() {
        val currentState = _questionState.value
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            _questionState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex + 1
            )
        } else {
            // Quiz completed
            _questionState.value = currentState.copy(isQuizCompleted = true)
        }
    }

    fun previousQuestion() {
        val currentState = _questionState.value
        if (currentState.currentQuestionIndex > 0) {
            _questionState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex - 1
            )
        }
    }

    fun goToQuestion(index: Int) {
        val currentState = _questionState.value
        if (index in 0 until currentState.questions.size) {
            _questionState.value = currentState.copy(
                currentQuestionIndex = index
            )
        }
    }

    // Quiz Submission
    fun submitQuiz(userId: String, groupId: String) {
        viewModelScope.launch {
            _submissionState.value = _submissionState.value.copy(isSubmitting = true, error = null)

            try {
                val quiz = _quizState.value.selectedQuiz
                val questions = _questionState.value.questions
                val userAnswers = _questionState.value.userAnswers

                if (quiz == null) {
                    throw Exception("No quiz selected")
                }

                // Calculate score
                var totalScore = 0
                questions.forEach { question ->
                    val userAnswer = userAnswers[question.id]
                    if (isAnswerCorrect(question, userAnswer)) {
                        totalScore += question.score
                    }
                }

                // Create submission
                val submission = QuizSubmission(
                    id = "", // Assuming service/database generates ID
                    quizId = quiz.id,
                    userId = userId,
                    groupId = groupId,
                    score = totalScore
                )

                quizSubmissionService.submitQuiz(submission)

                _submissionState.value = _submissionState.value.copy(
                    isSubmitting = false,
                    currentSubmission = submission
                )

            } catch (e: Exception) {
                _submissionState.value = _submissionState.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "Failed to submit quiz"
                )
            }
        }
    }

    fun loadUserSubmissions(userId: String) {
        viewModelScope.launch {
            _submissionState.value = _submissionState.value.copy(isLoading = true, error = null)
            try {
                // Get all submissions and filter by userId (since your service doesn't have this method)
                // You might want to add a getSubmissionsByUserId method to your service
                val allSubmissions = mutableListOf<QuizSubmission>()

                // For now, we'll need to get submissions for each quiz the user might have taken
                // This is not optimal - consider adding a direct method to your service
                val quizzes = _quizState.value.quizzes
                quizzes.forEach { quiz ->
                    val userSubmission = quizSubmissionService.getUserSubmission(userId, quiz.id)
                    userSubmission?.let { allSubmissions.add(it) }
                }

                _submissionState.value = _submissionState.value.copy(
                    isLoading = false,
                    submissions = allSubmissions
                )
            } catch (e: Exception) {
                _submissionState.value = _submissionState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load submissions"
                )
            }
        }
    }

    // Helper functions
    private fun isAnswerCorrect(question: Question, userAnswer: Any?): Boolean {
        return when (question.type.lowercase()) {
            "multiple_choice", "single_choice" -> {
                userAnswer is Int && userAnswer == question.correctIndex
            }
            "text", "short_answer" -> {
                userAnswer is String &&
                        userAnswer.trim().equals(question.expectedAnswer.trim(), ignoreCase = true)
            }
            "true_false", "boolean" -> {
                userAnswer is Boolean &&
                        userAnswer.toString().equals(question.expectedAnswer, ignoreCase = true)
            }
            else -> false
        }
    }

    fun getQuizProgress(): Float {
        val currentState = _questionState.value
        return if (currentState.questions.isEmpty()) {
            0f
        } else {
            (currentState.currentQuestionIndex + 1).toFloat() / currentState.questions.size.toFloat()
        }
    }

    fun getAnsweredQuestionsCount(): Int {
        return _questionState.value.userAnswers.size
    }

    fun isCurrentQuestionAnswered(): Boolean {
        val currentState = _questionState.value
        val currentQuestion = if (currentState.questions.isNotEmpty() &&
            currentState.currentQuestionIndex < currentState.questions.size) {
            currentState.questions[currentState.currentQuestionIndex]
        } else null

        return currentQuestion?.let {
            currentState.userAnswers.containsKey(it.id)
        } ?: false
    }

    // Additional helper functions for your services
    fun loadQuizSubmissions(quizId: String) {
        viewModelScope.launch {
            _submissionState.value = _submissionState.value.copy(isLoading = true, error = null)
            try {
                val submissions = quizSubmissionService.getSubmissionsForQuiz(quizId)
                _submissionState.value = _submissionState.value.copy(
                    isLoading = false,
                    submissions = submissions
                )
            } catch (e: Exception) {
                _submissionState.value = _submissionState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load quiz submissions"
                )
            }
        }
    }

    fun checkUserSubmission(userId: String, quizId: String) {
        viewModelScope.launch {
            try {
                val existingSubmission = quizSubmissionService.getUserSubmission(userId, quizId)
                _submissionState.value = _submissionState.value.copy(
                    currentSubmission = existingSubmission
                )
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun loadQuizzesBySubject(subjectId: String) {
        viewModelScope.launch {
            _quizState.value = _quizState.value.copy(isLoading = true, error = null)
            try {
                val quizzes = quizzesService.getQuizzesBySubject(subjectId)
                _quizState.value = _quizState.value.copy(
                    isLoading = false,
                    quizzes = quizzes
                )
            } catch (e: Exception) {
                _quizState.value = _quizState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load quizzes by subject"
                )
            }
        }
    }

    fun deleteQuiz(quizId: String) {
        viewModelScope.launch {
            _quizState.value = _quizState.value.copy(isLoading = true, error = null)
            try {
                quizzesService.deleteQuiz(quizId)
                // Remove from local state
                val updatedQuizzes = _quizState.value.quizzes.filter { it.id != quizId }
                _quizState.value = _quizState.value.copy(
                    isLoading = false,
                    quizzes = updatedQuizzes,
                    selectedQuiz = if (_quizState.value.selectedQuiz?.id == quizId) null
                    else _quizState.value.selectedQuiz
                )
            } catch (e: Exception) {
                _quizState.value = _quizState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to delete quiz"
                )
            }
        }
    }

    fun deleteSubmission(submissionId: String) {
        viewModelScope.launch {
            try {
                quizSubmissionService.deleteSubmission(submissionId)
                // Remove from local state
                val updatedSubmissions = _submissionState.value.submissions.filter {
                    it.id != submissionId
                }
                _submissionState.value = _submissionState.value.copy(
                    submissions = updatedSubmissions,
                    currentSubmission = if (_submissionState.value.currentSubmission?.id == submissionId)
                        null else _submissionState.value.currentSubmission
                )
            } catch (e: Exception) {
                _submissionState.value = _submissionState.value.copy(
                    error = e.message ?: "Failed to delete submission"
                )
            }
        }
    }
    fun resetQuiz() {
        _questionState.value = QuestionUiState()
        _submissionState.value = _submissionState.value.copy(
            currentSubmission = null,
            error = null
        )
    }

    fun clearError() {
        _quizState.value = _quizState.value.copy(error = null)
        _questionState.value = _questionState.value.copy(error = null)
        _submissionState.value = _submissionState.value.copy(error = null)
    }
}