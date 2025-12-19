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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

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

    // This function is now simplified because the appointmentId is a field.
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

    // This function also becomes simpler.
    suspend fun getAppointmentDetail(
        appointmentId: String
    ): Result<Appointment?> {
        val snapshot = firestoreHelper.getFirestoreInstance()
            .collection(COLLECTION_APPOINTMENT)
            .document(appointmentId)
            .get()
            .await()
        return Result.success(snapshot.toObject<Appointment>())
    }

    suspend fun getAppointmentItem (
        appointmentDocument: Appointment,
    ):Result<AppointmentItem?> {
        // This logic remains correct because it assumes appointmentId is already populated.
        val appointmentId = appointmentDocument.appointmentId.trim()
        val serviceId = appointmentDocument.serviceId.trim()
        val branchId = appointmentDocument.branchId.trim()
        val petId = appointmentDocument.petId.trim()

        val serviceResult = firestoreHelper.getDocumentField(
            collection = COLLECTION_SERVICE,
            documentId = serviceId,
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
            documentId = branchId,
            fieldName = "branchName",
            String::class.java
        )
        val locationName: String = locationResult.getOrNull() ?: "Locaton failed"

        val status = appointmentDocument.status

        val getPetResult = firestoreHelper.getDocument(
            collection = COLLECTION_PET,
            documentId = petId,
            clazz = Pet::class.java
        )
        val pet = getPetResult.getOrNull()
            ?: return Result.failure(Exception("Pet not found"))

        val getOwnerResult = firestoreHelper.getDocument(
            collection = COLLECTION_CUSTOMER,
            documentId = pet.custId.trim(),
            clazz = Customer::class.java
        )
        val owner = getOwnerResult.getOrNull()
            ?: return Result.failure(Exception("Owner not found"))

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
        val custId = authRepository.getCurrentUserId() ?: return flowOf(emptyList())

        return firestoreHelper.listenToCollection(
            collection = COLLECTION_APPOINTMENT,
            clazz = Appointment::class.java
        ) { query ->
            query
                .whereEqualTo("custId", custId)
                .whereGreaterThanOrEqualTo("dateTime", Timestamp.now())
                .orderBy("dateTime")
                .limit(limit.toLong())
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
}
