package com.example.pethub.data.model

import com.google.firebase.firestore.DocumentId

data class BranchProduct(
    @DocumentId val id: String = "",
    val branchId: String = "",
    val productId: String = "",
    val stock: Int = 0
)
