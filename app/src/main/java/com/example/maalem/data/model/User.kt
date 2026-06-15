package com.example.maalem.data.model


open class User(
    open val uid: String = "",
    open val name: String = "",
    open val email: String = "",
    open val phone: String = "",
    open val role: String = "",
    open val isActive: Boolean = true,
    open val createdAt: Long = System.currentTimeMillis()
)

data class Citizen(
    override val uid: String = "",
    override val name: String = "",
    override val email: String = "",
    override val phone: String = "",
    override val role: String = "citizen",
    override val isActive: Boolean = true,
    override val createdAt: Long = System.currentTimeMillis(),

    val address: String = "",

    val locationId: String = "",
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    val photoUrl: String = ""
) : User(uid, name, email, phone, role, isActive, createdAt)

data class Artisan(
    override val uid: String = "",
    override val name: String = "",
    override val email: String = "",
    override val phone: String = "",
    override val role: String = "artisan",
    override val isActive: Boolean = true,
    override val createdAt: Long = System.currentTimeMillis(),

    val specialty: String = "",
    val city: String = "",
    val bio: String = "",

    val locationId: String = "",
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    val isValidated: Boolean = false,
    val cinPhotoBase64: String = "",


    val averageRating: Double = 0.0,   // moyenne sur 10
    val reviewCount: Int = 0         // nombre d'avis
) : User(uid, name, email, phone, role, isActive, createdAt)


data class Admin(
    override val uid: String = "",
    override val name: String = "",
    override val email: String = "",
    override val phone: String = "",
    override val role: String = "admin",
    override val isActive: Boolean = true,
    override val createdAt: Long = System.currentTimeMillis()
) : User(uid, name, email, phone, role, isActive, createdAt)

enum class UserRole(val value: String) {
    CITIZEN("citizen"),
    ARTISAN("artisan"),
    ADMIN("admin");

    companion object {
        fun fromString(value: String) = entries.find { it.value == value } ?: CITIZEN
    }
}
