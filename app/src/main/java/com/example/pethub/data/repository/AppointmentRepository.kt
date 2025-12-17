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
        val userId = authRepository.getCurrentUserId()?:""
        val petResult = petRepository.getPetById(userId, appointment.petId)
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


    suspend fun getAllAppointments(): Result<List<Appointment>> {
        return firestoreHelper.getAllDocuments(
            COLLECTION_APPOINTMENT,
            Appointment::class.java
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
        val id = appointmentDocument.appointmentId
        val serviceResult = firestoreHelper.getDocumentField(
            collection = COLLECTION_SERVICE,
            documentId = appointmentDocument.serviceId,
            fieldName = "type",
            String::class.java
        )
        val serviceName = serviceResult.getOrElse {
            return Result.failure(Exception("Service name not found"))
        }
        val unformatDateTime: Timestamp? = appointmentDocument.dateTime
        val date = unformatDateTime?.toDate()
        val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        val dateTime = date?.let { formatter.format(it) } ?: "dateTime failed"
        val locationResult = firestoreHelper.getDocumentField(
            collection = COLLECTION_BRANCH,
            documentId = appointmentDocument.branchId,
            fieldName = "branchName",
            String::class.java
        )

        val locationName: String = locationResult.getOrNull() ?: "Locaton failed"
        val status = appointmentDocument.status
        val getPetResult = firestoreHelper.getDocument(
            collection = COLLECTION_PET,
            documentId = appointmentDocument.petId,
            clazz = Pet::class.java
        )
        val pet = getPetResult.getOrNull()
            ?: return Result.failure(Exception("Pet not found"))
        val getOwnerResult = firestoreHelper.getDocument(
            collection = COLLECTION_CUSTOMER,
            documentId = pet.custId,
            clazz = Customer::class.java
        )
        val owner = getOwnerResult.getOrNull()
            ?: return Result.failure(Exception("Owner not found"))
        return Result.success(
            AppointmentItem(
                id = id,
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
        val userId = authRepository.getCurrentUserId() ?: return flowOf(emptyList()) // Return an empty list flow if no user is logged in

        return firestoreHelper.listenToCollection(
            collection = COLLECTION_APPOINTMENT,
            clazz = Appointment::class.java
        ) { query ->
            // Chain multiple query conditions
            query
                .whereEqualTo("custId", userId) // Filter by the current user's ID
                .whereGreaterThanOrEqualTo("dateTime",
                    Timestamp.now()) // Filter for appointments from now onwards
                .orderBy("dateTime") // Order by the soonest appointment first
                .limit(limit.toLong()) // Apply the limit
        }
    }

        fun confirmBooking() {
            CoroutineScope(ioDispatcher).launch {
                // After successfully saving, send a notification
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    notificationRepository.sendNotification(
                        userId = userId,
                        title = "Appointment Confirmed!",
                        message = "Your appointment has been successfully booked.",
                        type = "appointment"
                    )
                }
            }
        }
    }
