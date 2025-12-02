package com.example.pethub.di

import com.example.pethub.data.local.database.dao.BookingDao
import com.example.pethub.data.local.database.dao.PetDao
import com.example.pethub.data.local.database.dao.ServiceDao
import com.example.pethub.data.local.database.dao.UserDao
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
     * Provide UserRepository for user profile operations
     */
    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseService: FirebaseService,
        firestoreHelper: FirestoreHelper,
        cloudinaryService: CloudinaryService,
        userDao: UserDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): UserRepository {
        return UserRepository(
            firebaseService,
            firestoreHelper,
            cloudinaryService,
            userDao,
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
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): ServiceRepository {
        return ServiceRepository(
            firestoreHelper,
            cloudinaryService,
            serviceDao,
            ioDispatcher
        )
    }

    /**
     * Provide BookingRepository for booking management operations
     */
    @Provides
    @Singleton
    fun provideBookingRepository(
        firebaseService: FirebaseService,
        firestoreHelper: FirestoreHelper,
        bookingDao: BookingDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): BookingRepository {
        return BookingRepository(
            firebaseService,
            firestoreHelper,
            bookingDao,
            ioDispatcher
        )
    }

    /**
     * Provide NotificationRepository for notification operations
     */
//    @Provides
//    @Singleton
//    fun provideNotificationRepository(
//        firebaseService: FirebaseService,
//        firestoreHelper: FirestoreHelper,
//        preferenceManager: PreferenceManager,
//        @IoDispatcher ioDispatcher: CoroutineDispatcher
//    ): NotificationRepository {
//        return NotificationRepository(
//            firebaseService,
//            firestoreHelper,
//            preferenceManager,
//            ioDispatcher
//        )
//    }


    /**
     * Provide FAQRepository for FAQ operations
     */
//    @Provides
//    @Singleton
//    fun provideFAQRepository(
//        firestoreHelper: FirestoreHelper,
//        @IoDispatcher ioDispatcher: CoroutineDispatcher
//    ): FAQRepository {
//        return FAQRepository(
//            firestoreHelper,
//            ioDispatcher
//        )
//    }
}

