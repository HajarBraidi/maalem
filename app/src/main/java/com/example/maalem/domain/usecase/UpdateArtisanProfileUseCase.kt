package com.example.maalem.domain.usecase

import com.example.maalem.domain.repository.ArtisanRepository
import javax.inject.Inject

class UpdateArtisanProfileUseCase @Inject constructor(
    private val repository: ArtisanRepository
) {
    suspend operator fun invoke(
        artisanId: String,
        name: String,
        phone: String,
        specialty: String,
        city: String,
        bio: String
    ): Result<Unit> {
        if (name.isBlank())
            return Result.failure(Exception("Le nom est requis"))
        if (specialty.isBlank())
            return Result.failure(Exception("La spécialité est requise"))
        if (city.isBlank())
            return Result.failure(Exception("La ville est requise"))
        return repository.updateProfile(artisanId, name, phone, specialty, city, bio)
    }
}