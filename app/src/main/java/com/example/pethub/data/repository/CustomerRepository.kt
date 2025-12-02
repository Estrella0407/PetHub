package com.example.pethub.data.repository

import com.example.pethub.data.model.Customer
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_CUSTOMER
import com.example.pethub.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val firebaseService: FirebaseService,
    private val firestoreHelper: FirestoreHelper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getCurrentCustomer(): Result<Customer?> {
        val userId = firebaseService.getCurrentUserId() ?: return Result.success(null)
        return firestoreHelper.getDocument(COLLECTION_CUSTOMER, userId, Customer::class.java)
    }

    fun listenToCurrentCustomer(): Flow<Customer?> {
        val userId = firebaseService.getCurrentUserId() ?: return flowOf(null)
        return firestoreHelper.listenToDocument(COLLECTION_CUSTOMER, userId, Customer::class.java)
    }
    
    suspend fun createCustomer(customer: Customer): Result<Unit> {
        return firestoreHelper.setDocument(COLLECTION_CUSTOMER, customer.custId, customer)
    }
}
