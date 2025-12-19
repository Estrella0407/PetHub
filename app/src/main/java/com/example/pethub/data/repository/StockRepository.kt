package com.example.pethub.data.repository

import android.util.Log
import com.example.pethub.data.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val TAG = "StockRepository"

    fun getBranchProductsWithDetails(): Flow<List<StockItem>> = callbackFlow {
        val branchId = auth.currentUser?.uid
        if (branchId == null) {
            trySend(emptyList())
            return@callbackFlow
        }

        val branchProductListener = firestore.collection("branchProduct")
            .whereEqualTo("branchId", branchId) // Filter by current branchId
            .addSnapshotListener { bpSnapshot, bpError ->
                if (bpError != null) {
                    close(bpError)
                    return@addSnapshotListener
                }

                if (bpSnapshot != null) {
                    // Fetch products to map details
                    firestore.collection("product").get()
                        .addOnSuccessListener { pSnapshot ->
                            val products = pSnapshot.documents.mapNotNull { doc ->
                                try {
                                    val id = doc.getString("productId") ?: ""
                                    val name = doc.getString("productName") ?: ""
                                    val img1 = doc.getString("productImageUrl")
                                    val img2 = doc.getString("imageUrl")
                                    val imageUrl = img1 ?: img2 ?: ""
                                    Product(id = id, name = name, imageUrl = imageUrl)
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            val items = bpSnapshot.documents.mapNotNull { doc ->
                                val bpProductId = doc.getString("productId") ?: ""
                                val product = products.find { it.id == bpProductId }

                                StockItem(
                                    documentId = doc.id, // Use Firestore Document ID for updates
                                    productName = product?.name ?: "Unknown Product",
                                    stockCount = doc.getLong("stock")?.toInt() ?: 0,
                                    imageUrl = product?.imageUrl ?: ""
                                )
                            }
                            trySend(items)
                        }
                }
            }
        awaitClose { branchProductListener.remove() }
    }

    suspend fun updateStock(documentId: String, newStock: Int): Result<Unit> {
        return try {
            firestore.collection("branchProduct").document(documentId)
                .update("stock", newStock)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating stock", e)
            Result.failure(e)
        }
    }
}

data class StockItem(
    val documentId: String,
    val productName: String,
    val stockCount: Int,
    val imageUrl: String
)
