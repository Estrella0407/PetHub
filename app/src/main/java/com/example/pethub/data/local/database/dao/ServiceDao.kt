package com.example.pethub.data.local.database.dao

import androidx.room.*
import com.example.pethub.data.local.database.entity.BranchServiceEntity
import com.example.pethub.data.local.database.entity.ServiceEntity
import com.example.pethub.data.local.database.model.ServiceWithAvailability
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {

    // --- Standard Service Operations ---
    @Query("SELECT * FROM service")
    fun getAllBaseServices(): Flow<List<ServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceEntity>)

    // --- Branch Specific Operations ---

    // Insert the links
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranchServices(branchServices: List<BranchServiceEntity>)

    // Clear links for a specific branch (useful when refreshing data)
    @Query("DELETE FROM branch_service WHERE branchId = :branchId")
    suspend fun clearServicesForBranch(branchId: String)

    /**
     * Get ALL services for a branch, merging the Service details with the availability flag.
     * This uses INNER JOIN, so it only returns services that exist in the branch_service table.
     */
    @Transaction
    @Query("""
        SELECT s.*, bs.availability, bs.branchId
        FROM service s
        INNER JOIN branch_service bs ON s.serviceId = bs.serviceId
        WHERE bs.branchId = :branchId 

    """)
    fun getServicesForBranch(branchId: String): Flow<List<ServiceWithAvailability>>

    /**
     * Get services by category for a specific branch.
     */
    @Transaction
    @Query("""
        SELECT s.*, bs.availability, bs.branchId
        FROM service s
        INNER JOIN branch_service bs ON s.serviceId = bs.serviceId
        WHERE bs.branchId = :branchId 
        AND s.serviceName = :serviceName
    """)
    fun getBranchServicesByCategory(branchId: String, serviceName: String): Flow<List<ServiceWithAvailability>>
}
