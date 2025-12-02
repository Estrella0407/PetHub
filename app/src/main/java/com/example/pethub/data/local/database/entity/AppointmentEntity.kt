package com.example.pethub.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey
    val appointmentId: String,
    val dateTime: Long?,
    val status: String,
    val branchId: String,
    val petId: String,
    val serviceId: String
)
