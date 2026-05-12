package com.example.maalem.data.repository

import com.example.maalem.domain.repository.CategoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    override suspend fun getCategories(): Result<List<String>> {
        return try {
            val doc = firestore.collection("config")
                .document("categories")
                .get()
                .await()

            @Suppress("UNCHECKED_CAST")
            val list = doc.get("list") as? List<String> ?: emptyList()

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}