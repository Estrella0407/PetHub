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
import kotlinx.coroutines.flow.emptyFlow
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
        val userId = authRepository.getCurrentUserId()

        // If there's no user logged in, return an empty flow to prevent crashes
        if (userId == null) {
            return emptyFlow()
        }

        // Call the existing function that listens to pets in the subcollection
        return listenToUserPets(userId)
    }

    /** --------------------------------------------------
     * Get all pets for customer (One-time)
     * -------------------------------------------------- */

    suspend fun getCustomerPets(userId: String): Result<List<Pet>> {
        return firestoreHelper.getSubcollectionDocuments(
            COLLECTION_CUSTOMER,
            userId,
            COLLECTION_PET,
            Pet::class.java
        )
    }

    /** --------------------------------------------------
     * Live pets listener (Real-time updates)
     * -------------------------------------------------- */
    fun listenToUserPets(userId: String): Flow<List<Pet>> {
        return firestoreHelper.listenToSubcollection(
            COLLECTION_CUSTOMER,
            userId,
            COLLECTION_PET,
            Pet::class.java
        )
    }

    /** --------------------------------------------------
     * Get specific pet by ID (Only if stored under "pets")
     * -------------------------------------------------- */
    suspend fun getPetById(petId: String): Result<Pet?> {
        return firestoreHelper.getDocument(
            COLLECTION_PET,
            petId,
            Pet::class.java
        )
    }

    /** --------------------------------------------------
     * Add new pet (Auto ID under customers/userId/pets)
     * -------------------------------------------------- */
    suspend fun addPet(userId: String, pet: Pet): Result<String> {
        val petData = pet.copy(
            custId = userId,
            createdAt = firestoreHelper.getServerTimestamp(),
            updatedAt = firestoreHelper.getServerTimestamp()
        )

        return firestoreHelper.createSubcollectionDocument(
            COLLECTION_CUSTOMER,
            userId,
            COLLECTION_PET,
            petData
        )
    }

    /** --------------------------------------------------
     * Update pet info
     * -------------------------------------------------- */
    suspend fun updatePet(userId: String, petId: String, updates: Map<String, Any>): Result<Unit> {
        return firestoreHelper.updateDocument(
            "$COLLECTION_CUSTOMER/$userId/$COLLECTION_PET",
            petId,
            updates
        )
    }

    /** --------------------------------------------------
     * Delete pet
     * -------------------------------------------------- */
    suspend fun deletePet(userId: String, petId: String): Result<Unit> {
        val docRef = firestoreHelper
            .getDocumentReference(COLLECTION_CUSTOMER, userId)
            .collection(COLLECTION_PET)
            .document(petId)

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
