package com.example.pethub.data.repository

import android.net.Uri
import com.example.pethub.data.local.database.dao.BookingDao
import com.example.pethub.data.local.database.dao.UserDao
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_USERS
import com.example.pethub.data.model.*
import com.example.pethub.data.remote.CloudinaryService
import com.example.pethub.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

// ============================================
// USER REPOSITORY
// ============================================

@Singleton
class UserRepository @Inject constructor(
    private val firebaseService: FirebaseService,
    private val firestoreHelper: FirestoreHelper,
    private val cloudinaryService: CloudinaryService,
    private val dao: UserDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getCurrentUser(): Result<User?> {
        val userId = firebaseService.getCurrentUserId()
            ?: return Result.success(null)

        return firestoreHelper.getDocument(COLLECTION_USERS, userId, User::class.java)
    }

    fun listenToCurrentUser(): Flow<User?> {
        val userId = firebaseService.getCurrentUserId() ?: return flowOf(null)
        return firestoreHelper.listenToDocument(COLLECTION_USERS, userId, User::class.java)
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> {
        return firestoreHelper.updateDocument(COLLECTION_USERS, userId, updates)
    }

    suspend fun uploadProfileImage(imageUri: Uri): Result<String> {
        val userId = firebaseService.getCurrentUserId()
            ?: return Result.failure(Exception("No user logged in"))

        return firebaseService.uploadProfileImage(userId, imageUri)
    }

    suspend fun updateProfileImage(imageUrl: String): Result<Unit> {
        val userId = firebaseService.getCurrentUserId()
            ?: return Result.failure(Exception("No user logged in"))

        return firestoreHelper.updateDocument(
            COLLECTION_USERS,
            userId,
            mapOf("profileImageUrl" to imageUrl)
        )
    }
}