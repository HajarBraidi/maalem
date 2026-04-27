package com.example.maalem.domain.repository

import com.example.maalem.data.model.User
import com.example.maalem.data.model.UserRole

data class LoginResult(
    val role: UserRole,
    val isValidated: Boolean = true
)

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<LoginResult>
    suspend fun register(email: String, password: String, user: User): Result<Unit>
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    fun isLoggedIn(): Boolean
}