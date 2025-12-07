package com.example.pethub.data.repository

import com.example.pethub.data.local.database.dao.BranchDao
import com.example.pethub.data.model.Branch
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_BRANCH
import com.example.pethub.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
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

}
