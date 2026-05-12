package com.example.maalem.domain.repository

interface CategoryRepository {
    suspend fun getCategories(): Result<List<String>>
}