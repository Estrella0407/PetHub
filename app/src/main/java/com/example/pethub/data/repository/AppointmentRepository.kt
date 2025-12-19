package com.example.pethub.data.repository

import com.example.pethub.data.local.database.dao.AppointmentDao
import com.example.pethub.data.model.Appointment
import com.example.pethub.data.model.AppointmentItem
import com.example.pethub.data.model.Customer
import com.example.pethub.data.model.Pet
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_APPOINTMENT
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_BRANCH
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_CUSTOMER
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_PET
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_SERVICE
import com.example.pethub.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull
import com.google.firebase.Timestamp
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
     * Creates a new appointment document in Firestore, ensuring the document's ID
     * is also saved as the 'appointmentId' field within the document.
     */
    suspend fun createAppointment(appointment: Appointment) {
        // 1. Get a reference to a new, empty document in the appointments collection.
        // This gives us the unique, auto-generated ID *before* we save any data.
        val newAppointmentRef = firestoreHelper.getFirestoreInstance()
            .collection(COLLECTION_APPOINTMENT).document()

        // 2. Create a new Appointment object from the one passed by the ViewModel,
        // but this time, we copy the auto-generated ID into the 'appointmentId' field.
        val appointmentWithId = appointment.copy(appointmentId = newAppointmentRef.id)

        // 3. Set the data of the new document reference with our complete object.
        // Instead of .add(), we use .set() on the specific document reference.
        newAppointmentRef.set(appointmentWithId).await()

        // 4. Trigger the notification after the save is successful.
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
            var similarPetsQuery: Query = firestoreHelper.getFirestoreInstance().collection("pet")
                .whereEqualTo("type", petType)

            if (petBreed.isNotBlank()) {
                similarPetsQuery = similarPetsQuery.whereEqualTo("breed", petBreed)
            }

            val similarPetsResult = similarPetsQuery.limit(50).get().await()
            val similarPetIds = similarPetsResult.documents.map { it.id }

            if (similarPetIds.isEmpty()) {
                return@withContext emptyList()
            }

            val appointmentsQuery = firestoreHelper.getFirestoreInstance().collection("appointment")
                .whereIn("petId", similarPetIds)
                .get()
                .await()

            val serviceIdCounts = appointmentsQuery.documents
                .mapNotNull { it.getString("serviceId") }
                .groupingBy { it }
                .eachCount()

            if (serviceIdCounts.isEmpty()) {
                return@withContext emptyList()
            }

            val topServiceIds = serviceIdCounts.entries
                .sortedByDescending { it.value }
                .take(10) // Fetch more to allow for filtering
                .map { it.key }

            if (topServiceIds.isEmpty()) {
                return@withContext emptyList()
            }

            val services = firestoreHelper.getFirestoreInstance().collection("service")
                .whereIn(FieldPath.documentId(), topServiceIds)
                .get()
                .await()
                .toObjects(Service::class.java)

            // Filter the results using the service.type field, not service.serviceName
            return@withContext services.filter { service ->
                isServiceSuitableForPet(service.type, petType)
            }.take(3)
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
}
