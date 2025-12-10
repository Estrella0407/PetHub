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
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull

// ============================================
// SERVICE REPOSITORY
// ============================================

@Singleton
class ServiceRepository @Inject constructor(
    private val firestoreHelper: FirestoreHelper,
    private val cloudinaryService: CloudinaryService,
    private val dao: ServiceDao,
    private val petRepository: PetRepository,
    private val appointmentRepository: AppointmentRepository,
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

    suspend fun loadRecommendedServices(petId: String): Result<List<Service>> =
        withContext(ioDispatcher) {
        try {
            // Get the current pet's breed
            val petResult = petRepository.getPetById(petId)
            val petBreed = petResult.getOrNull()?.breed ?: return@withContext Result.failure(Exception("Pet not found"))

            // Get all historical bookings
            val allAppointmentsResult = appointmentRepository.getAllAppointments()
            if (allAppointmentsResult.isFailure) {
                return@withContext Result.failure(allAppointmentsResult.exceptionOrNull() ?: Exception("Failed to get bookings"))
            }
            val allAppointments = allAppointmentsResult.getOrThrow()

            // Find the most popular service ID for that breed
            val mostPopularServiceId = allAppointments
                .filter { it.breed == petBreed }    // Filter bookings by the same breed
                .groupBy { it.serviceId }           // Group by service ID
                .maxByOrNull { it.value.size }      // Find the group with the most bookings
                ?.key                               // Get the service ID

            // Fetch all services and prioritize the recommended one
            val allServicesResult = getAllServices()
            if (allServicesResult.isFailure) {
                return@withContext allServicesResult // Return the failure result
            }

            val allServices = allServicesResult.getOrThrow()

            if (mostPopularServiceId != null) {
                // If a popular service was found, move it to the top of the list
                val recommendedList = allServices.sortedByDescending { it.serviceId == mostPopularServiceId }
                Result.success(recommendedList)
            } else {
                // If no booking data exists for this breed, return all services as-is
                Result.success(allServices)
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
