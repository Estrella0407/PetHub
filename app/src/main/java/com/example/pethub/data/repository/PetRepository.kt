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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

    /**
     * Performs a one-time fetch of the current user's pets.
     */
    suspend fun getCurrentUserPets(): Result<List<Pet>> = withContext(ioDispatcher) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            return@withContext Result.success(emptyList())
        }
        // Use the existing one-time query function
        return@withContext firestoreHelper.queryDocuments(
            COLLECTION_PET,
            "custId",
            userId,
            Pet::class.java
        )
    }

    fun getPetsForCurrentUser(): Flow<List<Pet>> {
        // Get the current user's ID from the AuthRepository
        // If there's no user logged in, return a flow with an empty list
        val userId = authRepository.getCurrentUserId() ?: return flowOf(emptyList())

        // Call the function that listens to pets in the top-level collection
        return listenToUserPets(userId)
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
        }.map { pets ->
            // Manual mapping of document ID to petId for stability with existing data
            pets.map { it.copy(petId = it.petId.ifBlank { "unknown" }) }
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
        return try {
            val docRef = firestoreHelper.getFirestoreInstance().collection(COLLECTION_PET).document()
            val petData = pet.copy(
                petId = docRef.id,
                custId = userId,
            )
            docRef.set(petData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** --------------------------------------------------
     * Update pet info
     * -------------------------------------------------- */
    suspend fun updatePet(userId: String, petId: String, updates: Map<String, Any?>): Result<Unit> {
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
