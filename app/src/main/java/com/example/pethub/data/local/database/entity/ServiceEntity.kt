package com.example.pethub.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service")
data class ServiceEntity(
    @PrimaryKey
    val serviceId: String,
    val serviceName: String,
    val description: String,
    val type: String,
    val price: Double,
    val imageUrl: String?
)