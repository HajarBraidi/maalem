package com.example.maalem.domain.repository

import com.example.maalem.data.model.AppLocation
import com.example.maalem.data.model.Artisan
import com.example.maalem.data.model.AppStats
import com.example.maalem.data.model.Citizen
import com.example.maalem.data.model.User

interface AdminRepository {

    // --- Artisans en attente ---
    suspend fun getPendingArtisans(): Result<List<Artisan>>
    suspend fun validateArtisan(uid: String, approve: Boolean): Result<Unit>

    // --- Gestion comptes ---
    suspend fun getAllArtisans(): Result<List<Artisan>>
    suspend fun getAllCitizens(): Result<List<Citizen>>
    suspend fun toggleUserAccount(uid: String, isActive: Boolean): Result<Unit>

    // --- Statistiques ---
    suspend fun getStats(): Result<AppStats>

    // --- Catégories ---
    suspend fun getCategories(): Result<List<String>>
    suspend fun addCategory(category: String): Result<Unit>
    suspend fun deleteCategory(category: String): Result<Unit>

    // --- Notifications ---
    suspend fun sendNotification(title: String, message: String): Result<Unit>

    // --- Localisations ---
    suspend fun getLocations(): Result<List<AppLocation>>
    suspend fun addLocation(location: AppLocation): Result<Unit>
    suspend fun deleteLocation(locationId: String): Result<Unit>

}