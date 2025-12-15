package com.example.pethub.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pethub.data.local.database.entity.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {

    @Query("SELECT * FROM pet WHERE custId = :userId")
    fun getUserPets(userId: String): Flow<List<PetEntity>>

    @Query("SELECT * FROM pet WHERE petId = :petId")
    suspend fun getPetById(petId: String): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPets(pets: List<PetEntity>)

    @Update
    suspend fun updatePet(pet: PetEntity)

    @Delete
    suspend fun deletePet(pet: PetEntity)

    @Query("DELETE FROM pet WHERE custId = :userId")
    suspend fun deleteUserPets(userId: String)
}