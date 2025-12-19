package com.example.pethub.data.model

import com.google.firebase.Timestamp
import java.util.Date

data class Pet(
    val petId: String = "",
    val petName: String = "",
    val dateOfBirth: Date? = null,
    val type: String = "",
    val breed: String = "",
    val weight: Double? = null,
    val sex: String = "",
    val remarks: String? = null,
    val custId: String = "",
    val imageUrl: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
