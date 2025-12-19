package com.example.pethub.data.repository

import android.util.Log
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
     * Fetches all branches that offer a specific service and have it marked as available.
     * This uses a collection group query on the 'branch_services' subcollection.
     */
    suspend fun getBranchesOfferingService(serviceId: String): Result<List<Branch>> = withContext(ioDispatcher) {
        Log.d("BranchRepoDebug", "Searching for branches offering serviceId: $serviceId")
        if (serviceId.isBlank()) {
            Log.w("BranchRepoDebug", "serviceId is blank, returning empty list.")
            return@withContext Result.success(emptyList())
        }

        try {
            val branchServiceDocs = firestoreHelper.getFirestoreInstance()
                .collectionGroup("branchService")
                .whereEqualTo("serviceId", serviceId)
                .whereEqualTo("availability", true)
                .get()
                .await()
                .documents

            Log.d("BranchRepoDebug", "Found ${branchServiceDocs.size} branchService documents.")

            val branchIds = branchServiceDocs
                .mapNotNull { it.getString("branchId") }
                .distinct()

            Log.d("BranchRepoDebug", "Extracted distinct branch IDs: $branchIds")

            if (branchIds.isEmpty()) {
                Log.w("BranchRepoDebug", "No branch IDs found. No branches offer this service or it's unavailable.")
                return@withContext Result.success(emptyList())
            }

            val branches = firestoreHelper.getFirestoreInstance()
                .collection(COLLECTION_BRANCH)
                .whereIn(FieldPath.documentId(), branchIds)
                .get()
                .await()
                .toObjects(Branch::class.java)

            Log.d("BranchRepoDebug", "Successfully fetched ${branches.size} full branch documents.")
            Result.success(branches)

        } catch (e: Exception) {
            Log.e("BranchRepoDebug", "Error fetching branches offering service", e)
            Result.failure(e)
        }
    }
}
