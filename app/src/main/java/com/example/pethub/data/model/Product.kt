package com.example.pethub.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val category: String = "Pet Food",
    val imageUrl: String = ""
)
