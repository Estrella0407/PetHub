package com.example.pethub.di

import com.example.pethub.data.local.database.dao.AppointmentDao
import com.example.pethub.data.local.database.dao.BranchDao
import com.example.pethub.data.local.database.dao.CustomerDao
import com.example.pethub.data.local.database.dao.PetDao
import com.example.pethub.data.local.database.dao.ServiceDao
import com.example.pethub.data.local.prefs.PreferenceManager
import com.example.pethub.data.remote.CloudinaryService
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

/**
 * Hilt module for providing Repository instances
 * Repositories handle data operations and business logic
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provide AuthRepository for authentication operations
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseService: FirebaseService,
        firestoreHelper: FirestoreHelper,
        preferenceManager: PreferenceManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): AuthRepository {
        return AuthRepository(
            firebaseService,
            firestoreHelper,
            preferenceManager,
            ioDispatcher
        )
    }

    /**
     * Provide CustomerRepository for customer profile operations
     */
    @Provides
    @Singleton
    fun provideCustomerRepository(
        firebaseService: FirebaseService,
        firestoreHelper: FirestoreHelper,
        customerDao: CustomerDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): CustomerRepository {
        return CustomerRepository(
            firebaseService,
            firestoreHelper,
            customerDao,
            ioDispatcher
        )
    }

    /**
     * Provide BranchRepository for branch operations
     */
    @Provides
    @Singleton
    fun provideBranchRepository(
        firestoreHelper: FirestoreHelper,
        branchDao: BranchDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): BranchRepository {
        return BranchRepository(
            firestoreHelper,
            branchDao,
            ioDispatcher
        )
    }

    /**
     * Provide PetRepository for pet management operations
     */
    @Provides
    @Singleton
    fun providePetRepository(
        firebaseService: FirebaseService,
        firestoreHelper: FirestoreHelper,
        cloudinaryService: CloudinaryService,
        petDao: PetDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): PetRepository {
        return PetRepository(
            firebaseService,
            firestoreHelper,
            cloudinaryService,
            petDao,
            ioDispatcher
        )
    }

    /**
     * Provide ServiceRepository for service browsing operations
     */
    @Provides
    @Singleton
    fun provideServiceRepository(
        firestoreHelper: FirestoreHelper,
        cloudinaryService: CloudinaryService,
        serviceDao: ServiceDao,
        petRepository: PetRepository,
        appointmentRepository: AppointmentRepository,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): ServiceRepository {
        return ServiceRepository(
            firestoreHelper,
            cloudinaryService,
            serviceDao,
            petRepository,
            appointmentRepository,
            ioDispatcher
        )
    }

    /**
     * Provide AppointmentRepository for appointment management operations
     */
    @Provides
    @Singleton
    fun provideAppointmentRepository(
        firestoreHelper: FirestoreHelper,
        appointmentDao: AppointmentDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        notificationRepository: NotificationRepository,
        authRepository: AuthRepository,
        petRepository: PetRepository
    ): AppointmentRepository {
        return AppointmentRepository(
            firestoreHelper,
            appointmentDao,
            ioDispatcher,
            authRepository,
            petRepository,
            notificationRepository
        )
    }
}
