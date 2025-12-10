package com.example.pethub.data.repository

import com.example.pethub.data.local.database.entity.CustomerEntity // 👈 Make sure this import is added
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

    fun getCurrentUser() = firebaseService.getCurrentUser()

    fun isUserAuthenticated() = firebaseService.isUserAuthenticated()

    // 👇 =================== NEW FUNCTION ADDED HERE =================== 👇
    /**
     * Gets the current user ID from Firebase Auth and uses it to fetch
     * the corresponding customer profile from Firestore.
     * This is the correct way to get the CustomerEntity for the logged-in user.
     */
    suspend fun getCustomerDetails(): Result<CustomerEntity?> {
        // 1. Get the current user's ID from Firebase Auth
        val userId = firebaseService.getCurrentUserId()
            ?: return Result.failure(Exception("No authenticated user found."))

        // 2. Use the ID to fetch the CustomerEntity from Firestore
        return firestoreHelper.getDocument(
            collection = COLLECTION_CUSTOMER,
            documentId = userId,
            clazz = CustomerEntity::class.java
        )
    }
    // 👆 =============================================================== 👆

    suspend fun signIn(email: String, password: String) =
        firebaseService.signIn(email, password)

    suspend fun register(email: String, username:String, password: String, phone:String, address: String): Result<String> {//, username: String): Result<String> {
        // Create Firebase Auth user
        val authResult = firebaseService.registerUser(email, password)//, username)

        if (authResult.isFailure) {
            return Result.failure(authResult.exceptionOrNull()!!)
        }

        val user = authResult.getOrNull()!!

        // Create Firestore Customer document
        val customerData = Customer(
            custId = user.uid,
            custName = username,
            custEmail = email,
            custPassword = password,
            custAddress = address,
            custPhone = phone,

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
            // Save the FCM token to the newly created user document
            firebaseService.updateFCMToken()
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
