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

    suspend fun createAppointment(appointment: Appointment) {
        // Get the pet's details
        val custId = authRepository.getCurrentUserId()?:""
        val petResult = petRepository.getPetById(custId, appointment.petId)
        val pet = petResult.getOrNull()

        // Create the final appointment object with the breed included
        val appointmentToSave = appointment.copy(
            breed = pet?.breed ?: "" // Add the breed here
        )

        // Save the new object to Firestore
        firestoreHelper.createDocument(COLLECTION_APPOINTMENT, appointmentToSave)

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
        val appointmentId = appointmentDocument.appointmentId.ifBlank { "unknown_id" }.trim()
        val serviceId = appointmentDocument.serviceId.trim()
        val branchId = appointmentDocument.branchId.trim()
        val petId = appointmentDocument.petId.trim()

        // 1. Get Service Name (with fallback)
        val serviceName = if (serviceId.isNotEmpty()) {
            val serviceResult = firestoreHelper.getDocument(
                collection = COLLECTION_SERVICE,
                documentId = serviceId,
                clazz = com.example.pethub.data.model.Service::class.java
            )
            val service = serviceResult.getOrNull()
            // Check 'serviceName' instead of 'type' as per the Service model
            service?.serviceName ?: service?.type ?: "Service"
        } else {
            "Service"
        }

        val unformatDateTime: Timestamp? = appointmentDocument.dateTime
        val date = unformatDateTime?.toDate()
        val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        val dateTime = date?.let { formatter.format(it) } ?: "No Date"

        // 2. Get Location Name (with fallback)
        val locationName = if (branchId.isNotEmpty()) {
            val locationResult = firestoreHelper.getDocumentField(
                collection = COLLECTION_BRANCH,
                documentId = branchId,
                fieldName = "branchName",
                String::class.java
            )
            locationResult.getOrNull() ?: "PetHub Branch"
        } else {
            "PetHub Branch"
        }

        val status = appointmentDocument.status

        // 3. Get Pet (with fallback)
        val pet = if (petId.isNotEmpty()) {
            val getPetResult = firestoreHelper.getDocument(
                collection = COLLECTION_PET,
                documentId = petId,
                clazz = Pet::class.java
            )
            getPetResult.getOrNull() ?: Pet(petName = "Unknown Pet")
        } else {
            Pet(petName = "Unknown Pet")
        }

        // 4. Get Owner (with fallback)
        val owner = if (pet.custId.isNotEmpty()) {
            val getOwnerResult = firestoreHelper.getDocument(
                collection = COLLECTION_CUSTOMER,
                documentId = pet.custId.trim(),
                clazz = Customer::class.java
            )
            getOwnerResult.getOrNull() ?: Customer(custName = "Customer")
        } else {
            Customer(custName = "Customer")
        }

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

    fun getUpcomingAppointments(limit: Int): Flow<List<Appointment>> {
        val custId = authRepository.getCurrentUserId() ?: return flowOf(emptyList()) // Return an empty list flow if no user is logged in

        return firestoreHelper.listenToCollection(
            collection = COLLECTION_APPOINTMENT,
            clazz = Appointment::class.java
        ) { query ->
            // Chain multiple query conditions
            query
                .whereEqualTo("custId", custId) // Filter by the current user's ID
                .whereGreaterThanOrEqualTo("dateTime",
                    Timestamp.now()) // Filter for appointments from now onwards
                .orderBy("dateTime") // Order by the soonest appointment first
                .limit(limit.toLong()) // Apply the limit
        }
    }

    fun getAllAppointmentsForCurrentUser(): Flow<List<Appointment>> {
        val custId = authRepository.getCurrentUserId() ?: return flowOf(emptyList())

        return firestoreHelper.listenToCollection(
            collection = COLLECTION_APPOINTMENT,
            clazz = Appointment::class.java
        ) { query ->
            query
                .whereEqualTo("custId", custId)
                .orderBy("dateTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
        }
    }

    fun confirmBooking() {
            CoroutineScope(ioDispatcher).launch {
                // After successfully saving, send a notification
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
    }
