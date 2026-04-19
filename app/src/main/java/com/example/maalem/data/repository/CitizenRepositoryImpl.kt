package com.example.maalem.data.repository

import com.example.maalem.data.model.Artisan
import com.example.maalem.data.model.Request
import com.example.maalem.domain.repository.CitizenRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CitizenRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CitizenRepository {

    override suspend fun getArtisans(category: String?): Result<List<Artisan>> {
        return try {
            var query = firestore.collection("users")
                .whereEqualTo("role", "artisan")
                .whereEqualTo("isValidated", true)

            if (!category.isNullOrEmpty()) {
                query = query.whereEqualTo("specialty", category)
            }

            val snapshot = query.get().await()
            val artisans = snapshot.documents.mapNotNull {
                it.toObject(Artisan::class.java)
            }
            Result.success(artisans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createRequest(request: Request): Result<Unit> {
        return try {
            val docRef = firestore.collection("requests").document()
            val requestWithId = request.copy(id = docRef.id)
            docRef.set(requestWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyRequests(citizenId: String): Result<List<Request>> {
        return try {
            val snapshot = firestore.collection("requests")
                .whereEqualTo("citizenId", citizenId)
                .get().await()
            val requests = snapshot.documents.mapNotNull {
                it.toObject(Request::class.java)
            }
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}