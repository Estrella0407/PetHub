package com.example.pethub.data.repository

import com.example.pethub.data.local.database.dao.AppointmentDao
import com.example.pethub.data.model.Appointment
import com.example.pethub.data.model.AppointmentItem
import com.example.pethub.data.model.Customer
import com.example.pethub.data.model.Pet
import com.example.pethub.data.model.Service
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_APPOINTMENT
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_BRANCH
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_CUSTOMER
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_PET
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_SERVICE
import com.example.pethub.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

@Singleton
class AppointmentRepository @Inject constructor(
    private val firestoreHelper: FirestoreHelper,
    private val dao: AppointmentDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val authRepository: AuthRepository,
    private val petRepository: PetRepository,
    private val notificationRepository: NotificationRepository,
) {

    /**
     * Fetches a single appointment by its ID and transforms it into a detailed AppointmentItem.
     * This is the primary function to be used by ViewModels to get all details for a single appointment.
     */
    suspend fun getAppointmentById(appointmentId: String): Result<AppointmentItem?> {
        // Step 1: Get the raw Appointment document from Firestore.
        val appointmentResult = getAppointmentDetail(appointmentId)

        return appointmentResult.fold(
            onSuccess = { appointment ->
                if (appointment != null) {
                    // Step 2: If successful, get the rich AppointmentItem.
                    getAppointmentItem(appointment)
                } else {
                    Result.success(null) // Appointment not found, return null successfully.
                }
            },
            onFailure = { exception ->
                // If fetching the appointment failed, propagate the failure.
                Result.failure(exception)
            }
        )
    }

    suspend fun createAppointment(appointment: Appointment) {
        // The 'appointment' object already has all the required IDs.
        // Simply save it to Firestore.
        firestoreHelper.createDocument(COLLECTION_APPOINTMENT, appointment)
        // Trigger notification
        confirmBooking()
    }

    suspend fun removeAppointment(appointment: Appointment){
        firestoreHelper.deleteDocument(
            collection = COLLECTION_APPOINTMENT,
            documentId = appointment.appointmentId
        )
    }

    suspend fun rescheduleAppointment(appointmentId: String, newDateTime: Timestamp): Result<Unit> {
        return firestoreHelper.updateDocument(
            collection = COLLECTION_APPOINTMENT,
            documentId = appointmentId,
            updates = mapOf("dateTime" to newDateTime)
        )
    }


    suspend fun getAllAppointments(): Result<List<Appointment>> {
        return firestoreHelper.getAllDocuments(
            COLLECTION_APPOINTMENT,
            Appointment::class.java
        )
    }

    suspend fun getAllAppointmentsByBranch(branchId: String?): Result<List<Appointment>> {
        val id = branchId ?: return Result.failure(Exception("Branch ID is required"))
        return firestoreHelper.queryDocuments(
            collection = COLLECTION_APPOINTMENT,
            field = "branchId",
            value = id,
            clazz = Appointment::class.java
        )
    }

    suspend fun getAppointmentDetail(
        appointmentId: String
    ): Result<Appointment?> {
        return firestoreHelper.getDocument(
            collection = COLLECTION_APPOINTMENT,
            documentId = appointmentId,
            clazz = Appointment::class.java
        )
    }

    suspend fun getAppointmentItem (
        appointmentDocument: Appointment,
    ):Result<AppointmentItem?> {
        val appointmentId = appointmentDocument.appointmentId.trim()
        val serviceId = appointmentDocument.serviceId.trim()
        val branchId = appointmentDocument.branchId.trim()
        val petId = appointmentDocument.petId.trim()

        if (appointmentId.isBlank() || serviceId.isBlank() || branchId.isBlank() || petId.isBlank()) {
            return Result.failure(Exception("Appointment contains invalid or missing IDs."))
        }

        val serviceResult = firestoreHelper.getDocumentField(
            collection = COLLECTION_SERVICE,
            documentId = serviceId,
            fieldName = "type",
            String::class.java
        )
        val serviceName = serviceResult.getOrNull() ?: "Unknown Service"

        val unformatDateTime: Timestamp? = appointmentDocument.dateTime
        val date = unformatDateTime?.toDate()
        val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        val dateTime = date?.let { formatter.format(it) } ?: "No Date"

        val locationResult = firestoreHelper.getDocumentField(
            collection = COLLECTION_BRANCH,
            documentId = branchId,
            fieldName = "branchName",
            String::class.java
        )
        val locationName: String = locationResult.getOrNull() ?: "Unknown Location"

        val status = appointmentDocument.status

        // First, get the Pet document using petId
        val getPetResult = firestoreHelper.getDocument(
            collection = COLLECTION_PET,
            documentId = petId,
            clazz = Pet::class.java
        )
        val pet = getPetResult.getOrNull()
            ?: return Result.failure(Exception("Pet not found for ID: $petId"))

        // Then, get the owner using the custId from the fetched Pet object
        if (pet.custId.isBlank()) {
            return Result.failure(Exception("Pet with ID $petId has no associated customer ID."))
        }
        val getOwnerResult = firestoreHelper.getDocument(
            collection = COLLECTION_CUSTOMER,
            documentId = pet.custId.trim(),
            clazz = Customer::class.java
        )
        val owner = getOwnerResult.getOrNull()
            ?: return Result.failure(Exception("Owner not found for ID: ${pet.custId}"))

        return Result.success(
            AppointmentItem(
                id = appointmentId,
                dateTime = dateTime,
                locationName = locationName,
                owner = owner,
                pet = pet,
                status = status,
                serviceName = serviceName
            )
        )
    }


    fun listenToBranchAppointments(branchId: String): Flow<List<Appointment>> {
        return firestoreHelper.listenToCollection(
            COLLECTION_APPOINTMENT,
            Appointment::class.java
        ) { query ->
            query.whereEqualTo("branchId", branchId)
        }
    }


    fun listenToAllAppointments(): Flow<List<Appointment>> {
        return firestoreHelper.listenToCollection(
            COLLECTION_APPOINTMENT,
            Appointment::class.java
        )
    }

    /**
     * Gets top recommended services based on the pet's type and breed.
     * It finds appointments for similar pets, counts the frequency of each service,
     * and returns the most popular ones.
     */
    suspend fun getRecommendedServicesForPet(petType: String, petBreed: String): List<Service> =
        withContext(ioDispatcher) {
            // Step 1: Find all pets that are the same type, and optionally the same breed.
            var similarPetsQuery: Query = firestoreHelper.getFirestoreInstance().collection("pet")
                .whereEqualTo("type", petType)

            // --- THIS IS THE FIX ---
            // If a breed is provided, add it to the query.
            if (petBreed.isNotBlank()) {
                similarPetsQuery = similarPetsQuery.whereEqualTo("breed", petBreed)
            }

            val similarPetsResult = similarPetsQuery.limit(50).get().await()
            val similarPetIds = similarPetsResult.documents.map { it.id }

            if (similarPetIds.isEmpty()) {
                // If no similar pets, return an empty list. The ViewModel will handle the fallback.
                return@withContext emptyList()
            }

            // Step 2: Find all appointments for these similar pets
            val appointmentsQuery = firestoreHelper.getFirestoreInstance().collection("appointment")
                .whereIn("petId", similarPetIds)
                .get()
                .await()

            // Step 3: Count the frequency of each serviceId from the appointments
            val serviceIdCounts = appointmentsQuery.documents
                .mapNotNull { it.getString("serviceId") }
                .groupingBy { it }
                .eachCount()

            if (serviceIdCounts.isEmpty()) {
                return@withContext emptyList()
            }

            // Step 4: Get the top 3 most frequent service IDs
            val topServiceIds = serviceIdCounts.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { it.key }

            if (topServiceIds.isEmpty()) {
                return@withContext emptyList()
            }

            // Step 5: Fetch the full service documents for the top IDs
            val services = firestoreHelper.getFirestoreInstance().collection("service")
                .whereIn(FieldPath.documentId(), topServiceIds)
                .get()
                .await()
                .toObjects(Service::class.java)

            // Return the list of service objects
            return@withContext services
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUpcomingAppointments(limit: Int): Flow<List<Appointment>> {
        val userId = authRepository.getCurrentUserId() ?: return flowOf(emptyList())

        // Called the correct function `listenToUserPets`
        return petRepository.listenToUserPets(userId).flatMapLatest { userPets ->
            val petIds = userPets.map { it.petId }
            if (petIds.isEmpty()) {
                return@flatMapLatest flowOf(emptyList<Appointment>())
            }

            // This part is correct and remains unchanged
            firestoreHelper.listenToCollection(
                collection = COLLECTION_APPOINTMENT,
                clazz = Appointment::class.java
            ) { query ->
                query
                    .whereIn("petId", petIds)
                    .whereGreaterThanOrEqualTo("dateTime", Timestamp.now())
                    .orderBy("dateTime")
                    .limit(limit.toLong())
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllAppointmentsForCurrentUser(): Flow<List<Appointment>> {
        val userId = authRepository.getCurrentUserId() ?: return flowOf(emptyList())

        // MODIFIED: Called the correct function `listenToUserPets`
        return petRepository.listenToUserPets(userId).flatMapLatest { userPets ->
            val petIds = userPets.map { it.petId }
            if (petIds.isEmpty()) {
                return@flatMapLatest flowOf(emptyList<Appointment>())
            }

            // This part is correct and remains unchanged
            firestoreHelper.listenToCollection(
                collection = COLLECTION_APPOINTMENT,
                clazz = Appointment::class.java
            ) { query ->
                query
                    .whereIn("petId", petIds)
                    .orderBy("dateTime", Query.Direction.DESCENDING)
            }
        }
    }

    private suspend fun confirmBooking() {
        val custId = authRepository.getCurrentUserId()
        if (custId != null) {
            notificationRepository.sendNotification(
                custId = custId,
                title = "Appointment Confirmed!",
                message = "Your appointment has been successfully booked.",
                type = "appointment"
            )
        }
    }

}
