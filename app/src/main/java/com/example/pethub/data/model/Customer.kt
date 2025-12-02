package com.example.pethub.data.model

data class Customer(
    val custId: String = "",
    val custName: String = "",
    val custPassword: String = "", // Note: Storing passwords in plain text is not recommended. Consider using Firebase Auth UID linkage only.
    val custPhone: String = "",
    val custEmail: String = "",
    val custAddress: String = "",
    // Keeping some metadata fields is good practice
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)
