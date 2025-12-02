package com.example.pethub.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val email: String,
    val name: String,
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val role: String,
    val createdAt: Long,
    val updatedAt: Long
)