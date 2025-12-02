package com.example.pethub.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pethub.data.local.database.dao.BookingDao
import com.example.pethub.data.local.database.dao.PetDao
import com.example.pethub.data.local.database.dao.ServiceDao
import com.example.pethub.data.local.database.dao.UserDao
import com.example.pethub.data.local.database.entity.BookingEntity
import com.example.pethub.data.local.database.entity.PetEntity
import com.example.pethub.data.local.database.entity.ServiceEntity
import com.example.pethub.data.local.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        PetEntity::class,
        ServiceEntity::class,
        BookingEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun petDao(): PetDao
    abstract fun serviceDao(): ServiceDao
    abstract fun bookingDao(): BookingDao
}