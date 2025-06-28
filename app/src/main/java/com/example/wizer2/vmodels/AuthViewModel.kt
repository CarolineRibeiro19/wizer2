package com.example.wizer2.vmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizer2.models.Role
import com.example.wizer2.models.User
import com.example.wizer2.services.UserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val userService: UserService) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = userService.signIn(email, password)
            _authState.value = if (user != null) {
                AuthState.Success(user)
            } else {
                AuthState.Error("Login failed")
            }
        }
    }

    fun register(email: String, password: String, username: String, role: Role) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = userService.signUp(email, password, username, role)
            _authState.value = if (user != null) {
                AuthState.Success(user)
            } else {
                AuthState.Error("Registration failed")
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}