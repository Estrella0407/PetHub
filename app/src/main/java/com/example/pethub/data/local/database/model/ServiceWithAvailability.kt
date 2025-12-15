package com.example.pethub.data.local.database.model

import androidx.room.Embedded
import com.example.pethub.data.local.database.entity.ServiceEntity

data class ServiceWithAvailability(
    @Embedded val service: ServiceEntity,  // Holds Name, Desc, Price, etc.
    val availability: Boolean,             // Holds the branch specific availability
    val branchId: String
)