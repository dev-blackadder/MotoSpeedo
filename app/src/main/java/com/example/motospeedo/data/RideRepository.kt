package com.example.motospeedo.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideRepository @Inject constructor(
    private val rideDao: RideDao
) {
    private val retentionMs = TimeUnit.DAYS.toMillis(30)

    val rides: Flow<List<RideRecord>> = rideDao.observeAll().map { entities ->
        entities.map { it.toRideRecord() }
    }

    suspend fun saveRide(record: RideRecord) {
        val cutoff = System.currentTimeMillis() - retentionMs
        rideDao.deleteOlderThan(cutoff)
        rideDao.insert(RideRecordEntity.fromRideRecord(record))
    }

    suspend fun deleteRide(id: Long) {
        rideDao.deleteById(id)
    }

    suspend fun clearAll() {
        rideDao.clearAll()
    }
}
