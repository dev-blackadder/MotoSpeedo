package com.example.motospeedo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rides")
data class RideRecordEntity(
    @PrimaryKey val id: Long,
    val startTime: Long,
    val elapsedMs: Long,
    val distanceMeters: Double,
    val maxSpeedMps: Float,
    val avgSpeedMps: Float
) {
    fun toRideRecord() = RideRecord(
        id = id,
        startTime = startTime,
        elapsedMs = elapsedMs,
        distanceMeters = distanceMeters,
        maxSpeedMps = maxSpeedMps,
        avgSpeedMps = avgSpeedMps
    )

    companion object {
        fun fromRideRecord(r: RideRecord) = RideRecordEntity(
            id = r.id,
            startTime = r.startTime,
            elapsedMs = r.elapsedMs,
            distanceMeters = r.distanceMeters,
            maxSpeedMps = r.maxSpeedMps,
            avgSpeedMps = r.avgSpeedMps
        )
    }
}
