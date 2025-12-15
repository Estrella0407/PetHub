package com.example.pethub.data.model

data class Service(
    val serviceId: String = "",
    val type: String = "", // category
    val description: String = "",
    val price: Double = 0.0,
    val serviceName: String = "",
    val imageUrl: String = "", // Kept for UI
)

/**
 * Data class for service items in UI
 */
data class ServiceItem(
    val id: String,
    val type: String,
    val serviceName: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
)

/**
 * Extension function to convert Service to ServiceItem
 */
fun Service.toServiceItem(): ServiceItem {
    return ServiceItem(
        id = serviceId,
        type = type,
        serviceName = serviceName,
        description = description,
        price = price,
        imageUrl = imageUrl,
    )
}
