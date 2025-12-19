package com.example.pethub.data.repository

import androidx.compose.ui.geometry.isEmpty
import com.example.pethub.data.local.database.dao.BranchDao
import com.example.pethub.data.model.Branch
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_BRANCH
import com.example.pethub.di.IoDispatcher
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BranchRepository @Inject constructor(
    private val firestoreHelper: FirestoreHelper,
    private val dao: BranchDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun listenToBranches(): Flow<List<Branch>> {
        return firestoreHelper.listenToCollection(
            COLLECTION_BRANCH,
            Branch::class.java
        )
    }

    suspend fun getAllBranches(): Result<List<Branch>> {
        return firestoreHelper.getAllDocuments(
            COLLECTION_BRANCH,
            Branch::class.java
        )
    }

    suspend fun getBranchById(branchId: String): Result<Branch?> {
        return firestoreHelper.getDocument(
            COLLECTION_BRANCH,
            branchId,
            Branch::class.java
        )
    }

    /**
     * Fetches branches that have a specific service available.
     * It queries each branch's 'branch_services' subcollection to find which branches
     * have the service enabled.
     * @param serviceName The service category name (e.g., "Grooming", "Boarding")
     */
    suspend fun getAvailableBranchesForService(
        serviceName: String
    ): Result<List<Branch>> = withContext(ioDispatcher) {
        try {
            val firestore = firestoreHelper.getFirestoreInstance()
            
            // Convert serviceName to lowercase to match document IDs in branch_services
            val serviceDocId = serviceName.lowercase()

            // Step 1: Get all branches
            val allBranches = firestore
                .collection(COLLECTION_BRANCH)
                .get()
                .await()
                .toObjects(Branch::class.java)

            // Step 2: For each branch, check if the service is available in their subcollection
            val availableBranches = allBranches.filter { branch ->
                try {
                    val serviceDoc = firestore
                        .collection("branch")
                        .document(branch.branchId)
                        .collection("branch_services")
                        .document(serviceDocId)
                        .get()
                        .await()

                    // Check if the document exists and availability is true
                    serviceDoc.exists() && serviceDoc.getBoolean("availability") == true
                } catch (e: Exception) {
                    // If there's an error checking this branch, exclude it
                    false
                }
            }

            Result.success(availableBranches)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
