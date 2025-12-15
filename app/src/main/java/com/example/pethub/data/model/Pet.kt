package com.example.pethub.data.model

import com.google.firebase.firestore.DocumentId
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
    // Keeping existing fields that might be useful or ensuring compatibility if needed
    val imageUrl: String? = null,
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)
