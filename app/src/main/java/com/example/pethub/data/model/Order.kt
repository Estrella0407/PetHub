package com.example.pethub.data.model

import com.google.firebase.Timestamp

data class Order(
    val orderId: String = "",
    val custId: String = "",
    val branchName: String = "",
    val orderDateTime: Timestamp? = null,
    val pickupDateTime: Timestamp? = null,
    val totalPrice: Double = 0.0,
    val status: String = "Pending", // Pending, Completed, Cancelled
)

data class OrderItem(
    val id: String,
    val title: String,
    val orderDateTime: Timestamp,
    val pickupDateTime: Timestamp,
    val status: String,
    val totalPrice: Double
)
