package com.example.wizer2.models

enum class Role {
    TEACHER {
        override fun toString(): String {
            return "TEACHER"
        }
    },
    STUDENT {
        override fun toString(): String {
            return "STUDENT"
        }
    }
}