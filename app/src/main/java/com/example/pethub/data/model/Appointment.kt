package com.example.pethub.data.model

data class Appointment(
    val appointmentId: String = "",
    val dateTime: Long? = null,
    val status: String = "pending",
    val branchId: String = "",
    val petId: String = "",
    val serviceId: String = "",
    // Added custId to facilitate security rules and queries for "My Appointments"
    val custId: String = "",
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)
