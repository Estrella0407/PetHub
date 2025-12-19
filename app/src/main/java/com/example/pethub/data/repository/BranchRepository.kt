package com.example.pethub.data.repository

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

    fun listenToBranches(): Flow<List<Branch>> = firestoreHelper.listenToCollection(COLLECTION_BRANCH, Branch::class.java)

    suspend fun getAllBranches(): Result<List<Branch>> = firestoreHelper.getAllDocuments(COLLECTION_BRANCH, Branch::class.java)

    suspend fun getBranchById(branchId: String): Result<Branch?> {
        return firestoreHelper.getDocument(
            COLLECTION_BRANCH,
            branchId,
            Branch::class.java
        )
    }

    /**
     * Simpler logic: Fetch ALL branches, then check availability for each directly.
     * This avoids PERMISSION_DENIED on collectionGroup queries.
     * If a branch has no record in branch_services for this category, it's ENABLED by default.
     */
    suspend fun getAvailableBranchesForService(categoryName: String): Result<List<Branch>> = withContext(ioDispatcher) {
        try {
            val allBranches = getAllBranches().getOrThrow()
            val firestore = firestoreHelper.getFirestoreInstance()
            val availableBranches = mutableListOf<Branch>()

            for (branch in allBranches) {
                try {
                    // Check the specific sub-document for this branch and service
                    val doc = firestore.collection("branch")
                        .document(branch.branchId)
                        .collection("branch_services")
                        .document(categoryName.lowercase())
                        .get()
                        .await()

                    // If the document exists, check 'availability' field. 
                    // If it doesn't exist, we assume it's available by default.
                    val isAvailable = if (doc.exists()) {
                        doc.getBoolean("availability") ?: true
                    } else {
                        true
                    }

                    if (isAvailable) {
                        availableBranches.add(branch)
                    }
                } catch (e: Exception) {
                    // Assuming available by default for safety.
                    availableBranches.add(branch)
                }
            }

            Result.success(availableBranches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
