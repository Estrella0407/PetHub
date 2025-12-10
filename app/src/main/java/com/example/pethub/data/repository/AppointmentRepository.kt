package com.example.pethub.data.repository

import com.example.pethub.data.local.database.dao.AppointmentDao
import com.example.pethub.data.model.Appointment
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.remote.FirestoreHelper.Companion.COLLECTION_APPOINTMENT
import com.example.pethub.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull
import com.google.firebase.Timestamp

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
        val petResult = petRepository.getPetById(appointment.petId)
        val pet = petResult.getOrNull()

        // Create the final appointment object with the breed included
        val appointmentToSave = appointment.copy(
            breed = pet?.breed ?: "" // Add the breed here
        )

        // Save the new object to Firestore
        firestoreHelper.createDocument(COLLECTION_APPOINTMENT, appointmentToSave)
    }

    suspend fun getAllAppointments(): Result<List<Appointment>> {
        return firestoreHelper.getAllDocuments(
            COLLECTION_APPOINTMENT,
            Appointment::class.java
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
        val userId = authRepository.getCurrentUserId() ?: return emptyFlow() // Return an empty flow if no user is logged in

        return firestoreHelper.listenToCollection(
            collection = COLLECTION_APPOINTMENT,
            clazz = Appointment::class.java
        ) { query ->
            // Chain multiple query conditions
            query
                .whereEqualTo("userId", userId) // Filter by the current user's ID
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
                    message = "Your appointment for Grooming on May 25th is confirmed.",
                    type = "appointment"
                )
            }
        }
    }
}
