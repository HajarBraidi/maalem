package com.example.maalem.data.repository

import com.example.maalem.data.model.*
import com.example.maalem.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.example.maalem.domain.repository.LoginResult
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<LoginResult> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val uid = auth.currentUser!!.uid
            val doc = firestore.collection("users").document(uid).get().await()
            val role = UserRole.fromString(doc.getString("role") ?: "citizen")

            val isValidated = if (role == UserRole.ARTISAN) {
                doc.getBoolean("isValidated") ?: false
            } else {
                true
            }

            Result.success(LoginResult(role, isValidated))
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
            // 1. Créer compte Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user!!.uid

            // 2. Préparer les données selon le rôle
            val userData = when (user) {
                is Citizen -> hashMapOf(
                    "uid" to uid,
                    "name" to user.name,
                    "email" to user.email,
                    "phone" to user.phone,
                    "role" to "citizen",
                    "address" to user.address,
                    "isActive" to true,
                    "createdAt" to System.currentTimeMillis()
                )
                is Artisan -> hashMapOf(
                    "uid" to uid,
                    "name" to user.name,
                    "email" to user.email,
                    "phone" to user.phone,
                    "role" to "artisan",
                    "specialty" to user.specialty,
                    "city" to user.city,
                    "bio" to user.bio,
                    "isActive" to false,
                    "isValidated" to false,
                    "cinPhotoBase64" to user.cinPhotoBase64,  // ← NOUVEAU
                    "createdAt" to System.currentTimeMillis()
                )
                else -> hashMapOf(
                    "uid" to uid,
                    "name" to user.name,
                    "email" to user.email,
                    "phone" to user.phone,
                    "role" to user.role,
                    "isActive" to true,
                    "createdAt" to System.currentTimeMillis()
                )
            }

            // 3. Sauvegarder dans Firestore
            firestore.collection("users").document(uid).set(userData).await()

            // 4. Déconnecter après inscription (doit se connecter manuellement)
            auth.signOut()

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
            val role = doc.getString("role") ?: "citizen"
            when (UserRole.fromString(role)) {
                UserRole.CITIZEN -> doc.toObject(Citizen::class.java)
                UserRole.ARTISAN -> doc.toObject(Artisan::class.java)
                UserRole.ADMIN -> doc.toObject(Admin::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun isLoggedIn(): Boolean = auth.currentUser != null
}