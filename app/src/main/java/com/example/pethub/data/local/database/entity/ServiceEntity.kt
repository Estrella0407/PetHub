package com.example.pethub.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey
    val serviceId: String,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val availability: Boolean,
    val rating: Double,
    val imageUrl: String?,
    val durationMinutes: Int,
    val isActive: Boolean,
    val createdAt: Long?,
    val updatedAt: Long?
)