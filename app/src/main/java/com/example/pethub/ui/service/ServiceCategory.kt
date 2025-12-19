package com.example.pethub.ui.service

// This class represents the main categories on your ServiceScreen
data class ServiceCategory(
    val categoryName: String,   // The name to display, e.g., "Grooming"
    val description: String,
    val iconResId: Int          // The local drawable resource ID for the icon
)
