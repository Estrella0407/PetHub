package com.example.pethub.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pethub.data.local.database.entity.BranchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BranchDao {
    @Query("SELECT * FROM branches WHERE branchId = :branchId")
    fun getBranch(branchId: String): Flow<BranchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranch(branch: BranchEntity)

    @Update
    suspend fun updateBranch(branch: BranchEntity)
}
