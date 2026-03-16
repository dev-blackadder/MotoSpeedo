package com.example.motospeedo.data

import android.content.SharedPreferences

class SettingsRepository(private val prefs: SharedPreferences) {

    companion object {
        private const val PREF_IS_METRIC = "is_metric"
        private const val PREF_SMOOTHING_ALPHA = "smoothing_alpha"
        private const val PREF_SPEED_WARNING = "speed_warning"
        private const val PREF_AUTO_START = "auto_start"
        private const val PREF_KEEP_BRIGHT = "keep_bright"
        private const val PREF_SPEED_ALERT = "speed_alert"
        private const val PREF_NIGHT_MODE = "night_mode"
        private const val PREF_AUTO_NIGHT = "auto_night"
        private const val PREF_NIGHT_START_H = "night_start_h"
        private const val PREF_NIGHT_START_M = "night_start_m"
        private const val PREF_NIGHT_END_H = "night_end_h"
        private const val PREF_NIGHT_END_M = "night_end_m"
        private const val PREF_TRIP_ACTIVE = "trip_active"
        private const val PREF_TRIP_START_MS = "trip_start_ms"
        private const val PREF_TRIP_PAUSED = "trip_paused"
        private const val PREF_TRIP_PAUSED_ELAPSED = "trip_paused_elapsed"

        const val DEFAULT_SMOOTHING_ALPHA = 0.65f
        const val DEFAULT_SPEED_WARNING = 110f
    }

    var isMetric: Boolean
        get() = prefs.getBoolean(PREF_IS_METRIC, true)
        set(value) = prefs.edit().putBoolean(PREF_IS_METRIC, value).apply()

    var smoothingAlpha: Float
        get() = prefs.getFloat(PREF_SMOOTHING_ALPHA, DEFAULT_SMOOTHING_ALPHA)
        set(value) = prefs.edit().putFloat(PREF_SMOOTHING_ALPHA, value).apply()

    var speedWarningThreshold: Float
        get() = prefs.getFloat(PREF_SPEED_WARNING, DEFAULT_SPEED_WARNING)
        set(value) = prefs.edit().putFloat(PREF_SPEED_WARNING, value).apply()

    var speedAlertEnabled: Boolean
        get() = prefs.getBoolean(PREF_SPEED_ALERT, true)
        set(value) = prefs.edit().putBoolean(PREF_SPEED_ALERT, value).apply()

    var autoStart: Boolean
        get() = prefs.getBoolean(PREF_AUTO_START, false)
        set(value) = prefs.edit().putBoolean(PREF_AUTO_START, value).apply()

    var keepBright: Boolean
        get() = prefs.getBoolean(PREF_KEEP_BRIGHT, false)
        set(value) = prefs.edit().putBoolean(PREF_KEEP_BRIGHT, value).apply()

    var nightModeEnabled: Boolean
        get() = prefs.getBoolean(PREF_NIGHT_MODE, false)
        set(value) = prefs.edit().putBoolean(PREF_NIGHT_MODE, value).apply()

    var autoNightEnabled: Boolean
        get() = prefs.getBoolean(PREF_AUTO_NIGHT, false)
        set(value) = prefs.edit().putBoolean(PREF_AUTO_NIGHT, value).apply()

    var nightStartHour: Int
        get() = prefs.getInt(PREF_NIGHT_START_H, 20)
        set(value) = prefs.edit().putInt(PREF_NIGHT_START_H, value).apply()

    var nightStartMinute: Int
        get() = prefs.getInt(PREF_NIGHT_START_M, 0)
        set(value) = prefs.edit().putInt(PREF_NIGHT_START_M, value).apply()

    var nightEndHour: Int
        get() = prefs.getInt(PREF_NIGHT_END_H, 6)
        set(value) = prefs.edit().putInt(PREF_NIGHT_END_H, value).apply()

    var nightEndMinute: Int
        get() = prefs.getInt(PREF_NIGHT_END_M, 0)
        set(value) = prefs.edit().putInt(PREF_NIGHT_END_M, value).apply()

    var tripActive: Boolean
        get() = prefs.getBoolean(PREF_TRIP_ACTIVE, false)
        set(value) = prefs.edit().putBoolean(PREF_TRIP_ACTIVE, value).apply()

    var tripStartMs: Long
        get() = prefs.getLong(PREF_TRIP_START_MS, 0L)
        set(value) = prefs.edit().putLong(PREF_TRIP_START_MS, value).apply()

    var tripPaused: Boolean
        get() = prefs.getBoolean(PREF_TRIP_PAUSED, false)
        set(value) = prefs.edit().putBoolean(PREF_TRIP_PAUSED, value).apply()

    var tripPausedElapsed: Long
        get() = prefs.getLong(PREF_TRIP_PAUSED_ELAPSED, 0L)
        set(value) = prefs.edit().putLong(PREF_TRIP_PAUSED_ELAPSED, value).apply()

    fun saveTripState(active: Boolean, startMs: Long, paused: Boolean, pausedElapsed: Long) {
        prefs.edit()
            .putBoolean(PREF_TRIP_ACTIVE, active)
            .putLong(PREF_TRIP_START_MS, startMs)
            .putBoolean(PREF_TRIP_PAUSED, paused)
            .putLong(PREF_TRIP_PAUSED_ELAPSED, pausedElapsed)
            .apply()
    }
}
