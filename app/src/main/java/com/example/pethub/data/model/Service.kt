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

/**
 * Data class for service items in UI
 */
data class ServiceItem(
    val id: String,
    val type: String,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val imageUrl: String,
    val availability: Boolean
)

/**
 * Extension function to convert Service to ServiceItem
 */
fun Service.toServiceItem(): ServiceItem {
    return ServiceItem(
        id = serviceId,
        type = type,
        name = serviceName,
        description = description,
        category = type,
        price = price,
        imageUrl = imageUrl,
        availability = true
    )
}
