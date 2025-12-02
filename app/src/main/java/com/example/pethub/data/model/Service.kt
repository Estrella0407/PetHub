package com.example.pethub.data.model

data class Service(
    val serviceId: String = "",
    val type: String = "", // category
    val description: String = "",
    val price: Double = 0.0,
    val serviceName: String = "",
    val imageUrl: String = "", // Kept for UI
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)
