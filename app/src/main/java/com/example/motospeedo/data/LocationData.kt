package com.example.motospeedo.data

data class LocationData(
    val speedMps: Float = 0f,
    val bearing: Float = 0f,
    val accuracy: Float = Float.MAX_VALUE,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val timestamp: Long = 0L
)
