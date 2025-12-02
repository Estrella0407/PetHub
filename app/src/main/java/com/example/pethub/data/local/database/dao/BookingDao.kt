package com.example.pethub.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pethub.data.local.database.entity.BookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY dateTime DESC")
    fun getUserBookings(userId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE bookingId = :bookingId")
    suspend fun getBookingById(bookingId: String): BookingEntity?

    @Query("SELECT * FROM bookings WHERE userId = :userId AND status IN (:statuses) ORDER BY dateTime ASC")
    fun getUpcomingBookings(userId: String, statuses: List<String>): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<BookingEntity>)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("DELETE FROM bookings WHERE userId = :userId")
    suspend fun deleteUserBookings(userId: String)
}