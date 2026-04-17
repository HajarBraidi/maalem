package com.example.maalem.data.repository

import androidx.room.util.copy
import com.example.maalem.data.model.User
import com.example.maalem.data.model.UserRole
import com.example.maalem.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<UserRole> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await() //Connexion Firebase
            val uid = auth.currentUser!!.uid
            val doc = firestore.collection("users").document(uid).get().await()
            val role = UserRole.fromString(doc.getString("role") ?: "citizen")
            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        user: User
    ): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user!!.uid            //Chaque utilisateur a un ID unique
            val userWithId = User(
                uid = uid,
                name = user.name,
                email = user.email,
                phone = user.phone,
                role = user.role,
                isActive = user.isActive,
                createdAt = user.createdAt
            )
            firestore.collection("users").document(uid).set(userWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun isLoggedIn(): Boolean = auth.currentUser != null
}