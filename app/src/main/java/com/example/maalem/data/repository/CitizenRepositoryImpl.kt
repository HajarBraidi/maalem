package com.example.maalem.data.repository

import com.example.maalem.data.model.Artisan
import com.example.maalem.data.model.Offer
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
            val artisans = snapshot.documents.map { doc ->
                Artisan(
                    uid = doc.getString("uid") ?: doc.id,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    specialty = doc.getString("specialty") ?: "",
                    city = doc.getString("city") ?: "",
                    bio = doc.getString("bio") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true,
                    isValidated = doc.getBoolean("isValidated") ?: false
                )
            }
            Result.success(artisans)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun createRequest(request: Request): Result<Unit> {
        return try {
            val docRef = firestore.collection("requests").document()
            val requestWithId = request.copy(id = docRef.id)
            docRef.set(requestWithId).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ✅ Mes demandes triées par date
    override suspend fun getMyRequests(citizenId: String): Result<List<Request>> {
        return try {
            val snapshot = firestore.collection("requests")
                .whereEqualTo("citizenId", citizenId)
                .get().await()
            val requests = snapshot.documents.map { doc ->
                Request(
                    id = doc.getString("id") ?: doc.id,
                    citizenId = doc.getString("citizenId") ?: "",
                    citizenName = doc.getString("citizenName") ?: "",
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    category = doc.getString("category") ?: "",
                    city = doc.getString("city") ?: "",
                    status = doc.getString("status") ?: "pending",
                    photoUrl = doc.getString("photoUrl") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }.sortedByDescending { it.createdAt }
            Result.success(requests)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ✅ Offres reçues pour une demande
    override suspend fun getOffersForRequest(requestId: String): Result<List<Offer>> {
        return try {
            val snapshot = firestore.collection("offers")
                .whereEqualTo("requestId", requestId)
                .get().await()
            val offers = snapshot.documents.map { doc ->
                Offer(
                    id = doc.getString("id") ?: doc.id,
                    requestId = doc.getString("requestId") ?: "",
                    artisanId = doc.getString("artisanId") ?: "",
                    artisanName = doc.getString("artisanName") ?: "",
                    artisanPhone = doc.getString("artisanPhone") ?: "",
                    price = doc.getDouble("price") ?: 0.0,
                    delay = doc.getString("delay") ?: "",
                    message = doc.getString("message") ?: "",
                    status = doc.getString("status") ?: "pending",
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
            Result.success(offers)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ✅ Accepter une offre → met à jour offre + demande + rejette les autres
    override suspend fun acceptOffer(offer: Offer, request: Request): Result<Unit> {
        return try {
            val batch = firestore.batch()

            // 1. Accepter cette offre
            val offerRef = firestore.collection("offers").document(offer.id)
            batch.update(offerRef, "status", "accepted")

            // 2. Rejeter toutes les autres offres
            val otherOffers = firestore.collection("offers")
                .whereEqualTo("requestId", request.id)
                .get().await()
            otherOffers.documents.forEach { doc ->
                if (doc.id != offer.id) {
                    batch.update(doc.reference, "status", "rejected")
                }
            }

            // 3. Mettre à jour la demande → accepted
            val requestRef = firestore.collection("requests").document(request.id)
            batch.update(requestRef, mapOf(
                "status" to "accepted",
                "artisanId" to offer.artisanId,
                "artisanName" to offer.artisanName
            ))

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ✅ Refuser une offre
    override suspend fun rejectOffer(offerId: String): Result<Unit> {
        return try {
            firestore.collection("offers").document(offerId)
                .update("status", "rejected").await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ✅ Profil artisan
    override suspend fun getArtisanById(artisanId: String): Result<Artisan> {
        return try {
            val doc = firestore.collection("users").document(artisanId).get().await()
            val artisan = Artisan(
                uid = doc.getString("uid") ?: doc.id,
                name = doc.getString("name") ?: "",
                email = doc.getString("email") ?: "",
                phone = doc.getString("phone") ?: "",
                specialty = doc.getString("specialty") ?: "",
                city = doc.getString("city") ?: "",
                bio = doc.getString("bio") ?: "",
                isActive = doc.getBoolean("isActive") ?: true,
                isValidated = doc.getBoolean("isValidated") ?: false
            )
            Result.success(artisan)
        } catch (e: Exception) { Result.failure(e) }
    }
}