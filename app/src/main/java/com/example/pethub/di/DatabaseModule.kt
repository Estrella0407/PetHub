package com.example.pethub.di

import android.content.Context
import androidx.room.Room
import com.example.pethub.data.local.database.AppDatabase
import com.example.pethub.data.local.database.dao.BookingDao
import com.example.pethub.data.local.database.dao.PetDao
import com.example.pethub.data.local.database.dao.ServiceDao
import com.example.pethub.data.local.database.dao.UserDao
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
            // .addMigrations(MIGRATION_1_2) // Add migrations for production
            .build()
    }

    /**
     * Provide UserDao for user-related database operations
     */
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
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
     * Provide BookingDao for booking-related database operations
     */
    @Provides
    @Singleton
    fun provideBookingDao(database: AppDatabase): BookingDao {
        return database.bookingDao()
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





/**
 * Example UserEntity.kt
 */
/*
package com.yourcompany.petservices.data.local.database.entity

i
*/

/**
 * Example PetDao.kt
 */
/*

*/

/**
 * Example ServiceDao.kt
 */
/*
@Dao

*/

/**
 * Example BookingDao.kt
 */
/*

*/

/**
 * Example Converters.kt for Room type converters
 */
/*
package com.yourcompany.petservices.data.local.database


*/

/**
 * Example PreferenceManager.kt
 */
/*
package com.yourcompany.petservices.data.local.prefs


*/