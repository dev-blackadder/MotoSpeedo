package com.example.motospeedo.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class SpeedProcessor(private var alpha: Float) {

    var smoothedSpeed = 0f
        private set
    var maxSpeed = 0f
        private set
    var speedSampleCount = 0
        private set
    var speedSampleSum = 0.0
        private set

    val averageSpeed: Float
        get() = if (speedSampleCount > 0) (speedSampleSum / speedSampleCount).toFloat() else 0f

    fun updateAlpha(newAlpha: Float) {
        alpha = newAlpha
    }

    fun processSpeed(speedMps: Float, accuracy: Float): Boolean {
        if (accuracy >= ACCURACY_THRESHOLD) return false
        smoothedSpeed = alpha * speedMps + (1 - alpha) * smoothedSpeed
        if (smoothedSpeed > maxSpeed) maxSpeed = smoothedSpeed
        if (smoothedSpeed > 0.5f) {
            speedSampleCount++
            speedSampleSum += smoothedSpeed
        }
        return true
    }

    fun isSpeedWarning(thresholdKmh: Float): Boolean {
        if (thresholdKmh <= 0) return false
        val thresholdMps = thresholdKmh / 3.6f
        return smoothedSpeed >= thresholdMps
    }

    fun reset() {
        smoothedSpeed = 0f
        maxSpeed = 0f
        speedSampleCount = 0
        speedSampleSum = 0.0
    }

    companion object {
        const val ACCURACY_THRESHOLD = 30f

        fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6_371_000.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }

        fun bearingToCompass(bearing: Float): String {
            val normalized = ((bearing % 360) + 360) % 360
            return when {
                normalized < 22.5f || normalized >= 337.5f -> "N"
                normalized < 67.5f  -> "NE"
                normalized < 112.5f -> "E"
                normalized < 157.5f -> "SE"
                normalized < 202.5f -> "S"
                normalized < 247.5f -> "SW"
                normalized < 292.5f -> "W"
                else -> "NW"
            }
        }
    }
}
