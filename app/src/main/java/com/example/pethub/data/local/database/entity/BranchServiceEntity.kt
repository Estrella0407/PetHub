package com.example.pethub.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "branch_service",
    primaryKeys = ["branchId", "serviceId"],
    foreignKeys = [
        ForeignKey(
            entity = ServiceEntity::class,
            parentColumns = ["serviceId"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BranchEntity::class,
            parentColumns = ["branchId"],
            childColumns = ["branchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["serviceId"])]
)
data class BranchServiceEntity(
    val branchId: String,
    val serviceId: String,
    val availability: Boolean = true,
)
