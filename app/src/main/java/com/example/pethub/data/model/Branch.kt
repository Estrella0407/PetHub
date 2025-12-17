package com.example.pethub.data.model

import com.google.firebase.firestore.DocumentId

data class Branch(
    @DocumentId val branchId: String = "",
    val branchName: String = "",
    val branchAddress: String = "",
    val branchPhone: String = "",
    val branchEmail: String = "",
    val branchPassword: String = "",
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)
