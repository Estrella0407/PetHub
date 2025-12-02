package com.example.pethub.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "branches")
data class BranchEntity(
    @PrimaryKey
    val branchId: String,
    val branchName: String,
    val branchAddress: String,
    val branchPhone: String,
    val branchEmail: String,
    val createdAt: Long?,
    val updatedAt: Long?
)
