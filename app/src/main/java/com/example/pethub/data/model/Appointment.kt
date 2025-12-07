package com.example.pethub.data.model

data class Appointment(
    val appointmentId: String = "",
    val dateTime: Long? = null,
    val status: String = "pending",
    val branchId: String = "",
    val petId: String = "",
    val breed: String = "",
    val serviceId: String = "",
    val custId: String = "",
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)

/**
 * Data class for appointment items in UI
 */
data class AppointmentItem(
    val id: String,
    val serviceName: String,
    val petName: String,
    val dateTime: String,
    val locationName: String,
    val status: String
)
