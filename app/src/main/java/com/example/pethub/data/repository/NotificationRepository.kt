package com.example.pethub.data.repository

import com.example.pethub.data.model.Notification
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_NOTIFICATION
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val firestoreHelper: FirestoreHelper,
    private val firebaseService: FirebaseService
) {

    fun getUserNotifications(): Flow<List<Notification>> {
        val custId = firebaseService.getCurrentUserId() ?: return flowOf(emptyList())
        
        return firestoreHelper.listenToCollection(
            COLLECTION_NOTIFICATION,
            Notification::class.java
        ) { query ->
            query.whereEqualTo("custId", custId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
        }
    }
    
    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return firestoreHelper.updateDocument(
            COLLECTION_NOTIFICATION,
            notificationId,
            mapOf("isRead" to true)
        )
    }

    /**
     * Creates a new notification for a specific user.
     * Use this to trigger notifications manually from the app (e.g. after Booking).
     */
    suspend fun sendNotification(custId: String, title: String, message: String, type: String = "info"): Result<Unit> {
        // Generate a unique ID for the notification
        val notificationId = UUID.randomUUID().toString()
        
        val notification = Notification(
            id = notificationId,
            custId = custId,
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            type = type
        )

        return firestoreHelper.createDocumentWithId(
            COLLECTION_NOTIFICATION,
            notificationId,
            notification
        )
    }
}
