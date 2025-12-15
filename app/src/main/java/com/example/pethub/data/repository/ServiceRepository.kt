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
        const val SUBCOLLECTION_BRANCH_SERVICES = "branch_services"
    }

    // =========================================================================
    // DATA SYNC (Firestore -> Room)
    // =========================================================================

    /**
     * This function fetches data from Firebase and saves it to Room.
     * Call this when the screen loads or when the user pulls-to-refresh.
     */
    suspend fun syncServicesForBranch(branchId: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            // A. Fetch All Global Services from Firestore
            val allServicesResult = firestoreHelper.getAllDocuments(COLLECTION_SERVICE, Service::class.java)
            val allServices = allServicesResult.getOrThrow()

            // B. Fetch Branch Specific Settings from Firestore
            val branchSettingsResult = firestoreHelper.getSubcollectionDocuments(
                parentCollection = COLLECTION_BRANCH,
                parentId = branchId,
                subcollection = SUBCOLLECTION_BRANCH_SERVICES,
                clazz = BranchService::class.java
            )
            val branchSettings = branchSettingsResult.getOrThrow()

            // C. Convert to Room Entities
            val serviceEntities = allServices.map { it.toEntity() }

            val branchServiceEntities = branchSettings.map { setting ->
                BranchServiceEntity(
                    branchId = setting.branchId,
                    serviceId = setting.serviceId,
                    availability = setting.availability
                )
            }

            // D. Save to Room (Single Source of Truth)
            serviceDao.insertServices(serviceEntities)
            serviceDao.insertBranchServices(branchServiceEntities)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // DATA ACCESS (Room -> UI)
    // =========================================================================

    /**
     * Helper to listen to all services (Real-time)
     * Used by HomeViewModel's loadServices()
     */
    fun listenToServices(): Flow<List<Service>> {
        return firestoreHelper.listenToCollection(
            COLLECTION_SERVICE,
            Service::class.java
        )
    }

    /**
     * The UI observes this Flow. It reads purely from Room.
     * This ensures the UI works even if offline.
     */
    fun getServicesForBranch(branchId: String): Flow<List<Service>> {
        return serviceDao.getServicesForBranch(branchId).map { list ->
            list.map { item ->
                // Convert Entity back to Domain Model
                Service(
                    serviceId = item.service.serviceId,
                    serviceName = item.service.serviceName,
                    description = item.service.description,
                    price = item.service.price,
                    type = item.service.type
                )
            }
        }
    }

    /**
     * Load recommended services based on a Pet ID.
     * Currently fetches all services, but can be filtered by pet type/breed in the future.
     */
    suspend fun loadRecommendedServices(petId: String): Result<List<Service>> {
        return try {
            // Fetch all services from Firestore
            val result = firestoreHelper.getAllDocuments(COLLECTION_SERVICE, Service::class.java)

            // Filter logic could go here based on the petId
            // For now, we return the successful result directly
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // MAPPERS (Helper functions)
    // =========================================================================

    private fun Service.toEntity(): ServiceEntity {
        return ServiceEntity(
            serviceId = this.serviceId,
            serviceName = this.serviceName,
            description = this.description,
            price = this.price,
            type = this.type
        )
    }
}
