package com.example.maalem.domain.repository

import com.example.maalem.data.model.Artisan
import com.example.maalem.data.model.Offer
import com.example.maalem.data.model.Request

interface ArtisanRepository {

    // Voir les demandes disponibles (filtrées par spécialité de l'artisan)
    suspend fun getAvailableRequests(specialty: String): Result<List<Request>>

    // Envoyer une offre sur une demande
    suspend fun sendOffer(offer: Offer): Result<Unit>

    // Voir les offres que cet artisan a déjà envoyées
    suspend fun getMyOffers(artisanId: String): Result<List<Offer>>

    // Récupérer le profil de l'artisan connecté
    suspend fun getArtisanProfile(artisanId: String): Result<Artisan>

    // Mettre à jour son profil (bio, spécialité, ville)
    suspend fun updateProfile(
        artisanId: String,
        name: String,
        phone: String,
        specialty: String,
        city: String,
        bio: String
    ): Result<Unit>
}