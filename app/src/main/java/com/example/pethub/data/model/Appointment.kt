package com.example.pethub.data.model

import com.google.firebase.Timestamp


data class Appointment(
    val appointmentId: String = "",
    val dateTime: Timestamp? = null,
    val status: String = "pending",
    val branchId: String = "",
    val petId: String = "",
    val serviceId: String = "",
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)

/**
 * Data class for appointment items in UI
 */
data class AppointmentItem(
    val id: String,
    val serviceName: String?,
    val owner: Customer,
    val pet: Pet,
    val dateTime: String,
    val locationName: String,
    val status: String
)
