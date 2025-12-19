package com.example.pethub.data.repository

import com.example.pethub.data.local.database.dao.ServiceDao
import com.example.pethub.data.local.database.entity.BranchServiceEntity
import com.example.pethub.data.local.database.entity.ServiceEntity
import com.example.pethub.data.model.BranchService
import com.example.pethub.data.model.Service
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_BRANCH
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_SERVICE
import com.example.pethub.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
        const val SUBCOLLECTION_BRANCH_SERVICES = "branchService"
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

    fun listenToAllBranchServices(branchId: String): Flow<List<BranchService>> {
        return firestoreHelper.listenToSubcollection(
            parentCollection = COLLECTION_BRANCH,
            parentId = branchId,
            subcollection = SUBCOLLECTION_BRANCH_SERVICES,
            clazz = BranchService::class.java
        )
    }

    fun listenToBranchServiceAvailability(branchId: String, serviceId: String): Flow<BranchService?> {
        return firestoreHelper.listenToDocument(
            collection = "$COLLECTION_BRANCH/$branchId/$SUBCOLLECTION_BRANCH_SERVICES",
            documentId = serviceId,
            clazz = BranchService::class.java
        )
    }

    suspend fun setBranchServiceAvailability(branchId: String, serviceId: String, isAvailable: Boolean): Result<Unit> {
        val branchService = BranchService(
            branchId = branchId,
            serviceId = serviceId,
            availability = isAvailable
        )
        return firestoreHelper.setDocument(
            collection = "$COLLECTION_BRANCH/$branchId/$SUBCOLLECTION_BRANCH_SERVICES",
            documentId = serviceId,
            data = branchService,
            merge = true
        )
    }

    // =========================================================================
    // DATA SYNC (Firestore -> Room) - Kept for other potential uses
    // =========================================================================

    suspend fun syncServicesForBranch(branchId: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            val allServicesResult = firestoreHelper.getAllDocuments(COLLECTION_SERVICE, Service::class.java)
            val allServices = allServicesResult.getOrThrow()

            val branchSettingsResult = firestoreHelper.getSubcollectionDocuments(
                parentCollection = COLLECTION_BRANCH,
                parentId = branchId,
                subcollection = SUBCOLLECTION_BRANCH_SERVICES,
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
