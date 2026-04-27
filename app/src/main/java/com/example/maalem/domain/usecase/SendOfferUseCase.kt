package com.example.maalem.domain.usecase

import com.example.maalem.data.model.Offer
import com.example.maalem.domain.repository.ArtisanRepository
import javax.inject.Inject

class SendOfferUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(offer: Offer): Result<Unit> {
        if (offer.requestId.isBlank())
            return Result.failure(Exception("La demande est requise"))
        if (offer.price <= 0)
            return Result.failure(Exception("Le prix doit être positif"))
        if (offer.delay.isBlank())
            return Result.failure(Exception("Le délai est requis"))
        if (offer.message.isBlank())
            return Result.failure(Exception("Un message est requis"))
        return repository.sendOffer(offer)
    }
}