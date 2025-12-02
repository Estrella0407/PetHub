package com.example.pethub.data.model

data class Booking(
    val bookingId: String = "",
    val petId: String = "",
    val userId: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val petName: String = "",
    val status: String = "pending",
    val dateTime: Long? = null,
    val locationName: String = "",
    val notes: String? = null,
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)
