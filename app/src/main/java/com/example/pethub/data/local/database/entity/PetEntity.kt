package com.example.pethub.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Entity(tableName = "pet")
data class PetEntity(
    @PrimaryKey
    val petId: String,
    val petName: String,
    val dateOfBirth: Date?,
    val type: String,
    val breed: String,
    val weight: Double?,
    val sex: String,
    val remarks: String?,
    val custId: String,
    val imageUrl: String?,
)