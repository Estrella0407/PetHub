package com.example.pethub.data.repository

import com.example.pethub.data.local.database.dao.ServiceDao
import com.example.pethub.data.model.*
import com.example.pethub.data.remote.CloudinaryService
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_SERVICE
import com.example.pethub.data.remote.FirestoreHelper.Companion.FIELD_CREATED_AT
import com.example.pethub.data.remote.FirestoreHelper.Companion.FIELD_IS_ACTIVE
import com.example.pethub.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// ============================================
// SERVICE REPOSITORY
// ============================================

@Singleton
class ServiceRepository @Inject constructor(
    private val firestoreHelper: FirestoreHelper,
    private val cloudinaryService: CloudinaryService,
    private val dao: ServiceDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getAllServices(): Result<List<Service>> {
        return firestoreHelper.queryWithBuilder(
            COLLECTION_SERVICE,
            Service::class.java
        ) { query ->
            query.whereEqualTo(FIELD_IS_ACTIVE, true)
                .orderBy(FIELD_CREATED_AT)
        }
    }

    fun listenToServices(): Flow<List<Service>> {
        return firestoreHelper.listenToCollection(
            COLLECTION_SERVICE,
            Service::class.java
        ) { query ->
            query.whereEqualTo(FIELD_IS_ACTIVE, true)
        }
    }

    suspend fun getServicesByCategory(category: String): Result<List<Service>> {
        return firestoreHelper.queryWithBuilder(
            COLLECTION_SERVICE,
            Service::class.java
        ) { query ->
            query.whereEqualTo("category", category)
                .whereEqualTo(FIELD_IS_ACTIVE, true)
        }
    }

    suspend fun getServiceById(serviceId: String): Result<Service?> {
        return firestoreHelper.getDocument(
            COLLECTION_SERVICE,
            serviceId,
            Service::class.java
        )
    }

    suspend fun searchServices(searchTerm: String): Result<List<Service>> {
        // Note: Firestore doesn't support full-text search
        // This is a basic implementation - consider using Algolia or ElasticSearch for production
        return firestoreHelper.getAllDocuments(COLLECTION_SERVICE, Service::class.java)
            .map { services ->
                services.filter { service ->
                    service.serviceName.contains(searchTerm, ignoreCase = true) ||
                            service.description.contains(searchTerm, ignoreCase = true)
                }
            }
    }
}
