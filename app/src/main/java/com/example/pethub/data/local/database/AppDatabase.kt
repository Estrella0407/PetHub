package com.example.pethub.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pethub.data.local.database.dao.AppointmentDao
import com.example.pethub.data.local.database.dao.BranchDao
import com.example.pethub.data.local.database.dao.CustomerDao
import com.example.pethub.data.local.database.dao.PetDao
import com.example.pethub.data.local.database.dao.ServiceDao
import com.example.pethub.data.local.database.entity.AppointmentEntity
import com.example.pethub.data.local.database.entity.BranchEntity
import com.example.pethub.data.local.database.entity.BranchServiceEntity
import com.example.pethub.data.local.database.entity.CustomerEntity
import com.example.pethub.data.local.database.entity.PetEntity
import com.example.pethub.data.local.database.entity.ServiceEntity

// =======================================================================
// ANYONE WHO WANTS TO MODIFY THIS FILE PLEASE UPDATE THE VERSION NUMBER!
// =======================================================================
@Database(
    entities = [
        CustomerEntity::class,
        BranchEntity::class,
        BranchServiceEntity::class,
        PetEntity::class,
        ServiceEntity::class,
        AppointmentEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun branchDao(): BranchDao
    abstract fun petDao(): PetDao
    abstract fun serviceDao(): ServiceDao
    abstract fun appointmentDao(): AppointmentDao
}
