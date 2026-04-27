package com.example.maalem.data.model

enum class Specialty(val value: String, val displayName: String) {
    PLUMBING("plumbing", "Plomberie (fuites d'eau)"),
    ELECTRICAL("electrical", "Électricité (prises, installations)"),
    CRACKS("cracks", "Maçonnerie (fissures de murs)"),
    HUMIDITY("humidity", "Peinture (murs humides)"),
    CARPENTRY("carpentry", "Menuiserie (portes endommagées)");

    companion object {
        fun fromValue(value: String) = entries.find { it.value == value } ?: PLUMBING
        fun displayNames(): Array<String> = entries.map { it.displayName }.toTypedArray()
        fun allValues(): Array<String> = entries.map { it.value }.toTypedArray()
    }
}