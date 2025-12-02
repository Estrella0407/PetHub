package com.example.pethub.data.repository

import android.net.Uri
import com.example.pethub.data.local.database.dao.PetDao
import com.example.pethub.data.model.*
import com.example.pethub.data.remote.CloudinaryService
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_PETS
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_USERS
import com.example.pethub.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// ============================================
// PET REPOSITORY
// ============================================

@Singleton
class PetRepository @Inject constructor(
    private val firebaseService: FirebaseService,
    private val firestoreHelper: FirestoreHelper,
    private val cloudinaryService: CloudinaryService,
    private val dao: PetDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getUserPets(userId: String): Result<List<Pet>> {
        return firestoreHelper.getSubcollectionDocuments(
            COLLECTION_USERS,
            userId,
            COLLECTION_PETS,
            Pet::class.java
        )
    }

    fun listenToUserPets(userId: String): Flow<List<Pet>> {
        return firestoreHelper.listenToSubcollection(
            COLLECTION_USERS,
            userId,
            COLLECTION_PETS,
            Pet::class.java
        )
    }

    suspend fun addPet(userId: String, pet: Pet): Result<String> {
        val petData = pet.copy(
            ownerId = userId,
            createdAt = firestoreHelper.getServerTimestamp(),
            updatedAt = firestoreHelper.getServerTimestamp()
        )

        return firestoreHelper.createSubcollectionDocument(
            COLLECTION_USERS,
            userId,
            COLLECTION_PETS,
            petData
        )
    }

    suspend fun updatePet(userId: String, petId: String, updates: Map<String, Any>): Result<Unit> {
        val docRef = firestoreHelper.getDocumentReference(COLLECTION_USERS, userId)
            .collection(COLLECTION_PETS)
            .document(petId)

        return firestoreHelper.updateDocument(
            "$COLLECTION_USERS/$userId/$COLLECTION_PETS",
            petId,
            updates
        )
    }

    suspend fun deletePet(userId: String, petId: String): Result<Unit> {
        val docRef = firestoreHelper.getDocumentReference(COLLECTION_USERS, userId)
            .collection(COLLECTION_PETS)
            .document(petId)

        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPetImage(userId: String, petId: String, imageUri: Uri): Result<String> {
        return firebaseService.uploadPetImage(userId, petId, imageUri)
    }
}