package com.example.pethub.data.repository

import android.util.Log
import com.example.pethub.data.model.CartItem
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
class ShopRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "ShopRepository"

    // --- 1. Get All Products (Manual Mapping for Robustness) ---
    fun getProducts(): Flow<List<Product>> = callbackFlow {
        Log.d(TAG, "Connecting to the Firebase 'product' collection...")

        // Ensure collection name matches Firebase exactly ("product")
        val listener = db.collection("product")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching products", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d(TAG, "Found ${snapshot.size()} documents. Starting manual parsing...")

                    val products = snapshot.documents.mapNotNull { doc ->
                        try {
                            // --- Manual Field Extraction ---
                            // 1. ID
                            val id = doc.getString("productId") ?: ""

                            // 2. Name
                            val name = doc.getString("productName") ?: "Unknown Product"

                            // 3. Price (Handles both Number and String types in Firebase)
                            val priceObj = doc.get("productPrice")
                            val price = when (priceObj) {
                                is Number -> priceObj.toDouble()
                                is String -> priceObj.toDoubleOrNull()
                                else -> 0.0
                            } ?: 0.0

                            // 4. Description
                            val description = doc.getString("productDescription") ?: ""

                            // 5. Category
                            val category = doc.getString("productCategory") ?: "Pet Food"

                            // 6. Image URL (Checks "productImageUrl" first, then fallback to "imageUrl")
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
                            null // Skip invalid documents instead of crashing
                        }
                    }

                    if (products.isNotEmpty()) {
                        Log.d(TAG, "Parsing successful! First item: ${products[0].name}, price: ${products[0].price}")
                    } else {
                        Log.w(TAG, "List is empty after parsing. Check Firebase field names.")
                    }

                    trySend(products)
                } else {
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    // --- 2. Get Cart Items (Real-time) ---
    fun getCartItems(): Flow<List<CartItem>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(userId).collection("cart")
            .addSnapshotListener { snapshot, _ ->
                val items = snapshot?.toObjects(CartItem::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    // --- 3. Add to Cart (With Safety Checks) ---
    suspend fun addToCart(product: Product) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Log.e(TAG, "Cannot add to cart: User not logged in")
            return
        }

        if (product.id.isEmpty()) {
            Log.e(TAG, "Cannot add to cart: Product ID is empty. Check mapping.")
            return
        }

        val cartRef = db.collection("users").document(userId).collection("cart")
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

    // --- 4. Update Cart Quantity ---
    suspend fun updateCartItem(cartItem: CartItem) {
        val userId = auth.currentUser?.uid ?: return
        val cartRef = db.collection("users").document(userId).collection("cart")

        if (cartItem.quantity > 0) {
            cartRef.document(cartItem.productId).set(cartItem).await()
        } else {
            cartRef.document(cartItem.productId).delete().await()
        }
    }

    // --- 5. Place Order (Save to 'order' & 'productOrder', then Clear Cart) ---
    suspend fun placeOrder(
        branchName: String,
        pickupDate: String,
        pickupTime: String,
        cartItems: List<CartItem>,
        totalAmount: Double
    ): Result<Boolean> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

        if (cartItems.isEmpty()) return Result.failure(Exception("Cart is empty"))

        return try {
            val batch = db.batch()

            // A. Create Main Order
            val orderRef = db.collection("order").document()
            val orderId = orderRef.id

            // Parse Date String to Timestamp
            val dateFormat = java.text.SimpleDateFormat("d/M/yyyy HH:mm", java.util.Locale.getDefault())
            val parsedDate = try {
                dateFormat.parse("$pickupDate $pickupTime")
            } catch (e: Exception) {
                null
            }
            val pickupTimestamp = parsedDate?.let { com.google.firebase.Timestamp(it) } ?: com.google.firebase.Timestamp.now()

            val newOrder = hashMapOf(
                "orderId" to orderId,
                "userId" to userId, // Matches 'custId' in some legacy data, keep consistent with your needs
                "custId" to userId, // Add both if unsure about legacy UI requirements
                "branchName" to branchName,
                "pickupTime" to pickupTimestamp,
                "orderDateTime" to com.google.firebase.Timestamp.now(),
                "totalPrice" to totalAmount,
                "status" to "Pending"
            )
            batch.set(orderRef, newOrder)

            // B. Create Product Orders
            cartItems.forEach { item ->
                val productOrderRef = db.collection("productOrder").document()
                val productOrderData = hashMapOf(
                    "productOrderId" to productOrderRef.id,
                    "orderId" to orderId,
                    "productId" to item.productId,
                    "productName" to item.product.name,
                    "quantity" to item.quantity,
                    "priceAtPurchase" to item.product.price
                )
                batch.set(productOrderRef, productOrderData)
            }

            // C. Clear Cart
            val cartCollection = db.collection("users").document(userId).collection("cart")
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

    // --- 6. Get Branch List (For Cart Dropdown) ---
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
                        // Checks "branchName" first, fallback to "name"
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
