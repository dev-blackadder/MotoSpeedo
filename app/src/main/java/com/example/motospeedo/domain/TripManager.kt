package com.example.motospeedo.domain

import com.example.motospeedo.data.LocationData
import com.example.motospeedo.data.RideRecord
import com.example.motospeedo.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TripManager(
    private val settings: SettingsRepository,
    private val speedProcessor: SpeedProcessor,
    private val scope: CoroutineScope
) {

    var isTripActive: Boolean = false
        private set
    var isPaused: Boolean = false
        private set
    var totalDistance: Double = 0.0
        private set
    var elapsedTime: Long = 0L
        private set

    private var startTimeMs: Long = 0L
    private var pausedAccumulatedMs: Long = 0L
    private var lastLat: Double = Double.NaN
    private var lastLng: Double = Double.NaN

    fun tryRestore(): Boolean {
        if (!settings.tripActive) return false
        startTimeMs = settings.tripStartMs
        isPaused = settings.tripPaused
        isTripActive = true
        if (isPaused) {
            elapsedTime = settings.tripPausedElapsed
        } else {
            pausedAccumulatedMs = settings.tripPausedElapsed
            elapsedTime = computeElapsed()
        }
        return true
    }

    fun startTrip() {
        startTimeMs = System.currentTimeMillis()
        pausedAccumulatedMs = 0L
        elapsedTime = 0L
        totalDistance = 0.0
        isTripActive = true
        isPaused = false
        lastLat = Double.NaN
        lastLng = Double.NaN
        speedProcessor.reset()
        persistTripState()

        scope.launch {
            while (isTripActive) {
                delay(5_000)
                if (isTripActive) persistTripState()
            }
        }
    }

    fun processLocation(location: LocationData) {
        if (!isTripActive || isPaused) return
        elapsedTime = computeElapsed()
        if (!lastLat.isNaN() && !lastLng.isNaN()) {
            val d = SpeedProcessor.haversine(lastLat, lastLng, location.latitude, location.longitude)
            if (d < 500.0) totalDistance += d
        }
        lastLat = location.latitude
        lastLng = location.longitude
    }

    fun pauseTrip() {
        if (!isTripActive || isPaused) return
        elapsedTime = computeElapsed()
        isPaused = true
        settings.saveTripState(
            active = true,
            startMs = startTimeMs,
            paused = true,
            pausedElapsed = elapsedTime
        )
    }

    fun resumeTrip() {
        if (!isTripActive || !isPaused) return
        pausedAccumulatedMs = elapsedTime
        startTimeMs = System.currentTimeMillis()
        isPaused = false
        lastLat = Double.NaN
        lastLng = Double.NaN
        persistTripState()
    }

    fun stopTrip(): RideRecord {
        elapsedTime = computeElapsed()
        val record = RideRecord(
            id = startTimeMs,
            startTime = startTimeMs,
            elapsedMs = elapsedTime,
            distanceMeters = totalDistance,
            maxSpeedMps = speedProcessor.maxSpeed,
            avgSpeedMps = speedProcessor.averageSpeed
        )
        isTripActive = false
        isPaused = false
        settings.saveTripState(active = false, startMs = 0L, paused = false, pausedElapsed = 0L)
        return record
    }

    private fun computeElapsed(): Long =
        if (isPaused) elapsedTime
        else System.currentTimeMillis() - startTimeMs + pausedAccumulatedMs

    private fun persistTripState() {
        settings.saveTripState(
            active = isTripActive,
            startMs = startTimeMs,
            paused = isPaused,
            pausedElapsed = pausedAccumulatedMs
        )
    }
}
