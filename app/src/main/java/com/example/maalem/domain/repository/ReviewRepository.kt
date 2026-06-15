package com.example.maalem.domain.repository

import com.example.maalem.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // Ajouter un avis + recalculer la moyenne sur l'artisan
    suspend fun addReview(review: Review): Result<Unit> {
        return try {
            // 1. Enregistrer l'avis
            val docRef = firestore.collection("reviews").document()
            val reviewWithId = review.copy(id = docRef.id)
            docRef.set(reviewWithId).await()

            // 2. Recalculer la moyenne
            recalculateAverageRating(review.artisanId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Récupérer tous les avis d'un artisan
    suspend fun getReviewsForArtisan(artisanId: String): Result<List<Review>> {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("artisanId", artisanId)
                .get()
                .await()

            val reviews = snapshot.documents.mapNotNull {
                it.toObject(Review::class.java)
            }.sortedByDescending { it.createdAt }

            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Vérifier si le citoyen a déjà laissé un avis pour cet artisan
    suspend fun hasAlreadyReviewed(artisanId: String, citizenId: String): Boolean {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("artisanId", artisanId)
                .whereEqualTo("citizenId", citizenId)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    // Recalculer et mettre à jour averageRating + reviewCount sur l'artisan
    private suspend fun recalculateAverageRating(artisanId: String) {
        val snapshot = firestore.collection("reviews")
            .whereEqualTo("artisanId", artisanId)
            .get()
            .await()

        val reviews = snapshot.documents.mapNotNull {
            it.toObject(Review::class.java)
        }

        val count = reviews.size
        val average = if (count > 0) reviews.map { it.rating }.average() else 0.0
        firestore.collection("users").document(artisanId)
            .update(
                mapOf(
                    "averageRating" to average,
                    "reviewCount" to count
                )
            ).await()
    }
}