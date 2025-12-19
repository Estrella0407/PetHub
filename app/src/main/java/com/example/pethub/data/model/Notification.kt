package com.example.pethub.data.model

data class Notification(
    val id: String = "",
    val custId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false,
    val type: String = "info" // e.g., 'info', 'appointment', 'reminder'
)
