package com.example.pethub.data.repository

import android.util.Log
import com.example.pethub.data.model.CartItem
import com.example.pethub.data.model.Product
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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

        // CHANGED: "users" -> "customer"
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
                    productId = product.id,
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
        if (cartItem.productId.isEmpty()) {
            Log.e(TAG, "Cannot update cart: cartItem.productId is empty")
            return
        }
        // CHANGED: "users" -> "customer"
        val cartRef = db.collection("customer").document(custId).collection("cart")

        if (cartItem.quantity > 0) {
            cartRef.document(cartItem.productId).set(cartItem).await()
        } else {
            cartRef.document(cartItem.productId).delete().await()
        }
    }

    // --- 5. Place Order (UPDATED PATH for Clearing Cart) ---
    suspend fun placeOrder(
        branchName: String,
        pickupDate: String,
        pickupTime: String,
        cartItems: List<CartItem>,
        totalAmount: Double
    ): Result<Boolean> {
        val custId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

        if (cartItems.isEmpty()) return Result.failure(Exception("Cart is empty"))

        return try {
            // 1. Resolve branchId from branchName
            val branchQuerySnapshot = db.collection("branch")
                .whereEqualTo("branchName", branchName)
                .get()
                .await()
            val branchDocument = branchQuerySnapshot.documents.firstOrNull()
                ?: return Result.failure(Exception("Branch not found for name: $branchName"))
            val branchId = branchDocument.id

            // 2. Check stock for each product in this branch before placing order
            //    Map: productId -> branchProduct documentId
            val branchProductDocIds = mutableMapOf<String, String>()
            for (item in cartItems) {
                Log.d(TAG, "Checking stock for productId=${item.productId}, name=${item.product.name}, branchId=$branchId, branchName=$branchName")

                val bpQuerySnapshot = db.collection("branchProduct")
                    .whereEqualTo("branchId", branchId)
                    .whereEqualTo("productId", item.productId)
                    .get()
                    .await()

                Log.d(TAG, "branchProduct query result size=${bpQuerySnapshot.size()} for productId=${item.productId}")

                val bpDoc = bpQuerySnapshot.documents.firstOrNull()
                    ?: return Result.failure(
                        Exception("${item.product.name} is out of stock in $branchName.")
                    )

                val currentStock = bpDoc.getLong("stock") ?: 0L
                Log.d(TAG, "Current stock for productId=${item.productId} is $currentStock")

                if (currentStock <= 0L || currentStock < item.quantity.toLong()) {
                    return Result.failure(
                        Exception("${item.product.name} is out of stock in $branchName.")
                    )
                }

                branchProductDocIds[item.productId] = bpDoc.id
            }

            val batch = db.batch()

            // A. Create Main Order (now also storing branchId)
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
                "branchId" to branchId,
                "branchName" to branchName,
                "pickupTime" to pickupTimestamp,
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
                    "productId" to item.productId,
                    "productName" to item.product.name,
                    "productImageUrl" to item.product.imageUrl,
                    "quantity" to item.quantity,
                    "priceAtPurchase" to item.product.price,
                    "custId" to custId
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

            // D. Commit Order & Cart Changes
            batch.commit().await()

            // E. Decrease stock for each product in branchProduct
            for (item in cartItems) {
                val bpDocId = branchProductDocIds[item.productId] ?: continue
                val bpRef = db.collection("branchProduct").document(bpDocId)
                try {
                    bpRef.update("stock", FieldValue.increment(-item.quantity.toLong())).await()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to decrease stock for product ${item.productId}", e)
                }
            }

            Result.success(true)

        } catch (e: Exception) {
            Log.e(TAG, "Place order failed", e)
            Result.failure(e)
        }
    }

    // --- 6. Get Branch List (No changes needed here) ---
    fun getBranches(): Flow<List<String>> = callbackFlow {
        val listener = db.collection("branch")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting branches", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val branchNames = snapshot.documents.mapNotNull { doc ->
                        doc.getString("branchName") ?: doc.getString("name")
                    }
                    trySend(branchNames)
                } else {
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }
}