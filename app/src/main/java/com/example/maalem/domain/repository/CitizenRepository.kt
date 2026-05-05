package com.example.maalem.domain.repository

import com.example.maalem.data.model.Artisan
import com.example.maalem.data.model.Offer
import com.example.maalem.data.model.Request

interface CitizenRepository {
    suspend fun getArtisans(category: String? = null): Result<List<Artisan>>
    suspend fun createRequest(request: Request): Result<Unit>
    suspend fun getMyRequests(citizenId: String): Result<List<Request>>
    suspend fun getOffersForRequest(requestId: String): Result<List<Offer>>
    suspend fun acceptOffer(offer: Offer, request: Request): Result<Unit>
    suspend fun rejectOffer(offerId: String): Result<Unit>
    suspend fun getArtisanById(artisanId: String): Result<Artisan>
}