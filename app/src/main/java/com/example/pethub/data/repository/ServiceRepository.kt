package com.example.pethub.data.repository

import com.example.pethub.data.local.database.dao.ServiceDao
import com.example.pethub.data.local.database.entity.BranchServiceEntity
import com.example.pethub.data.local.database.entity.ServiceEntity
import com.example.pethub.data.model.BranchService
import com.example.pethub.data.model.Service
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_SERVICE
import com.example.pethub.di.IoDispatcher
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor(
    private val firestoreHelper: FirestoreHelper,
    private val serviceDao: ServiceDao,
    private val petRepository: PetRepository,
    private val appointmentRepository: AppointmentRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    companion object {
        const val COLLECTION_BRANCH_SERVICE = "branchService" // Updated collection name
    }

    // =========================================================================
    // DATA ACCESS (Firestore -> UI)
    // =========================================================================

    fun listenToServices(): Flow<List<Service>> {
        return firestoreHelper.listenToCollection(
            COLLECTION_SERVICE,
            Service::class.java
        )
    }

    /**
     * Listens to all service settings for a specific branch from the subcollection.
     */
    fun listenToAllBranchServices(branchId: String): Flow<List<BranchService>> {
        return firestoreHelper.listenToSubcollection(
            parentCollection = "branch",
            parentId = branchId,
            subcollection = "branch_services",
            clazz = BranchService::class.java
        )
    }

    /**
     * Listens for changes to a specific service within a specific branch.
     * Generates a composite ID to find the document.
     */
    fun listenToBranchServiceAvailability(branchId: String, serviceId: String): Flow<BranchService?> {
        val documentId = "$branchId-$serviceId" // Composite ID
        return firestoreHelper.listenToDocument(
            collection = COLLECTION_BRANCH_SERVICE,
            documentId = documentId,
            clazz = BranchService::class.java
        )
    }

    /**
     * Sets the availability of a service for a branch in the 'branch_services' subcollection.
     */
    suspend fun setBranchServiceAvailability(
        branchId: String,
        serviceId: String,
        serviceName: String,
        isAvailable: Boolean
    ): Result<Unit> = withContext(ioDispatcher) {
            try {
                val branchService = mapOf(
                    "branchId" to branchId,
                    "serviceId" to serviceId,
                    "serviceName" to serviceName,
                    "availability" to isAvailable
                )

                firestoreHelper.getFirestoreInstance()
                    .collection("branch")
                    .document(branchId)
                    .collection("branch_services")
                    .document(serviceId)
                    .set(branchService, SetOptions.merge())
                    .await()

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    // =========================================================================
    // DATA SYNC (Firestore -> Room) - Kept for other potential uses
    // =========================================================================

    suspend fun syncServicesForBranch(branchId: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            val allServicesResult = firestoreHelper.getAllDocuments(COLLECTION_SERVICE, Service::class.java)
            val allServices = allServicesResult.getOrThrow()

            // Query the top-level 'branchService' collection for the specific branch
            val branchSettingsResult = firestoreHelper.queryDocuments(
                collection = COLLECTION_BRANCH_SERVICE,
                field = "branchId",
                value = branchId,
                clazz = BranchService::class.java
            )
            val branchSettings = branchSettingsResult.getOrThrow()

            val serviceEntities = allServices.map { it.toEntity() }

            val branchServiceEntities = branchSettings.map { setting ->
                BranchServiceEntity(
                    branchId = setting.branchId,
                    serviceId = setting.serviceId,
                    availability = setting.availability
                )
            }

            serviceDao.insertServices(serviceEntities)
            serviceDao.insertBranchServices(branchServiceEntities)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // DATA ACCESS (Room -> UI) - Kept for other potential uses
    // =========================================================================

    fun getServicesForBranch(branchId: String): Flow<List<Service>> {
        return serviceDao.getServicesForBranch(branchId).map { list ->
            list.map { item ->
                Service(
                    serviceId = item.service.serviceId,
                    type = item.service.type,
                    description = item.service.description,
                    price = item.service.price,
                    serviceName = item.service.serviceName ?: "",
                    imageUrl = item.service.imageUrl ?: ""
                )
            }
        }
    }

    suspend fun loadRecommendedServices(petId: String): Result<List<Service>> {
        return try {
            val result = firestoreHelper.getAllDocuments(COLLECTION_SERVICE, Service::class.java)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // FUNCTIONS FOR SERVICE DETAIL SCREEN
    // =========================================================================

    /**
     * Fetches a single service by its ID directly from Firestore.
     * Used by ServiceDetailViewModel to get the main service's details.
     */
    suspend fun getServiceById(serviceId: String): Result<Service?> = withContext(ioDispatcher) {
        firestoreHelper.getDocument(
            collection = COLLECTION_SERVICE,
            documentId = serviceId,
            clazz = Service::class.java
        )
    }

    /**
     * Fetches all services belonging to a specific category from Firestore.
     * Used by ServiceDetailViewModel to populate the 'Service Type' dropdown.
     */
    suspend fun getServicesByCategory(serviceName: String): Result<List<Service>> = withContext(ioDispatcher) {
        if (serviceName.isBlank()) {
            return@withContext Result.success(emptyList())
        }
        firestoreHelper.queryDocuments(
            collection = COLLECTION_SERVICE,
            field = "serviceName",
            value = serviceName,
            clazz = Service::class.java
        )
    }


    // =========================================================================
    // MAPPERS (Helper functions)
    // =========================================================================

    private fun Service.toEntity(): ServiceEntity {
        return ServiceEntity(
            serviceId = this.serviceId,
            type = this.type,
            serviceName = this.serviceName,
            description = this.description,
            price = this.price,
            imageUrl = this.imageUrl
        )
    }
}