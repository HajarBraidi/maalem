package com.example.maalem.domain.usecase

import com.example.maalem.data.model.Request
import com.example.maalem.domain.repository.CitizenRepository
import javax.inject.Inject

class CreateRequestUseCase @Inject constructor(
    private val repository: CitizenRepository
) {
    suspend operator fun invoke(request: Request): Result<Unit> {
        if (request.title.isBlank())
            return Result.failure(Exception("Le titre est requis"))
        if (request.description.isBlank())
            return Result.failure(Exception("La description est requise"))
        if (request.category.isBlank())
            return Result.failure(Exception("La catégorie est requise"))
        if (request.city.isBlank())
            return Result.failure(Exception("La ville est requise"))
        return repository.createRequest(request)
    }
}