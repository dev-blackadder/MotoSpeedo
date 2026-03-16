package com.example.motospeedo.viewmodel

import android.app.Application
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.motospeedo.data.LocationProvider
import com.example.motospeedo.data.RideRecord
import com.example.motospeedo.data.RideRepository
import com.example.motospeedo.data.SettingsRepository
import com.example.motospeedo.domain.NightModeManager
import com.example.motospeedo.domain.SpeedProcessor
import com.example.motospeedo.domain.TripManager
import com.example.motospeedo.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    application: Application,
    val settings: SettingsRepository,
    private val rideRepository: RideRepository,
    private val locationProvider: LocationProvider
) : AndroidViewModel(application) {

    enum class GpsAccuracy { GOOD, WEAK, NO_SIGNAL }

    data class DashboardUiState(
        val currentSpeed: Float = 0f,
        val maxSpeed: Float = 0f,
        val averageSpeed: Float = 0f,
        val tripDistance: Double = 0.0,
        val heading: Float = 0f,
        val altitude: Double = 0.0,
        val compassDirection: String = "--",
        val elapsedTime: Long = 0L,
        val isMetric: Boolean = true,
        val isTripActive: Boolean = false,
        val isPaused: Boolean = false,
        val gpsAccuracy: GpsAccuracy = GpsAccuracy.NO_SIGNAL,
        val gpsLost: Boolean = false,
        val speedWarningActive: Boolean = false,
        val showTripSummary: Boolean = false,
        val showStopConfirm: Boolean = false,
        val isNightMode: Boolean = false
    )

    data class TripSummary(
        val distance: Double,
        val elapsedMs: Long,
        val maxSpeedMps: Float,
        val avgSpeedMps: Float,
        val isMetric: Boolean
    )

    private val speedProcessor = SpeedProcessor(settings.smoothingAlpha)
    private val nightModeManager = NightModeManager(settings)
    private val tripManager = TripManager(settings, speedProcessor, viewModelScope)

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            isMetric = settings.isMetric,
            isNightMode = nightModeManager.computeNightMode()
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState

    private val _smoothingAlpha = MutableStateFlow(settings.smoothingAlpha)
    val smoothingAlpha: StateFlow<Float> = _smoothingAlpha

    private val _speedWarningThreshold = MutableStateFlow(settings.speedWarningThreshold)
    val speedWarningThreshold: StateFlow<Float> = _speedWarningThreshold

    private val _autoStart = MutableStateFlow(settings.autoStart)
    val autoStart: StateFlow<Boolean> = _autoStart

    private val _keepBright = MutableStateFlow(settings.keepBright)
    val keepBright: StateFlow<Boolean> = _keepBright

    private val _speedAlertEnabled = MutableStateFlow(settings.speedAlertEnabled)
    val speedAlertEnabled: StateFlow<Boolean> = _speedAlertEnabled

    private val _nightModeEnabled = MutableStateFlow(settings.nightModeEnabled)
    val nightModeEnabled: StateFlow<Boolean> = _nightModeEnabled

    private val _autoNightEnabled = MutableStateFlow(settings.autoNightEnabled)
    val autoNightEnabled: StateFlow<Boolean> = _autoNightEnabled

    private val _nightStartHour = MutableStateFlow(settings.nightStartHour)
    val nightStartHour: StateFlow<Int> = _nightStartHour

    private val _nightStartMinute = MutableStateFlow(settings.nightStartMinute)
    val nightStartMinute: StateFlow<Int> = _nightStartMinute

    private val _nightEndHour = MutableStateFlow(settings.nightEndHour)
    val nightEndHour: StateFlow<Int> = _nightEndHour

    private val _nightEndMinute = MutableStateFlow(settings.nightEndMinute)
    val nightEndMinute: StateFlow<Int> = _nightEndMinute

    val rideHistory = rideRepository.rides.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private var gpsWatchdogJob: Job? = null
    private var lastSpeedWarning = false
    private var toneGenerator: ToneGenerator? = null
    private var summaryRecord: RideRecord? = null

    companion object {
        private const val GPS_TIMEOUT_MS = 5000L
        private const val AUTO_START_SPEED_MPS = 1.4f
    }

    init {
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                _uiState.update { it.copy(isNightMode = nightModeManager.computeNightMode()) }
            }
        }

        // Always start location service so GPS signal is available immediately
        ContextCompat.startForegroundService(
            getApplication(),
            Intent(getApplication(), LocationService::class.java)
        )

        if (tripManager.tryRestore()) {
            syncTripState()
            resetGpsWatchdog()
        }

        viewModelScope.launch {
            locationProvider.locationFlow.collect { location ->
                if (location != null) {
                    updateGpsStatus(location)
                    if (tripManager.isTripActive && !tripManager.isPaused) {
                        speedProcessor.processSpeed(location.speedMps, location.accuracy)
                        tripManager.processLocation(location)
                        updateUiFromLocation(location)
                    } else if (_autoStart.value && !tripManager.isTripActive) {
                        if (location.speedMps >= AUTO_START_SPEED_MPS && location.accuracy < SpeedProcessor.ACCURACY_THRESHOLD) {
                            startTrip()
                        }
                    }
                    resetGpsWatchdog()
                }
            }
        }

        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (tripManager.isTripActive && !tripManager.isPaused) {
                    syncTripState()
                }
            }
        }
    }

    fun startTrip() {
        tripManager.startTrip()
        _uiState.update {
            DashboardUiState(
                isTripActive = true,
                isMetric = it.isMetric,
                isNightMode = it.isNightMode,
            )
        }
        ContextCompat.startForegroundService(
            getApplication(),
            Intent(getApplication(), LocationService::class.java)
        )
        resetGpsWatchdog()
    }

    fun requestStopTrip() {
        _uiState.update { it.copy(showStopConfirm = true) }
    }

    fun cancelStopTrip() {
        _uiState.update { it.copy(showStopConfirm = false) }
    }

    fun confirmStopTrip() {
        gpsWatchdogJob?.cancel(); gpsWatchdogJob = null
        val record = tripManager.stopTrip()
        summaryRecord = record
        viewModelScope.launch { rideRepository.saveRide(record) }
        getApplication<Application>().stopService(
            Intent(getApplication(), LocationService::class.java)
        )
        _uiState.update {
            it.copy(
                isTripActive = false,
                isPaused = false,
                showStopConfirm = false,
                showTripSummary = true
            )
        }
    }

    fun dismissTripSummary() {
        _uiState.update { it.copy(showTripSummary = false) }
    }

    fun pauseTrip() {
        tripManager.pauseTrip()
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumeTrip() {
        tripManager.resumeTrip()
        _uiState.update { it.copy(isPaused = false) }
        resetGpsWatchdog()
    }

    fun deleteRide(id: Long) {
        viewModelScope.launch { rideRepository.deleteRide(id) }
    }

    fun clearRideHistory() {
        viewModelScope.launch { rideRepository.clearAll() }
    }

    fun toggleUnits() {
        val newIsMetric = !_uiState.value.isMetric
        settings.isMetric = newIsMetric
        _uiState.update { it.copy(isMetric = newIsMetric) }
    }

    fun getTripSummary(): TripSummary {
        val r = summaryRecord ?: return TripSummary(0.0, 0L, 0f, 0f, settings.isMetric)
        return TripSummary(r.distanceMeters, r.elapsedMs, r.maxSpeedMps, r.avgSpeedMps, _uiState.value.isMetric)
    }

    fun setSmoothingAlpha(alpha: Float) {
        _smoothingAlpha.value = alpha
        settings.smoothingAlpha = alpha
        speedProcessor.updateAlpha(alpha)
    }

    fun setSpeedWarningThreshold(kmh: Float) {
        _speedWarningThreshold.value = kmh
        settings.speedWarningThreshold = kmh
    }

    fun setAutoStart(enabled: Boolean) {
        _autoStart.value = enabled
        settings.autoStart = enabled
        if (enabled && !tripManager.isTripActive) {
            ContextCompat.startForegroundService(
                getApplication(),
                Intent(getApplication(), LocationService::class.java)
            )
        }
    }

    fun setKeepBright(enabled: Boolean) {
        _keepBright.value = enabled
        settings.keepBright = enabled
    }

    fun setSpeedAlertEnabled(enabled: Boolean) {
        _speedAlertEnabled.value = enabled
        settings.speedAlertEnabled = enabled
    }

    fun setNightModeEnabled(enabled: Boolean) {
        _nightModeEnabled.value = enabled
        settings.nightModeEnabled = enabled
        _uiState.update { it.copy(isNightMode = nightModeManager.computeNightMode()) }
    }

    fun setAutoNightEnabled(enabled: Boolean) {
        _autoNightEnabled.value = enabled
        settings.autoNightEnabled = enabled
        _uiState.update { it.copy(isNightMode = nightModeManager.computeNightMode()) }
    }

    fun setNightStartTime(hour: Int, minute: Int) {
        _nightStartHour.value = hour; _nightStartMinute.value = minute
        settings.nightStartHour = hour; settings.nightStartMinute = minute
        _uiState.update { it.copy(isNightMode = nightModeManager.computeNightMode()) }
    }

    fun setNightEndTime(hour: Int, minute: Int) {
        _nightEndHour.value = hour; _nightEndMinute.value = minute
        settings.nightEndHour = hour; settings.nightEndMinute = minute
        _uiState.update { it.copy(isNightMode = nightModeManager.computeNightMode()) }
    }

    private fun updateGpsStatus(location: com.example.motospeedo.data.LocationData) {
        val accuracy = when {
            location.accuracy < 10f -> GpsAccuracy.GOOD
            location.accuracy < SpeedProcessor.ACCURACY_THRESHOLD -> GpsAccuracy.WEAK
            else -> GpsAccuracy.NO_SIGNAL
        }
        _uiState.update {
            it.copy(
                gpsAccuracy = accuracy,
                heading = location.bearing,
                altitude = location.altitude,
                compassDirection = SpeedProcessor.bearingToCompass(location.bearing)
            )
        }
    }

    private fun updateUiFromLocation(location: com.example.motospeedo.data.LocationData) {
        val speedWarning = speedProcessor.isSpeedWarning(_speedWarningThreshold.value)
        if (speedWarning && !lastSpeedWarning && _speedAlertEnabled.value) {
            playSpeedAlert()
        }
        lastSpeedWarning = speedWarning

        _uiState.update {
            it.copy(
                currentSpeed = speedProcessor.smoothedSpeed,
                maxSpeed = speedProcessor.maxSpeed,
                averageSpeed = speedProcessor.averageSpeed,
                tripDistance = tripManager.totalDistance,
                elapsedTime = tripManager.elapsedTime,
                speedWarningActive = speedWarning
            )
        }
    }

    private fun syncTripState() {
        _uiState.update {
            it.copy(
                isTripActive = tripManager.isTripActive,
                isPaused = tripManager.isPaused,
                elapsedTime = tripManager.elapsedTime,
                tripDistance = tripManager.totalDistance,
                currentSpeed = speedProcessor.smoothedSpeed,
                maxSpeed = speedProcessor.maxSpeed,
                averageSpeed = speedProcessor.averageSpeed
            )
        }
    }

    private fun resetGpsWatchdog() {
        gpsWatchdogJob?.cancel()
        _uiState.update { it.copy(gpsLost = false) }
        if (tripManager.isTripActive && !tripManager.isPaused) {
            gpsWatchdogJob = viewModelScope.launch {
                delay(GPS_TIMEOUT_MS)
                _uiState.update { it.copy(gpsLost = true) }
            }
        }
    }

    private fun playSpeedAlert() {
        viewModelScope.launch {
            repeat(3) {
                try {
                    val tg = toneGenerator
                        ?: ToneGenerator(AudioManager.STREAM_NOTIFICATION, ToneGenerator.MAX_VOLUME)
                            .also { toneGenerator = it }
                    tg.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                } catch (_: Exception) {}
                delay(280)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        toneGenerator?.release()
        toneGenerator = null
    }
}
