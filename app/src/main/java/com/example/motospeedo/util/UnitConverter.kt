package com.example.motospeedo.util

object UnitConverter {

    fun mpsToKmh(mps: Float): Float = mps * 3.6f

    fun mpsToMph(mps: Float): Float = mps * 2.23694f

    fun mpsToDisplaySpeed(mps: Float, isMetric: Boolean): Float =
        if (isMetric) mpsToKmh(mps) else mpsToMph(mps)

    fun formatSpeed(mps: Float, isMetric: Boolean): String =
        mpsToDisplaySpeed(mps, isMetric).toInt().toString()

    fun speedUnit(isMetric: Boolean): String = if (isMetric) "km/h" else "mph"

    fun metersToKm(meters: Double): Double = meters / 1_000.0

    fun metersToMiles(meters: Double): Double = meters / 1_609.344

    fun metersToDisplayDistance(meters: Double, isMetric: Boolean): Double =
        if (isMetric) metersToKm(meters) else metersToMiles(meters)

    fun formatDistance(meters: Double, isMetric: Boolean): String {
        val value = metersToDisplayDistance(meters, isMetric)
        return if (value < 10) String.format("%.1f", value) else value.toInt().toString()
    }

    fun distanceUnit(isMetric: Boolean): String = if (isMetric) "km" else "mi"

    fun metersToDisplayAltitude(meters: Double, isMetric: Boolean): Int =
        if (isMetric) meters.toInt() else (meters * 3.28084).toInt()

    fun altitudeUnit(isMetric: Boolean): String = if (isMetric) "m" else "ft"

    fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1_000
        val hours = totalSeconds / 3_600
        val minutes = (totalSeconds % 3_600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
        else String.format("%d:%02d", minutes, seconds)
    }
}
