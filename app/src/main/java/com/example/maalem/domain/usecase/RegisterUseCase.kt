package com.example.maalem.domain.usecase

import com.example.maalem.data.model.User
import com.example.maalem.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String,
        user: User
    ): Result<Unit> {
        if (email.isBlank() || password.isBlank())
            return Result.failure(Exception("Tous les champs sont requis"))
        if (password.length < 6)
            return Result.failure(Exception("Mot de passe trop court (min 6 caractères)"))
        if (password != confirmPassword)
            return Result.failure(Exception("Les mots de passe ne correspondent pas"))
        return authRepository.register(email, password, user)
    }
}