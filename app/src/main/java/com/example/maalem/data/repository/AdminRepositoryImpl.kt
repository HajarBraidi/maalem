package com.example.maalem.data.repository

import com.example.maalem.data.model.AppStats
import com.example.maalem.data.model.Artisan
import com.example.maalem.data.model.Citizen
import com.example.maalem.data.model.User
import com.example.maalem.domain.repository.AdminRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminRepository {

    // ✅ Artisans non encore validés
    override suspend fun getPendingArtisans(): Result<List<Artisan>> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("role", "artisan")
                .whereEqualTo("isValidated", false)
                .get().await()
            val list = snapshot.documents.map { doc ->
                Artisan(
                    uid = doc.getString("uid") ?: doc.id,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    specialty = doc.getString("specialty") ?: "",
                    city = doc.getString("city") ?: "",
                    bio = doc.getString("bio") ?: "",
                    isActive = doc.getBoolean("isActive") ?: false,
                    isValidated = doc.getBoolean("isValidated") ?: false,
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
            Result.success(list)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ✅ Valider ou refuser un artisan
    override suspend fun validateArtisan(uid: String, approve: Boolean): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).update(
                mapOf(
                    "isValidated" to approve,
                    "isActive" to approve
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ✅ Tous les artisans (validés ou non)
    override suspend fun getAllArtisans(): Result<List<Artisan>> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("role", "artisan")
                .get().await()
            val list = snapshot.documents.map { doc ->
                Artisan(
                    uid = doc.getString("uid") ?: doc.id,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    specialty = doc.getString("specialty") ?: "",
                    city = doc.getString("city") ?: "",
                    bio = doc.getString("bio") ?: "",
                    // ✅ Lire explicitement depuis Firestore
                    isActive = doc.getBoolean("isActive") ?: false,
                    isValidated = doc.getBoolean("isValidated") ?: false,
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
            Result.success(list)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ✅ Tous les citoyens
    override suspend fun getAllCitizens(): Result<List<Citizen>> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("role", "citizen")
                .get().await()
            val list = snapshot.documents.map { doc ->
                Citizen(
                    uid = doc.getString("uid") ?: doc.id,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    address = doc.getString("address") ?: "",
                    photoUrl = doc.getString("photoUrl") ?: "",
                    // ✅ Lire explicitement depuis Firestore
                    isActive = doc.getBoolean("isActive") ?: true,
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
            Result.success(list)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ✅ Activer ou désactiver un compte
    override suspend fun toggleUserAccount(uid: String, isActive: Boolean): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .update("isActive", isActive).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ✅ Statistiques globales
    override suspend fun getStats(): Result<AppStats> {
        return try {
            val users = firestore.collection("users").get().await()
            val requests = firestore.collection("requests").get().await()

            Result.success(AppStats(
                totalCitizens = users.count { it.getString("role") == "citizen" },
                totalArtisans = users.count { it.getString("role") == "artisan" },
                pendingArtisans = users.count {
                    it.getString("role") == "artisan" &&
                            it.getBoolean("isValidated") == false
                },
                totalRequests = requests.documents.size,
                pendingRequests = requests.count { it.getString("status") == "pending" }
            ))
        } catch (e: Exception) { Result.failure(e) }
    }

    // ✅ Catégories
    override suspend fun getCategories(): Result<List<String>> {
        return try {
            val doc = firestore.collection("config")
                .document("categories").get().await()
            @Suppress("UNCHECKED_CAST")
            val list = doc.get("list") as? List<String> ?: emptyList()
            Result.success(list)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun addCategory(category: String): Result<Unit> {
        return try {
            val ref = firestore.collection("config").document("categories")
            val doc = ref.get().await()
            @Suppress("UNCHECKED_CAST")
            val list = (doc.get("list") as? List<String>
                ?: emptyList()).toMutableList()
            if (!list.contains(category)) {
                list.add(category)
                ref.set(mapOf("list" to list)).await()
            }
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteCategory(category: String): Result<Unit> {
        return try {
            val ref = firestore.collection("config").document("categories")
            val doc = ref.get().await()
            @Suppress("UNCHECKED_CAST")
            val list = (doc.get("list") as? List<String>
                ?: emptyList()).toMutableList()
            list.remove(category)
            ref.set(mapOf("list" to list)).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ✅ Envoyer notification (sauvegardée dans Firestore)
    override suspend fun sendNotification(title: String, message: String): Result<Unit> {
        return try {
            firestore.collection("notifications").add(
                mapOf(
                    "title" to title,
                    "message" to message,
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}