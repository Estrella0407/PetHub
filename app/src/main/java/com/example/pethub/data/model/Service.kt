package com.example.pethub.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * Data class for service items in UI
 */
@IgnoreExtraProperties
data class Service(
    val serviceId: String = "",
    val type: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val serviceName: String = "", // Maps to Firestore "serviceName" field
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
        type = if (type.isNotEmpty()) type else serviceName,
        serviceName = serviceName,
        description = description,
        price = price,
        imageUrl = imageUrl,
    )
}
