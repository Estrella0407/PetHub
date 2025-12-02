package com.example.pethub.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pethub.data.local.database.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {

    @Query("SELECT * FROM services WHERE isActive = 1")
    fun getActiveServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE serviceId = :serviceId")
    suspend fun getServiceById(serviceId: String): ServiceEntity?

    @Query("SELECT * FROM services WHERE category = :category AND isActive = 1")
    fun getServicesByCategory(category: String): Flow<List<ServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceEntity>)

    @Query("DELETE FROM services")
    suspend fun clearAllServices()
}