package com.example.pethub.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Notification(
    val id: String = "",
    val custId: String = "",
    val title: String = "",
    val message: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,
    @PropertyName("read")
    val isRead: Boolean = false,
    val type: String = "info", // e.g., 'info', 'appointment', 'reminder'
    val data: Map<String, String>? = null
)
