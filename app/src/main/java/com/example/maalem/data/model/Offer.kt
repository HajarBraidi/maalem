package com.example.maalem.data.model

data class Offer(
    val id: String = "",
    val requestId: String = "",       // lien vers Request
    val artisanId: String = "",
    val artisanName: String = "",     // dénormalisation pour éviter jointures
    val artisanPhone: String = "",
    val price: Double = 0.0,
    val delay: String = "",
    val message: String = "",
    val status: String = "pending",    // pending | accepted | rejected
    val createdAt: Long = System.currentTimeMillis()
)

enum class OfferStatus(val value: String) {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected");

    companion object {
        fun fromString(value: String) =
            entries.find { it.value == value } ?: PENDING
    }
}