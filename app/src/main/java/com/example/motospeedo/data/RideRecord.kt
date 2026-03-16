package com.example.motospeedo.data

data class RideRecord(
    val id: Long,
    val startTime: Long,
    val elapsedMs: Long,
    val distanceMeters: Double,
    val maxSpeedMps: Float,
    val avgSpeedMps: Float
)
