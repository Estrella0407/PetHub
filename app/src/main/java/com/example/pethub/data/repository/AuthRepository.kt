package com.example.pethub.data.repository

import com.example.pethub.data.local.database.dao.BookingDao
import com.example.pethub.data.local.database.dao.UserDao
import com.example.pethub.data.local.prefs.PreferenceManager
import com.example.pethub.data.model.*
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_USERS
import com.example.pethub.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

// ============================================
// AUTH REPOSITORY
// ============================================

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseService: FirebaseService,
    private val firestoreHelper: FirestoreHelper,
    private val preferenceManager: PreferenceManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun observeAuthState() = firebaseService.observeAuthState()

    fun getCurrentUserId() = firebaseService.getCurrentUserId()

    fun isUserAuthenticated() = firebaseService.isUserAuthenticated()

    suspend fun signIn(email: String, password: String) =
        firebaseService.signIn(email, password)

    suspend fun register(email: String, password: String, username: String): Result<String> {
        // 1. Create Firebase Auth user
        val authResult = firebaseService.registerUser(email, password, username)

        if (authResult.isFailure) {
            return Result.failure(authResult.exceptionOrNull()!!)
        }

        val user = authResult.getOrNull()!!

        // 2. Create Firestore user document
        val userData = User(
            userId = user.uid,
            email = email,
            username = username,
            password = password,
            role = "customer",
            createdAt = firestoreHelper.getServerTimestamp(),
            updatedAt = firestoreHelper.getServerTimestamp()
        )

        val createResult = firestoreHelper.createDocumentWithId(
            COLLECTION_USERS,
            user.uid,
            userData
        )

        return if (createResult.isSuccess) {
            Result.success(user.uid)
        } else {
            Result.failure(createResult.exceptionOrNull()!!)
        }
    }

    suspend fun signOut() {
        firebaseService.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String) =
        firebaseService.sendPasswordResetEmail(email)

    suspend fun isAdmin() = firebaseService.isAdmin()
}