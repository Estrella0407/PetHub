package com.example.pethub.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.data.remote.FirestoreHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Firebase and network-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provide Firebase Authentication instance
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    /**
     * Provide Firebase Firestore instance with settings
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()

        // Configure Firestore settings
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) // Enable offline persistence
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()

        firestore.firestoreSettings = settings

        return firestore
    }

    /**
     * Provide Firebase Cloud Messaging instance
     */
    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    /**
     * Provide FirebaseService
     */
    @Provides
    @Singleton
    fun provideFirebaseService(
        auth: FirebaseAuth,
        messaging: FirebaseMessaging,
        @ApplicationContext context: Context
    ): FirebaseService {
        return FirebaseService(auth, messaging, context)
    }

    /**
     * Provide FirestoreHelper
     */
    @Provides
    @Singleton
    fun provideFirestoreHelper(
        firestore: FirebaseFirestore
    ): FirestoreHelper {
        return FirestoreHelper(firestore)
    }
}

/**
 * Example usage in a Repository:
 *
 * @Inject constructor(
 *     private val firebaseService: FirebaseService,
 *     private val firestoreHelper: FirestoreHelper
 * )
 */