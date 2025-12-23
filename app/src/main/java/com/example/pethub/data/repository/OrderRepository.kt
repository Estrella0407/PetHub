package com.example.pethub.data.repository

import com.example.pethub.data.model.Order
import com.example.pethub.data.model.OrderItem
import com.example.pethub.data.model.ProductOrder
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_ORDER
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val firestoreHelper: FirestoreHelper,
    private val authRepository: AuthRepository
) {
    suspend fun getAllOrders(): Result<List<Order>> {
        return firestoreHelper.getAllDocuments(
            COLLECTION_ORDER,
            Order::class.java
        )
    }

    suspend fun getOrderDetail(
        orderId: String
    ): Result<Order?> {

        return firestoreHelper.getDocument(
            collection = COLLECTION_ORDER,
            documentId = orderId,
            clazz = Order::class.java
        )
    }

    fun getOrdersForCurrentUser(limit: Int): Flow<List<Order>> {
        val custId = authRepository.getCurrentUserId() ?: return flowOf(emptyList()) // Return an empty list flow if no user is logged in

        return firestoreHelper.listenToCollection(
            collection = COLLECTION_ORDER,
            clazz = Order::class.java
        ) { query ->
            // Chain multiple query conditions
            query
                .whereEqualTo("custId", custId) // Filter by the current user's ID
                .whereGreaterThanOrEqualTo("pickupDateTime",
                    Timestamp.now()) // Filter for orders from now onwards (pickup date)
                .orderBy("pickupDateTime") // Order by the soonest pickup date first
                .limit(limit.toLong()) // Apply the limit
        }
    }

    fun getAllOrdersForCurrentUser(): Flow<List<Order>> {
        val custId = authRepository.getCurrentUserId() ?: return flowOf(emptyList())

        return firestoreHelper.listenToCollection(
            collection = COLLECTION_ORDER,
            clazz = Order::class.java
        ) { query ->
            query
                .whereEqualTo("custId", custId)
                .orderBy("pickupDateTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
        }
    }

    suspend fun getProductOrdersForOrder(orderId: String): Result<List<ProductOrder>> {
        val result = firestoreHelper.queryDocuments(
            collection = "productOrder",
            field = "orderId",
            value = orderId,        clazz = ProductOrder::class.java
        )

        return result.map { productOrders ->
            productOrders.sortedBy { it.productName }
        }
    }


    suspend fun getOrderItem(order: Order): OrderItem {
        // This part to get the title is correct and can remain.
        val productOrdersResult = firestoreHelper.queryDocuments(
            collection = "productOrder",
            field = "orderId",
            value = order.orderId,
            clazz = ProductOrder::class.java
        )

        val productOrders = productOrdersResult.getOrDefault(emptyList())

        val title = if (productOrders.isNotEmpty()) {
            val firstItemName = productOrders[0].productName
            if (productOrders.size > 1) {
                "$firstItemName +${productOrders.size - 1} more"
            } else {
                firstItemName
            }
        } else {
            "Order #${order.orderId.take(4)}"
        }

        val orderDateTime = order.orderDateTime ?: Timestamp.now()
        val pickupDateTime = order.pickupDateTime ?: Timestamp.now()

        // Populate OrderItem with the correct data types.
        return OrderItem(
            id = order.orderId,
            title = title,
            orderDateTime = orderDateTime,      // Pass the Timestamp object directly
            pickupDateTime = pickupDateTime,    // Pass the Timestamp object directly
            status = order.status,
            totalPrice = order.totalPrice
        )
    }

    suspend fun getPendingOrdersForCurrentUser(): List<Order> {
        val custId = authRepository.getCurrentUserId() ?: return emptyList()

        return firestoreHelper.getFirestoreInstance().collection(COLLECTION_ORDER)
            .whereEqualTo("custId", custId)
            .whereEqualTo("status", "Pending")
            .get()
            .await()
            .toObjects(Order::class.java)
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return firestoreHelper.updateDocument(
            collection = COLLECTION_ORDER,
            documentId = orderId,
            updates = mapOf("status" to status)
        )
    }
}

