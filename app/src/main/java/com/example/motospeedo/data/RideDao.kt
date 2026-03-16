package com.example.motospeedo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ride: RideRecordEntity)

    @Query("SELECT * FROM rides ORDER BY startTime DESC")
    fun observeAll(): Flow<List<RideRecordEntity>>

    @Query("SELECT * FROM rides ORDER BY startTime DESC")
    suspend fun getAll(): List<RideRecordEntity>

    @Query("DELETE FROM rides WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM rides")
    suspend fun clearAll()

    @Query("DELETE FROM rides WHERE startTime < :cutoffMs")
    suspend fun deleteOlderThan(cutoffMs: Long)
}
