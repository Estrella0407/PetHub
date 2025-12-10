package com.example.pethub.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "", // e.g., "10kg Dry Cat Food"
    val price: Double = 0.0,
    val category: String = "", // "Pet Food", "Pet Treats", etc.
    val imageUrl: String = ""
)
