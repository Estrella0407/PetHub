package com.example.pethub.data.repository

import com.example.pethub.data.model.Notification
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_NOTIFICATION
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Date
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

    fun hasUnreadNotifications(): Flow<Boolean> {
        val custId = firebaseService.getCurrentUserId() ?: return flowOf(false)

        return firestoreHelper.listenToCollection(
            COLLECTION_NOTIFICATION,
            Notification::class.java
        ) { query ->
            // Query for documents that are unread and belong to the current user
            query.whereEqualTo("custId", custId)
                .whereEqualTo("read", false)
        }.map { notifications ->
            // If the resulting list is not empty, it means there are unread notifications
            notifications.isNotEmpty()
        }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return firestoreHelper.updateDocument(
            COLLECTION_NOTIFICATION,
            notificationId,
            mapOf("read" to true)  // Changed from "isRead" to "read"
        )
    }

    /**
     * Creates a new notification for a specific user.
     * Use this to trigger notifications manually from the app (e.g. after Booking).
     */
    suspend fun sendNotification(
        custId: String,
        title: String,
        message: String,
        type: String = "info",
        data: Map<String, String>? = null
    ): Result<Unit> {
        // Generate a unique ID for the notification
        val notificationId = UUID.randomUUID().toString()
        
        val notification = Notification(
            id = notificationId,
            custId = custId,
            title = title,
            message = message,
            timestamp = null,
            isRead = false,
            type = type,
            data = data
        )

        return firestoreHelper.createDocumentWithId(
            COLLECTION_NOTIFICATION,
            notificationId,
            notification
        )
    }
}
