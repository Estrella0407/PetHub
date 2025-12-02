package com.example.pethub.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey
    val bookingId: String,
    val userId: String,
    val petId: String,
    val serviceId: String,
    val serviceName: String,
    val status: String,
    val dateTime: Long,
    val locationName: String,
    val notes: String?,
    val createdAt: Long?,
    val updatedAt: Long?
)