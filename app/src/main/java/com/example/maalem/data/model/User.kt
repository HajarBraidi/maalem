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

<<<<<<< Updated upstream
=======
// Citoyen
data class Citizen(
    override val uid: String = "",
    override val name: String = "",
    override val email: String = "",
    override val phone: String = "",
    override val role: String = "citizen",
    override val isActive: Boolean = true,
    override val createdAt: Long = System.currentTimeMillis(),
    val address: String = "",
    val photoUrl: String = ""
) : User(uid, name, email, phone, role, isActive, createdAt)

// Artisan
data class Artisan(
    override val uid: String = "",
    override val name: String = "",
    override val email: String = "",
    override val phone: String = "",
    override val role: String = "artisan",
    override val isActive: Boolean = true, // attend validation admin
    override val createdAt: Long = System.currentTimeMillis(),
    val specialty: String = "",
    val city: String = "",
    val bio: String = "",
    val isValidated: Boolean = false
) : User(uid, name, email, phone, role, isActive, createdAt)

// Admin
data class Admin(
    override val uid: String = "",
    override val name: String = "",
    override val email: String = "",
    override val phone: String = "",
    override val role: String = "admin",
    override val isActive: Boolean = true,
    override val createdAt: Long = System.currentTimeMillis()
) : User(uid, name, email, phone, role, isActive, createdAt)

>>>>>>> Stashed changes
enum class UserRole(val value: String) {
    CITIZEN("citizen"),
    ARTISAN("artisan"),
    ADMIN("admin");

    companion object {       //Convertir un String → en UserRole
        fun fromString(value: String) = entries.find { it.value == value } ?: CITIZEN
    }
}
