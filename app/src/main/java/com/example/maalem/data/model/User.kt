package com.example.maalem.data.model

data class User(val uid: String = "",
                val name: String = "",
                val email: String = "",
                val phone: String = "",
                val role: String = "citizen",   // "citizen" | "artisan" | "admin"
                val photoUrl: String = "",
                val isActive: Boolean = true,
                val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole(val value: String) {
    CITIZEN("citizen"),
    ARTISAN("artisan"),
    ADMIN("admin");

    companion object {       //Convertir un String → en UserRole
        fun fromString(value: String) = entries.find { it.value == value } ?: CITIZEN
    }
}
