package com.example.maalem.domain.repository

import com.example.maalem.data.model.AppLocation

interface LocationRepository {
    suspend fun getCities(): Result<List<AppLocation>>
}