package com.example.pethub.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey
    val custId: String,
    val custName: String,
    val custEmail: String,
    val custPhone: String?,
    val custAddress: String?,
    val createdAt: Long?,
    val updatedAt: Long?
)
