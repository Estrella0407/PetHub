package com.example.pethub.data.model

data class Service(
    val serviceId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val availability: Boolean = true,
    val rating: Double = 0.0,
    val imageUrl: String = "",
    val durationMinutes: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)
