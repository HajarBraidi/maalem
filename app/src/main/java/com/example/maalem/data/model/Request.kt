package com.example.maalem.data.model

data class Request(
    val id: String = "",
    val citizenId: String = "",
    val citizenName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val city: String = "",
    val status: String = "pending",  // pending | accepted | completed
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class RequestStatus(val value: String) {
    PENDING("pending"),
    ACCEPTED("accepted"),
    COMPLETED("completed");

    companion object {
        fun fromString(value: String) =
            entries.find { it.value == value } ?: PENDING
    }
}