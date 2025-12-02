package com.example.pethub.data.model

data class Pet(
    val petId: String = "",
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val gender: String = "",
    val dateOfBirth: Long? = null,
    val weight: Double? = null,
    val medicalRecords: String? = null,
    val ownerId: String = "",
    val imageUrl: String? = null,
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)
