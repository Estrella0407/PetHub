package com.example.pethub.data.model

data class ProductOrder(
    val productOrderId: String = "",
    val orderId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productImageUrl: String = "",
    val quantity: Int = 0,
    val priceAtPurchase: Double = 0.0
)
