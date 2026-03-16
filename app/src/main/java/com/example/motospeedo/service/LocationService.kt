package com.example.motospeedo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.motospeedo.MainActivity
import com.example.motospeedo.R
import com.example.motospeedo.data.LocationData
import com.example.motospeedo.data.LocationProvider
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject lateinit var locationProvider: LocationProvider

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "motospeedo_tracking"
    }

    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    locationProvider.update(
                        LocationData(
                            speedMps = if (location.hasSpeed()) location.speed else 0f,
                            bearing = if (location.hasBearing()) location.bearing else 0f,
                            accuracy = if (location.hasAccuracy()) location.accuracy else Float.MAX_VALUE,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            altitude = location.altitude,
                            timestamp = location.time
                        )
                    )
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        val client = LocationServices.getFusedLocationProviderClient(this)
        client.removeLocationUpdates(locationCallback)
        locationProvider.clear()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .build()

        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Ride Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when MotoSpeedo is tracking your ride"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MotoSpeedo")
            .setContentText("Tracking your ride")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
