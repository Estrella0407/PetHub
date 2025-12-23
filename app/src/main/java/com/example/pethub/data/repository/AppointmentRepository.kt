package com.example.pethub.data.repository

import com.example.pethub.data.local.database.dao.AppointmentDao
import com.example.pethub.data.model.Appointment
import com.example.pethub.data.model.AppointmentItem
import com.example.pethub.data.model.Customer
import com.example.pethub.data.model.Pet
import com.example.pethub.data.model.Service // <-- Added missing import
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_APPOINTMENT
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_BRANCH
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_CUSTOMER
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_PET
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_SERVICE
import com.example.pethub.di.IoDispatcher
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath // <-- Added missing import
import com.google.firebase.firestore.Query // <-- Added missing import
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await // <-- Replaced the old await() with the correct one
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull

@Singleton
class AppointmentRepository @Inject constructor(
    private val firestoreHelper: FirestoreHelper,
    private val dao: AppointmentDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val authRepository: AuthRepository,
    private val petRepository: PetRepository,
    private val serviceRepository: ServiceRepository,
    private val notificationRepository: NotificationRepository,
) {

    /**
     * Creates a new appointment document in Firestore, ensuring the document's ID
     * is also saved as the 'appointmentId' field within the document.
     */
    suspend fun createAppointment(appointment: Appointment) {
        val newAppointmentRef = firestoreHelper.getFirestoreInstance()
            .collection(COLLECTION_APPOINTMENT).document()

        val appointmentWithId = appointment.copy(appointmentId = newAppointmentRef.id)

        newAppointmentRef.set(appointmentWithId).await()

        val custId = authRepository.getCurrentUserId()
        if (custId != null) {
            try {
                // Fetch related details for a richer notification message
                val serviceTypeResult = firestoreHelper.getDocumentField(
                    collection = COLLECTION_SERVICE,
                    documentId = appointmentWithId.serviceId,
                    fieldName = "type",
                    String::class.java
                )
                val serviceType = serviceTypeResult.getOrNull() ?: "a service"
                val pet = petRepository.getPetById(custId, appointmentWithId.petId).getOrNull()
                val formattedDate = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                    .format(appointmentWithId.dateTime!!.toDate()) // Safe to use !! as dateTime is mandatory for creation

                val message = "Your appointment for ${pet?.petName ?: "your pet"} ($serviceType) on $formattedDate has been booked."

                notificationRepository.sendNotification(
                    custId = custId,
                    title = "Appointment Confirmed!",
                    message = message, // Use the new detailed message
                    type = "appointment",
                    data = mapOf("appointmentId" to appointmentWithId.appointmentId)
                )
            } catch (e: Exception) {
                println("Failed to send detailed creation notification: ${e.message}")
                // Fallback to a generic notification if details can't be fetched
                notificationRepository.sendNotification(
                    custId = custId,
                    title = "Appointment Confirmed!",
                    message = "Your appointment has been successfully booked.",
                    type = "appointment",
                    data = mapOf("appointmentId" to appointmentWithId.appointmentId)
                )
            }
        }
    }

    suspend fun removeAppointment(appointment: Appointment) {
        // Delete the appointment first
        firestoreHelper.deleteDocument(
            collection = COLLECTION_APPOINTMENT,
            documentId = appointment.appointmentId
        )

        try {
            // Get the pet document to find the customer ID
            val petResult = firestoreHelper.getDocument(
                collection = COLLECTION_PET,
                documentId = appointment.petId,
                clazz = Pet::class.java
            )
            val pet = petResult.getOrNull()

            // Get custId from the pet, NOT from authRepository
            val custId = pet?.custId

            if (!custId.isNullOrBlank()) {
                // Fetch service type for detailed message
                val serviceTypeResult = firestoreHelper.getDocumentField(
                    collection = COLLECTION_SERVICE,
                    documentId = appointment.serviceId,
                    fieldName = "type",
                    String::class.java
                )
                val serviceType = serviceTypeResult.getOrNull() ?: "a service"

                val formattedDate = appointment.dateTime?.let {
                    SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()).format(it.toDate())
                } ?: "a recent date"

                val message = "Your appointment for ${pet.petName} ($serviceType) on $formattedDate has been canceled."

                notificationRepository.sendNotification(
                    custId = custId,  // Use the customer's ID from the pet, not the admin's
                    title = "Appointment Canceled",
                    message = message,
                    type = "appointment",
                    data = mapOf("appointmentId" to appointment.appointmentId)
                )

                println("✅ Cancellation notification sent to customer: $custId")
            } else {
                println("❌ Could not find customer ID for pet: ${appointment.petId}")
            }
        } catch (e: Exception) {
            println("❌ Failed to send cancellation notification: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun rescheduleAppointment(appointmentId: String, newDateTime: Timestamp): Result<Unit> {
        val result = firestoreHelper.updateDocument(
            collection = COLLECTION_APPOINTMENT,
            documentId = appointmentId,
            updates = mapOf("dateTime" to newDateTime)
        )

        if (result.isSuccess) {
            try {
                // Get the appointment details first
                val appointment = getAppointmentDetail(appointmentId).getOrNull()

                if (appointment != null) {
                    val petId = appointment.petId
                    val serviceId = appointment.serviceId

                    // Get the pet document to find the customer ID
                    val petResult = firestoreHelper.getDocument(
                        collection = COLLECTION_PET,
                        documentId = petId,
                        clazz = Pet::class.java
                    )
                    val pet = petResult.getOrNull()

                    // Get custId from the pet, NOT from authRepository
                    val custId = pet?.custId

                    if (!custId.isNullOrBlank()) {
                        // Fetch service type
                        val serviceTypeResult = firestoreHelper.getDocumentField(
                            collection = COLLECTION_SERVICE,
                            documentId = serviceId,
                            fieldName = "type",
                            String::class.java
                        )
                        val serviceType = serviceTypeResult.getOrNull() ?: "a service"

                        val formattedNewDate = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                            .format(newDateTime.toDate())

                        val message = "Your appointment for ${pet.petName} ($serviceType) has been rescheduled to $formattedNewDate."

                        notificationRepository.sendNotification(
                            custId = custId,  // Use the customer's ID from the pet, not the admin's
                            title = "Appointment Rescheduled!",
                            message = message,
                            type = "appointment",
                            data = mapOf("appointmentId" to appointmentId)
                        )

                        println("✅ Reschedule notification sent to customer: $custId")
                    } else {
                        println("❌ Could not find customer ID for pet: $petId")
                    }
                } else {
                    println("❌ Appointment not found: $appointmentId")
                }
            } catch (e: Exception) {
                println("❌ Failed to send reschedule notification: ${e.message}")
                e.printStackTrace()  // Add this to see the full error in logs
            }
        } else {
            println("❌ Failed to update appointment in Firestore")
        }
        return result
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

        val getPetResult = firestoreHelper.getDocument(
            collection = COLLECTION_PET,
            documentId = petId,
            clazz = Pet::class.java
        )
        val pet = getPetResult.getOrNull()
            ?: return Result.failure(Exception("Pet not found for ID: $petId"))

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
            var similarPetsQuery: Query = firestoreHelper.getFirestoreInstance().collection("pet")
                .whereEqualTo("type", petType)

            if (petBreed.isNotBlank()) {
                similarPetsQuery = similarPetsQuery.whereEqualTo("breed", petBreed)
            }

            // Use the correct await() here as well
            val similarPetsResult = similarPetsQuery.limit(30).get().await()
            val similarPetIds = similarPetsResult.documents.map { it.id }

            if (similarPetIds.isEmpty()) {
                return@withContext emptyList()
            }

            val appointmentsQuery = firestoreHelper.getFirestoreInstance().collection("appointment")
                .whereIn("petId", similarPetIds)
                .get()
                .await() // And here

            val serviceIdCounts = appointmentsQuery.documents
                .mapNotNull { it.getString("serviceId") }
                .groupingBy { it }
                .eachCount()

            if (serviceIdCounts.isEmpty()) {
                return@withContext emptyList()
            }

            val topServiceIds = serviceIdCounts.entries
                .sortedByDescending { it.value }
                .take(10)
                .map { it.key }

            if (topServiceIds.isEmpty()) {
                return@withContext emptyList()
            }

            val services = firestoreHelper.getFirestoreInstance().collection("service")
                .whereIn(FieldPath.documentId(), topServiceIds)
                .get()
                .await() // And here
                .toObjects(Service::class.java)

            // This function needs to be defined somewhere, assuming it exists
            // fun isServiceSuitableForPet(serviceType: String, petType: String): Boolean { ... }
            return@withContext services.filter { service ->
                isServiceSuitableForPet(service.type, petType)
            }.take(3)
        }

    // A placeholder for the missing function. You should implement its logic.
    private fun isServiceSuitableForPet(serviceType: String, petType: String): Boolean {
        // Example Logic: "All" is suitable for any pet. Otherwise, types must match.
        return serviceType.equals("All", ignoreCase = true) || serviceType.equals(petType, ignoreCase = true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUpcomingAppointments(limit: Int): Flow<List<Appointment>> {
        val userId = authRepository.getCurrentUserId() ?: return flowOf(emptyList())

        return petRepository.listenToUserPets(userId).flatMapLatest { userPets ->
            val petIds = userPets.map { it.petId }
            if (petIds.isEmpty()) {
                return@flatMapLatest flowOf(emptyList<Appointment>())
            }

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

        return petRepository.listenToUserPets(userId).flatMapLatest { userPets ->
            val petIds = userPets.map { it.petId }
            if (petIds.isEmpty()) {
                return@flatMapLatest flowOf(emptyList<Appointment>())
            }

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
}
