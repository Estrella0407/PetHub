package com.example.pethub.di

import android.app.Application
import android.content.Context
import com.example.pethub.data.remote.CloudinaryService
import com.cloudinary.android.MediaManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Hilt module for application-wide dependencies
 * Provides Context, Dispatchers, and general app utilities
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provide Application Context
     * Used throughout the app for various operations
     */
    @Provides
    @Singleton
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ): Context = context

    /**
     * Provide Application instance
     */
    @Provides
    @Singleton
    fun provideApplication(
        @ApplicationContext context: Context
    ): Application = context as Application

    /**
     * Provide IO Dispatcher for background work
     * Used for database operations, network calls, file I/O
     */
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Provide Main Dispatcher for UI updates
     * Used for updating UI elements
     */
    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    /**
     * Provide Default Dispatcher for CPU-intensive work
     * Used for heavy computations
     */
    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    /**
     * Provide Cloudinary Service for image uploads
     * Alternative to Firebase Storage (FREE!)
     */
    @Provides
    @Singleton
    fun provideCloudinaryService(
        @ApplicationContext context: Context
    ): CloudinaryService {
        // Initialize Cloudinary MediaManager
        val config = hashMapOf(
            "cloud_name" to "YOUR_CLOUD_NAME",
            "api_key" to "YOUR_API_KEY",
            "api_secret" to "YOUR_API_SECRET"
        )

        MediaManager.init(context, config)

        return CloudinaryService(MediaManager.get())
    }

    /**
     * Provide SharedPreferences
     * For storing simple key-value data
     */
    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): android.content.SharedPreferences {
        return context.getSharedPreferences(
            "pet_services_prefs",
            Context.MODE_PRIVATE
        )
    }
}

/**
 * Qualifier annotations for different Dispatchers
 * Used to distinguish between different dispatcher types
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher