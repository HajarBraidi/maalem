package com.example.maalem.data.repository

import com.example.maalem.data.model.Artisan
import com.example.maalem.data.model.Offer
import com.example.maalem.data.model.Request
import com.example.maalem.domain.repository.ArtisanRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ArtisanRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ArtisanRepository {

    override suspend fun getAvailableRequests(specialty: String): Result<List<Request>> {
        return try {
            val snapshot = firestore.collection("requests")
                .whereEqualTo("category", specialty)
                .whereEqualTo("status", "pending")
                .get().await()
            val requests = snapshot.documents.mapNotNull {
                it.toObject(Request::class.java)
            }
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendOffer(offer: Offer): Result<Unit> {
        return try {
            // Vérifier si l'artisan a déjà postulé à cette demande
            val existing = firestore.collection("offers")
                .whereEqualTo("requestId", offer.requestId)
                .whereEqualTo("artisanId", offer.artisanId)
                .get().await()

            if (!existing.isEmpty) {
                return Result.failure(Exception("Vous avez déjà postulé à cette demande"))
            }

            val docRef = firestore.collection("offers").document()
            val offerWithId = offer.copy(id = docRef.id)
            docRef.set(offerWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyOffers(artisanId: String): Result<List<Offer>> {
        return try {
            val snapshot = firestore.collection("offers")
                .whereEqualTo("artisanId", artisanId)
                .get().await()
            val offers = snapshot.documents.mapNotNull {
                it.toObject(Offer::class.java)
            }
            Result.success(offers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getArtisanProfile(artisanId: String): Result<Artisan> {
        return try {
            val doc = firestore.collection("users").document(artisanId).get().await()
            if (!doc.exists()) {
                return Result.failure(Exception("Artisan non trouvé"))
            }
            // Lire les champs manuellement (plus fiable qu'toObject avec l'héritage)
            val artisan = Artisan(
                uid = doc.getString("uid") ?: artisanId,
                name = doc.getString("name") ?: "",
                email = doc.getString("email") ?: "",
                phone = doc.getString("phone") ?: "",
                role = doc.getString("role") ?: "artisan",
                isActive = doc.getBoolean("isActive") ?: false,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                specialty = doc.getString("specialty") ?: "",
                city = doc.getString("city") ?: "",
                bio = doc.getString("bio") ?: "",
                isValidated = doc.getBoolean("isValidated") ?: false
            )
            Result.success(artisan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(
        artisanId: String,
        name: String,
        phone: String,
        specialty: String,
        city: String,
        bio: String
    ): Result<Unit> {
        return try {
            firestore.collection("users").document(artisanId).update(
                mapOf(
                    "name" to name,
                    "phone" to phone,
                    "specialty" to specialty,
                    "city" to city,
                    "bio" to bio
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}