package com.example.maalem.data.model

data class Review(
    val id: String = "",
    val artisanId: String = "",
    val citizenId: String = "",
    val citizenName: String = "",
    val rating: Float = 0f,        // valeur 1–10
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)