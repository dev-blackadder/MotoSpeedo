package com.example.motospeedo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motospeedo.R
import com.example.motospeedo.data.RideRecord
import com.example.motospeedo.ui.theme.MotoSpeedoThemeAccessor
import com.example.motospeedo.util.UnitConverter
import com.example.motospeedo.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RideHistoryScreen(viewModel: DashboardViewModel, onBack: () -> Unit) {
    val colors = MotoSpeedoThemeAccessor.colors
    val rides by viewModel.rideHistory.collectAsState()
    val isMetric by viewModel.uiState.collectAsState()
    val metric = isMetric.isMetric
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                "Ride History",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            if (rides.isNotEmpty()) {
                IconButton(onClick = { showClearConfirm = true }, modifier = Modifier.size(44.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Clear all",
                        tint = colors.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(44.dp))
            }
        }

        Text(
            "Last 30 days",
            fontSize = 13.sp,
            color = colors.textDark,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        if (rides.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("No rides yet", fontSize = 18.sp, color = colors.textDark)
                Text("Start a trip to record your ride", fontSize = 14.sp, color = colors.textDark)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(rides, key = { it.id }) { ride ->
                    RideCard(ride, metric) { viewModel.deleteRide(ride.id) }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = colors.surfaceElevated,
            title = { Text("Clear all rides?", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete all saved rides.", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearRideHistory()
                    showClearConfirm = false
                }) {
                    Text("CLEAR", color = colors.stopColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("CANCEL", color = colors.textMuted)
                }
            }
        )
    }
}

@Composable
private fun RideCard(ride: RideRecord, isMetric: Boolean, onDelete: () -> Unit) {
    val colors = MotoSpeedoThemeAccessor.colors
    val dateFmt = SimpleDateFormat("EEE d MMM  HH:mm", Locale.getDefault())
    val dateStr = dateFmt.format(Date(ride.startTime))

    val distStr = UnitConverter.formatDistance(ride.distanceMeters, isMetric)
    val distUnit = UnitConverter.distanceUnit(isMetric)
    val maxSpd = UnitConverter.formatSpeed(ride.maxSpeedMps, isMetric)
    val avgSpd = UnitConverter.formatSpeed(ride.avgSpeedMps, isMetric)
    val spdUnit = UnitConverter.speedUnit(isMetric)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceElevated, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(dateStr, fontSize = 14.sp, color = colors.accentBright, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                RideStat("DIST", "$distStr $distUnit")
                RideStat("TIME", UnitConverter.formatDuration(ride.elapsedMs))
                RideStat("MAX", "$maxSpd $spdUnit")
                RideStat("AVG", "$avgSpd $spdUnit")
            }
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = "Delete",
                tint = colors.textDark,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun RideStat(label: String, value: String) {
    val colors = MotoSpeedoThemeAccessor.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = colors.textDark, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Bold)
    }
}
