package com.example.wizer2.vmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wizer2.services.UserService

class AuthViewModelFactory(private val userService: UserService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}