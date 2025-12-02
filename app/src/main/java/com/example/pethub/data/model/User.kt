package com.example.pethub.data.model

data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "customer",
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)
