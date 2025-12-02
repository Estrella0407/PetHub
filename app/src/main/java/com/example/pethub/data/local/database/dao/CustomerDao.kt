package com.example.pethub.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pethub.data.local.database.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE custId = :custId")
    fun getCustomer(custId: String): Flow<CustomerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)
    
    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()
}
