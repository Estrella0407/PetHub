package com.example.pethub.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pethub.data.local.database.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Query("SELECT * FROM appointments WHERE branchId = :branchId ORDER BY dateTime DESC")
    fun getBranchAppointments(branchId: String): Flow<List<AppointmentEntity>>

    // Note: Since we don't have userId in Appointment anymore (it's linked via Pet -> Customer), 
    // retrieving by Customer requires a join or multiple queries. 
    // For now, simpler queries:

    @Query("SELECT * FROM appointments WHERE appointmentId = :appointmentId")
    suspend fun getAppointmentById(appointmentId: String): AppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<AppointmentEntity>)

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)
    
    @Query("DELETE FROM appointments WHERE appointmentId = :appointmentId")
    suspend fun deleteAppointment(appointmentId: String)
}
