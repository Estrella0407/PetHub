package com.example.pethub.data.repository

import com.example.pethub.data.local.prefs.PreferenceManager
import com.example.pethub.data.model.Customer
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_BRANCH
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_CUSTOMER
import com.example.pethub.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

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

    suspend fun register(email: String, password: String): Result<String> {//, username: String): Result<String> {
        // Create Firebase Auth user
        val authResult = firebaseService.registerUser(email, password)//, username)

        if (authResult.isFailure) {
            return Result.failure(authResult.exceptionOrNull()!!)
        }

        val user = authResult.getOrNull()!!

        // Create Firestore Customer document
        val customerData = Customer(
            custId = user.uid,
//            custName = username,
            custEmail = email,
            custPassword = password,
            // No role field in Customer, assumed "customer"
            createdAt = firestoreHelper.getServerTimestamp(),
            updatedAt = firestoreHelper.getServerTimestamp()
        )

        val createResult = firestoreHelper.createDocumentWithId(
            COLLECTION_CUSTOMER,
            user.uid,
            customerData
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

    suspend fun isAdmin(): Boolean {
        // Check if the current user ID exists in the 'branches' collection
        // as Admin = Branch in this new schema.
        val userId = firebaseService.getCurrentUserId() ?: return false
        
        // 1. Check Firebase Custom Claim (fastest)
        if (firebaseService.isAdmin()) return true
        
        // 2. Fallback: Check if document exists in 'branches' collection
        // This allows manual DB entry for branches to grant admin access
        val branchExists = firestoreHelper.documentExists(COLLECTION_BRANCH, userId)
        return branchExists.getOrDefault(false)
    }
}
