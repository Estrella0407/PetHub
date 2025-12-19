package com.example.pethub.data.model

data class Customer(
    val custId: String = "",
    val custName: String = "",
    val custPassword: String = "", // Note: Storing passwords in plain text is not recommended.
    val custPhone: String = "",
    val custEmail: String = "",
    val custAddress: String = "",
    val profileImageUrl: String? = null, // Added for profile image
    val fcmToken: String = "",
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)
