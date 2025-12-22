package com.example.pethub.data.repository

import android.util.Log
import com.example.pethub.data.model.CartItem
import com.example.pethub.data.model.Product
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
class ShopRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "ShopRepository"

    // --- 1. Get All Products (No changes needed here) ---
    fun getProducts(): Flow<List<Product>> = callbackFlow {
        Log.d(TAG, "Connecting to the Firebase 'product' collection...")

        val listener = db.collection("product")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching products", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("productId") ?: ""
                            val name = doc.getString("productName") ?: "Unknown Product"
                            val priceObj = doc.get("productPrice")
                            val price = when (priceObj) {
                                is Number -> priceObj.toDouble()
                                is String -> priceObj.toDoubleOrNull()
                                else -> 0.0
                            } ?: 0.0
                            val description = doc.getString("productDescription") ?: ""
                            val category = doc.getString("productCategory") ?: "Pet Food"
                            val img1 = doc.getString("productImageUrl")
                            val img2 = doc.getString("imageUrl")
                            val imageUrl = img1 ?: img2 ?: ""

                            Product(
                                id = id,
                                name = name,
                                price = price,
                                description = description,
                                category = category,
                                imageUrl = imageUrl
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing document ${doc.id}", e)
                            null
                        }
                    }
                    trySend(products)
                } else {
                    trySend(emptyList())
                }
            }
            awaitClose { listener.remove() }
    }

    // --- 2. Get Cart Items (UPDATED PATH) ---
    fun getCartItems(): Flow<List<CartItem>> = callbackFlow {
        val custId = auth.currentUser?.uid
        if (custId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("customer").document(custId).collection("cart")
            .addSnapshotListener { snapshot, _ ->
                val items = snapshot?.toObjects(CartItem::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    // --- 3. Add to Cart (UPDATED PATH) ---
    suspend fun addToCart(product: Product) {
        val custId = auth.currentUser?.uid

        if (custId == null) {
            Log.e(TAG, "Cannot add to cart: User not logged in")
            return
        }

        if (product.id.isEmpty()) {
            Log.e(TAG, "Cannot add to cart: Product ID is empty.")
            return
        }

        // CHANGED: "users" -> "customer"
        val cartRef = db.collection("customer").document(custId).collection("cart")
        val docRef = cartRef.document(product.id)

        try {
            val snapshot = docRef.get().await()

            if (snapshot.exists()) {
                val currentQuantity = snapshot.getLong("quantity") ?: 0
                docRef.update("quantity", currentQuantity + 1).await()
            } else {
                val newItem = CartItem(
                    product = product,
                    quantity = 1
                )
                docRef.set(newItem).await()
            }
            Log.d(TAG, "Added ${product.name} to cart.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add to cart", e)
        }
    }

    // --- 4. Update Cart Quantity (UPDATED PATH) ---
    suspend fun updateCartItem(cartItem: CartItem) {
        val custId = auth.currentUser?.uid ?: return

        // CHANGED: "users" -> "customer"
        val cartRef = db.collection("customer").document(custId).collection("cart")

        if (cartItem.quantity > 0) {
            cartRef.document(cartItem.product.id).set(cartItem).await()
        } else {
            cartRef.document(cartItem.product.id).delete().await()
        }
    }

    // --- 5. Place Order (UPDATED to Decrement Stock) ---
    suspend fun placeOrder(
        branchId: String,  // <--- CHANGED: Now takes ID, not Name
        branchName: String, // Pass name for Order record
        pickupDate: String,
        pickupTime: String,
        cartItems: List<CartItem>,
        totalAmount: Double
    ): Result<Boolean> {
        val custId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

        if (cartItems.isEmpty()) return Result.failure(Exception("Cart is empty"))

        return try {
            val batch = db.batch()

            // ADDED: Pre-check stock availability
            // We need to find the branchProduct documents for this branch + these products
            for (item in cartItems) {
                // Find the branchProduct document
                // Note: This assumes a specific structure (branchId matches).
                // Since this could be complex to query inside the transaction loop without document IDs,
                // we'll do a query first.
                val bpQuery = db.collection("branchProduct")
                    .whereEqualTo("branchId", branchId)
                    .whereEqualTo("productId", item.product.id)
                    .get()
                    .await()

                if (bpQuery.isEmpty) {
                     return Result.failure(Exception("Product ${item.product.name} not available at this branch."))
                }

                val bpDoc = bpQuery.documents[0]
                val currentStock = bpDoc.getLong("stock")?.toInt() ?: 0

                if (currentStock < item.quantity) {
                    return Result.failure(Exception("Insufficient stock for ${item.product.name}. Available: $currentStock"))
                }

                // Decrement stock in batch
                batch.update(bpDoc.reference, "stock", currentStock - item.quantity)
            }


            // A. Create Main Order (No changes needed here)
            val orderRef = db.collection("order").document()
            val orderId = orderRef.id

            val dateFormat = java.text.SimpleDateFormat("d/M/yyyy HH:mm", java.util.Locale.getDefault())
            val parsedDate = try {
                dateFormat.parse("$pickupDate $pickupTime")
            } catch (e: Exception) {
                null
            }
            val pickupTimestamp = parsedDate?.let { Timestamp(it) } ?: Timestamp.now()

            val newOrder = hashMapOf(
                "orderId" to orderId,
                "custId" to custId,
                "branchId" to branchId, // Save ID too
                "branchName" to branchName,
                "pickupDateTime" to pickupTimestamp,
                "orderDateTime" to Timestamp.now(),
                "totalPrice" to totalAmount,
                "status" to "Pending"
            )
            batch.set(orderRef, newOrder)

            // B. Create Product Orders (No changes needed here)
            cartItems.forEach { item ->
                val productOrderRef = db.collection("productOrder").document()
                val productOrderData = hashMapOf(
                    "productOrderId" to productOrderRef.id,
                    "orderId" to orderId,
                    "productId" to item.product.id,
                    "productName" to item.product.name,
                    "quantity" to item.quantity,
                    "priceAtPurchase" to item.product.price
                )
                batch.set(productOrderRef, productOrderData)
            }

            // C. Clear Cart (UPDATED PATH)
            // CHANGED: "users" -> "customer"
            val cartCollection = db.collection("customer").document(custId).collection("cart")
            val snapshot = cartCollection.get().await()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }

            // D. Commit Transaction
            batch.commit().await()
            Result.success(true)

        } catch (e: Exception) {
            Log.e(TAG, "Place order failed", e)
            Result.failure(e)
        }
    }

    // --- 6. Get Branch List (UPDATED to return Branch Objects) ---
    fun getBranches(): Flow<List<com.example.pethub.data.model.Branch>> = callbackFlow {
        val listener = db.collection("branch")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting branches", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val branches = snapshot.toObjects(com.example.pethub.data.model.Branch::class.java)
                    trySend(branches)
                } else {
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }
}