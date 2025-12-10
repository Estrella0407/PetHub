package com.example.pethub.data.model

data class Pet(
    val petId: String = "",
    val petName: String = "",
    val dateOfBirth: Long? = null,
    val type: String = "", // was species
    val breed: String = "",
    val weight: Int? = null,
    val sex: String = "", // was gender
    val remarks: String? = null,
    val custId: String = "", // was ownerId
    // Keeping existing fields that might be useful or ensuring compatibility if needed
    val imageUrl: String? = null,
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)
