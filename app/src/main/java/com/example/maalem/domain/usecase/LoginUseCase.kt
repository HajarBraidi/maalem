package com.example.maalem.domain.usecase

import com.example.maalem.data.model.UserRole
import com.example.maalem.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<UserRole> {  //operator fun invoke permet d’appeler la classe comme une fonction
        if (email.isBlank() || password.isBlank())
            return Result.failure(Exception("Email et mot de passe requis"))
        if (password.length < 6)
            return Result.failure(Exception("Mot de passe trop court"))
        return authRepository.login(email, password)
    }
}