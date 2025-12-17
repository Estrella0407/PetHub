package com.example.pethub.data.model

import com.google.firebase.Timestamp

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val branchName: String = "",
    val pickupDate: String = "",
    val pickupTime: String = "",
    val totalPrice: Double = 0.0,
    val status: String = "Pending", // Pending, Completed, Cancelled
    val timestamp: Timestamp = Timestamp.now()
)
