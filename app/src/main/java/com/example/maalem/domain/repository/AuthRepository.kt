package com.example.maalem.domain.repository

import com.example.maalem.data.model.User
import com.example.maalem.data.model.UserRole

interface AuthRepository {
    suspend fun login(email:String, password:String ) : Result<UserRole>
    suspend fun register(email: String, password: String, user: User): Result<Unit>
    suspend fun logout()
    suspend fun getCurrentUser(): User? //récupérer l’utilisateur connecté
    fun isLoggedIn(): Boolean   //vérifier si quelqu’un est connecté

}