package com.example.pethub.data.model

data class CartItem(
    val productId: String = "",
    val product: Product = Product(), // Embedding product details makes UI easier
    val quantity: Int = 0
)
