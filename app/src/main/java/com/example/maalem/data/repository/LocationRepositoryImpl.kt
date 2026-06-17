package com.example.maalem.data.repository

import com.example.maalem.data.model.AppLocation
import com.example.maalem.domain.repository.LocationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : LocationRepository {

    override suspend fun getCities(): Result<List<AppLocation>> {
        return try {
            val doc = firestore.collection("config")
                .document("locations")
                .get()
                .await()

            @Suppress("UNCHECKED_CAST")
            val list = doc.get("list") as? List<Map<String, Any>> ?: emptyList()
            val cities = list.map { map ->
                AppLocation(
                    id = map["id"] as? String ?: "",
                    name = map["name"] as? String ?: "",
                    latitude = (map["latitude"] as? Double) ?: 0.0,
                    longitude = (map["longitude"] as? Double) ?: 0.0
                )
            }
            Result.success(cities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}