package com.example.pethub.data.repository

import android.net.Uri
import com.example.pethub.data.local.database.dao.PetDao
import com.example.pethub.data.model.Pet
import com.example.pethub.data.remote.CloudinaryService
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_CUSTOMER
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_PET
import com.example.pethub.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepository @Inject constructor(
    private val firebaseService: FirebaseService,
    private val firestoreHelper: FirestoreHelper,
    private val cloudinaryService: CloudinaryService,
    private val authRepository: AuthRepository,
    private val dao: PetDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun getPetsForCurrentUser(): Flow<List<Pet>> {
        // Get the current user's ID from the AuthRepository
        // If there's no user logged in, return a flow with an empty list
        val userId = authRepository.getCurrentUserId() ?: return flowOf(emptyList())

        // Call the function that listens to pets in the top-level collection
        return listenToUserPets(userId)
    }

    /** --------------------------------------------------
     * Get all pets for customer (One-time)
     * -------------------------------------------------- */

    suspend fun getCustomerPets(userId: String): Result<List<Pet>> {
        // Query top-level collection where custId matches
        return firestoreHelper.queryDocuments(
            COLLECTION_PET,
            "custId",
            userId,
            Pet::class.java
        )
    }

    /** --------------------------------------------------
     * Live pets listener (Real-time updates)
     * -------------------------------------------------- */
    fun listenToUserPets(userId: String): Flow<List<Pet>> {
        // Listen to top-level collection with query
        return firestoreHelper.listenToCollection(
            COLLECTION_PET,
            Pet::class.java
        ) { query ->
            query.whereEqualTo("custId", userId)
        }
    }

    /** --------------------------------------------------
     * Get specific pet by ID
     * -------------------------------------------------- */
    suspend fun getPetById(userId: String, petId: String): Result<Pet?> {
        // Get from top-level collection
        return firestoreHelper.getDocument(
            COLLECTION_PET,
            petId,
            Pet::class.java
        )
    }

    /** --------------------------------------------------
     * Add new pet (Auto ID under top-level 'pet' collection)
     * -------------------------------------------------- */
    suspend fun addPet(userId: String, pet: Pet): Result<String> {
        val petData = pet.copy(
            custId = userId,
        )

        // Create document in top-level collection
        return firestoreHelper.createDocument(
            COLLECTION_PET,
            petData
        )
    }

    /** --------------------------------------------------
     * Update pet info
     * -------------------------------------------------- */
    suspend fun updatePet(userId: String, petId: String, updates: Map<String, Any>): Result<Unit> {
        return firestoreHelper.updateDocument(
            COLLECTION_PET,
            petId,
            updates
        )
    }

    /** --------------------------------------------------
     * Delete pet
     * -------------------------------------------------- */
    suspend fun deletePet(userId: String, petId: String): Result<Unit> {
        val docRef = firestoreHelper
            .getDocumentReference(COLLECTION_PET, petId)

        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** --------------------------------------------------
     * Upload pet image
     * -------------------------------------------------- */
    suspend fun uploadPetImage(userId: String, petId: String, imageUri: Uri): Result<String> {
        return firebaseService.uploadPetImage(userId, petId, imageUri)
    }
}
