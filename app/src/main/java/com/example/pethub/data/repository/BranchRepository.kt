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
     * It queries the top-level 'branchService' collection first to find available branch IDs,
     * then fetches the details for those specific branches.
     */
    suspend fun getAvailableBranchesForService(
        serviceId: String
    ): Result<List<Branch>> = withContext(ioDispatcher) {
        try {
            val firestore = firestoreHelper.getFirestoreInstance()

            val availableBranchServices = firestore
                .collection("branchService")
                .whereEqualTo("serviceId", serviceId)
                .whereEqualTo("availability", true)
                .get()
                .await()

            val branchIds =
                availableBranchServices.documents.mapNotNull {
                    it.getString("branchId")
                }

            if (branchIds.isEmpty()) {
                // No branches offer this service, return an empty list.
                return@withContext Result.success(emptyList())
            }

            // Now that we have the IDs, fetch the full branch documents.
            val branches = firestoreHelper.getFirestoreInstance()
                .collection(COLLECTION_BRANCH)
                .whereIn(FieldPath.documentId(), branchIds)
                .get()
                .await()
                .toObjects(Branch::class.java)

            Result.success(branches)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
