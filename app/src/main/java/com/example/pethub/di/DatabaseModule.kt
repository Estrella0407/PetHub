package com.example.pethub.di

import android.content.Context
import androidx.room.Room
import com.example.pethub.data.local.database.AppDatabase
import com.example.pethub.data.local.database.dao.AppointmentDao
import com.example.pethub.data.local.database.dao.BranchDao
import com.example.pethub.data.local.database.dao.CustomerDao
import com.example.pethub.data.local.database.dao.PetDao
import com.example.pethub.data.local.database.dao.ServiceDao
import com.example.pethub.data.local.prefs.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Room Database and local storage
 * Provides database instance and DAOs for offline caching
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provide Room Database instance
     * Used for offline data caching
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pet_services_database"
        )
            .fallbackToDestructiveMigration() // During development
            .build()
    }

    /**
     * Provide CustomerDao for customer-related database operations
     */
    @Provides
    @Singleton
    fun provideCustomerDao(database: AppDatabase): CustomerDao {
        return database.customerDao()
    }

    /**
     * Provide BranchDao for branch-related database operations
     */
    @Provides
    @Singleton
    fun provideBranchDao(database: AppDatabase): BranchDao {
        return database.branchDao()
    }

    /**
     * Provide PetDao for pet-related database operations
     */
    @Provides
    @Singleton
    fun providePetDao(database: AppDatabase): PetDao {
        return database.petDao()
    }

    /**
     * Provide ServiceDao for service-related database operations
     */
    @Provides
    @Singleton
    fun provideServiceDao(database: AppDatabase): ServiceDao {
        return database.serviceDao()
    }

    /**
     * Provide AppointmentDao for appointment-related database operations
     */
    @Provides
    @Singleton
    fun provideAppointmentDao(database: AppDatabase): AppointmentDao {
        return database.appointmentDao()
    }

    /**
     * Provide PreferenceManager for app preferences
     * Used for storing user settings, tokens, etc.
     */
    @Provides
    @Singleton
    fun providePreferenceManager(
        @ApplicationContext context: Context
    ): PreferenceManager {
        return PreferenceManager(context)
    }
}
