package com.example.pethub.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey
    val petId: String,
    val ownerId: String,
    val name: String,
    val species: String,
    val breed: String,
    val gender: String,
    val dateOfBirth: Long?,
    val weight: Double?,
    val medicalRecords: String?,
    val imageUrl: String?,
    val createdAt: Long?,
    val updatedAt: Long?
)