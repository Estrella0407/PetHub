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
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

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

    @Provides
    @Singleton
    fun providePetRepository(
        firebaseService: FirebaseService,
        firestoreHelper: FirestoreHelper,
        cloudinaryService: CloudinaryService,
        authRepository: AuthRepository,
        petDao: PetDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): PetRepository {
        return PetRepository(
            firebaseService,
            firestoreHelper,
            cloudinaryService,
            authRepository,
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
        serviceDao: ServiceDao,
        petRepository: PetRepository,
        appointmentRepositoryProvider: Provider<AppointmentRepository>,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): ServiceRepository {
        return ServiceRepository(
            firestoreHelper,
            serviceDao,
            petRepository,
            // Pass the provider to the constructor
            appointmentRepositoryProvider,
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
        authRepository: AuthRepository,
        petRepository: PetRepository,
        serviceRepository: ServiceRepository,
        notificationRepository: NotificationRepository
    ): AppointmentRepository {
        return AppointmentRepository(
            firestoreHelper,
            appointmentDao,
            ioDispatcher,
            authRepository,
            petRepository,
            // Pass the direct instance here
            serviceRepository,
            notificationRepository
        )
    }
}
