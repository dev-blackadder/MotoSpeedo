package com.example.motospeedo.ui

import android.content.res.Configuration
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import kotlin.math.min
import com.example.motospeedo.R
import com.example.motospeedo.ui.theme.MotoSpeedoColors
import com.example.motospeedo.ui.theme.MotoSpeedoThemeAccessor
import com.example.motospeedo.util.UnitConverter
import com.example.motospeedo.viewmodel.DashboardViewModel
import com.example.motospeedo.viewmodel.DashboardViewModel.GpsAccuracy

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenSettings: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val keepBright by viewModel.keepBright.collectAsState()
    val colors = MotoSpeedoThemeAccessor.colors
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val accentColor = colors.accent
    val accentColorBright = colors.accentBright

    KeepScreenOn()
    BrightnessLock(enable = keepBright && state.isTripActive)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        val screenWidthDp = maxWidth
        val screenHeightDp = maxHeight

        if (isLandscape) {
            LandscapeLayout(state, viewModel, onBack, onOpenSettings, onOpenHistory, accentColor, accentColorBright, colors, screenWidthDp.value, screenHeightDp.value)
        } else {
            PortraitLayout(state, viewModel, onBack, onOpenSettings, onOpenHistory, accentColor, accentColorBright, colors, screenWidthDp.value, screenHeightDp.value)
        }

        if (state.gpsLost && state.isTripActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.errorBg)
                    .padding(vertical = 6.dp)
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                Text("GPS SIGNAL LOST", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }
        }
    }

    if (state.showStopConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelStopTrip() },
            containerColor = colors.surface,
            title = { Text("Stop Trip?", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to end this trip?", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmStopTrip() }) {
                    Text("STOP", color = colors.stopColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelStopTrip() }) {
                    Text("CONTINUE", color = colors.accent)
                }
            }
        )
    }

    if (state.showTripSummary) {
        TripSummaryDialog(viewModel.getTripSummary()) { viewModel.dismissTripSummary() }
    }
}

@Composable
private fun PortraitLayout(
    state: DashboardViewModel.DashboardUiState,
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    accentColor: Color,
    accentColorBright: Color,
    colors: MotoSpeedoColors,
    screenW: Float,
    screenH: Float
) {
    // Scale speed font to ~35% of screen width, capped at 140sp
    val speedFontSize = min(screenW * 0.35f, 140f)
    val headingFontSize = min(screenW * 0.08f, 32f)
    val statValueSize = min(screenW * 0.07f, 28f)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GpsIndicator(state.gpsAccuracy, colors)
            Row(verticalAlignment = Alignment.CenterVertically) {
                HistoryButton(onOpenHistory, colors)
                SettingsButton(onOpenSettings, colors)
                BackButton(onBack, colors)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        SpeedDisplay(
            speedMps = state.currentSpeed,
            isMetric = state.isMetric,
            onToggleUnits = { viewModel.toggleUnits() },
            fontSize = speedFontSize,
            warningActive = state.speedWarningActive,
            accentColorBright = accentColorBright,
            colors = colors
        )
        HeadingDisplay(state.compassDirection, state.heading, colors, headingFontSize)
        AltitudeDisplay(state.altitude, state.isMetric, colors)

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem("AVG", UnitConverter.formatSpeed(state.averageSpeed, state.isMetric), UnitConverter.speedUnit(state.isMetric), colors, statValueSize)
            StatItem("MAX", UnitConverter.formatSpeed(state.maxSpeed, state.isMetric), UnitConverter.speedUnit(state.isMetric), colors, statValueSize)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem("DIST", UnitConverter.formatDistance(state.tripDistance, state.isMetric), UnitConverter.distanceUnit(state.isMetric), colors, statValueSize)
            StatItem("TIME", UnitConverter.formatDuration(state.elapsedTime), "", colors, statValueSize)
        }

        Spacer(modifier = Modifier.height(16.dp))
        TripControls(state, viewModel, accentColor, colors)
    }
}

@Composable
private fun LandscapeLayout(
    state: DashboardViewModel.DashboardUiState,
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    accentColor: Color,
    accentColorBright: Color,
    colors: MotoSpeedoColors,
    screenW: Float,
    screenH: Float
) {
    // Scale speed font to ~40% of screen height, capped at 120sp
    val speedFontSize = min(screenH * 0.40f, 120f)
    val headingFontSize = min(screenH * 0.08f, 28f)
    val statValueSize = min(screenH * 0.09f, 26f)

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GpsIndicator(state.gpsAccuracy, colors)
            Row(verticalAlignment = Alignment.CenterVertically) {
                HistoryButton(onOpenHistory, colors)
                SettingsButton(onOpenSettings, colors)
            }
        }

        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SpeedDisplay(
                    speedMps = state.currentSpeed,
                    isMetric = state.isMetric,
                    onToggleUnits = { viewModel.toggleUnits() },
                    fontSize = speedFontSize,
                    warningActive = state.speedWarningActive,
                    accentColorBright = accentColorBright,
                    colors = colors
                )
                HeadingDisplay(state.compassDirection, state.heading, colors, headingFontSize)
                AltitudeDisplay(state.altitude, state.isMetric, colors)
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("AVG", UnitConverter.formatSpeed(state.averageSpeed, state.isMetric), UnitConverter.speedUnit(state.isMetric), colors, statValueSize)
                    StatItem("MAX", UnitConverter.formatSpeed(state.maxSpeed, state.isMetric), UnitConverter.speedUnit(state.isMetric), colors, statValueSize)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("DIST", UnitConverter.formatDistance(state.tripDistance, state.isMetric), UnitConverter.distanceUnit(state.isMetric), colors, statValueSize)
                    StatItem("TIME", UnitConverter.formatDuration(state.elapsedTime), "", colors, statValueSize)
                }

                Spacer(modifier = Modifier.weight(1f))
                TripControls(state, viewModel, accentColor, colors)
            }
        }
    }
}

@Composable
private fun TripControls(state: DashboardViewModel.DashboardUiState, viewModel: DashboardViewModel, accentColor: Color, colors: MotoSpeedoColors) {
    Row(
        modifier = Modifier.wrapContentWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!state.isTripActive) {
            TripButton("START", colors.surface, accentColor) { viewModel.startTrip() }
        } else {
            if (state.isPaused) {
                TripButton("RESUME", colors.surface, accentColor) { viewModel.resumeTrip() }
            } else {
                TripButton("PAUSE", colors.surface, colors.pauseColor) { viewModel.pauseTrip() }
            }
            TripButton("STOP", colors.surface, colors.stopColor) { viewModel.requestStopTrip() }
        }
    }
}

@Composable
private fun SpeedDisplay(
    speedMps: Float,
    isMetric: Boolean,
    onToggleUnits: () -> Unit,
    fontSize: Float,
    warningActive: Boolean,
    accentColorBright: Color,
    colors: MotoSpeedoColors
) {
    val displaySpeed = UnitConverter.mpsToDisplaySpeed(speedMps, isMetric)
    val unitFontSize = (fontSize * 0.17f).coerceIn(14f, 24f)

    val infiniteTransition = rememberInfiniteTransition(label = "warning")
    val warningAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "alpha"
    )
    val speedColor by animateColorAsState(
        targetValue = if (warningActive) colors.error else accentColorBright,
        label = "speedColor"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = displaySpeed.toInt().toString(),
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            color = speedColor,
            textAlign = TextAlign.Center,
            lineHeight = fontSize.sp,
            modifier = if (warningActive) Modifier.graphicsLayer { alpha = warningAlpha } else Modifier
        )
        Text(
            text = UnitConverter.speedUnit(isMetric),
            fontSize = unitFontSize.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary,
            modifier = Modifier
                .clickable(onClick = onToggleUnits)
                .padding(horizontal = 24.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AltitudeDisplay(altitudeMeters: Double, isMetric: Boolean, colors: MotoSpeedoColors) {
    val value = UnitConverter.metersToDisplayAltitude(altitudeMeters, isMetric)
    val unit = UnitConverter.altitudeUnit(isMetric)
    Text(
        text = "$value $unit alt",
        fontSize = 13.sp,
        color = colors.textDark,
        modifier = Modifier.padding(top = 2.dp)
    )
}

@Composable
private fun HeadingDisplay(compassDirection: String, bearing: Float, colors: MotoSpeedoColors, fontSize: Float = 32f) {
    val bearingSize = (fontSize * 0.625f).coerceIn(12f, 20f)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Text(text = compassDirection, fontSize = fontSize.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "${bearing.toInt()}°", fontSize = bearingSize.sp, color = colors.textMuted)
    }
}

@Composable
private fun GpsIndicator(accuracy: GpsAccuracy, colors: MotoSpeedoColors) {
    val (text, color) = when (accuracy) {
        GpsAccuracy.GOOD -> "GPS: Good" to colors.gpsGood
        GpsAccuracy.WEAK -> "GPS: Weak" to colors.gpsWeak
        GpsAccuracy.NO_SIGNAL -> "GPS: No Signal" to colors.gpsNoSignal
    }
    Text(text = text, fontSize = 14.sp, color = color)
}

@Composable
private fun BackButton(onClick: () -> Unit, colors: MotoSpeedoColors) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "Back to home",
            tint = colors.textDark,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun HistoryButton(onClick: () -> Unit, colors: MotoSpeedoColors) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
        Icon(
            painter = painterResource(id = R.drawable.ic_history),
            contentDescription = "Ride History",
            tint = colors.textMuted,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SettingsButton(onClick: () -> Unit, colors: MotoSpeedoColors) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
        Icon(
            painter = painterResource(id = R.drawable.ic_settings),
            contentDescription = "Settings",
            tint = colors.textMuted,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun StatItem(label: String, value: String, unit: String, colors: MotoSpeedoColors, valueFontSize: Float = 28f) {
    val labelSize = (valueFontSize * 0.46f).coerceIn(10f, 13f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = labelSize.sp, color = colors.textMuted, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = valueFontSize.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        if (unit.isNotEmpty()) Text(text = unit, fontSize = labelSize.sp, color = colors.textMuted)
    }
}

@Composable
private fun TripButton(label: String, containerColor: Color, textColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(44.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
private fun TripSummaryDialog(summary: DashboardViewModel.TripSummary, onDismiss: () -> Unit) {
    val colors = MotoSpeedoThemeAccessor.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text("Trip Complete", color = colors.accentBright, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryRow("Distance", UnitConverter.formatDistance(summary.distance, summary.isMetric), UnitConverter.distanceUnit(summary.isMetric), colors)
                SummaryRow("Duration", UnitConverter.formatDuration(summary.elapsedMs), "", colors)
                SummaryRow("Max Speed", UnitConverter.formatSpeed(summary.maxSpeedMps, summary.isMetric), UnitConverter.speedUnit(summary.isMetric), colors)
                SummaryRow("Avg Speed", UnitConverter.formatSpeed(summary.avgSpeedMps, summary.isMetric), UnitConverter.speedUnit(summary.isMetric), colors)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("DONE", color = colors.accent, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun SummaryRow(label: String, value: String, unit: String, colors: MotoSpeedoColors) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = colors.textMuted, fontSize = 15.sp)
        Text(
            "$value${if (unit.isNotEmpty()) " $unit" else ""}",
            color = colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? ComponentActivity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}

@Composable
private fun BrightnessLock(enable: Boolean) {
    val context = LocalContext.current
    DisposableEffect(enable) {
        val window = (context as? ComponentActivity)?.window
        if (enable) {
            window?.let {
                val lp = it.attributes
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
                it.attributes = lp
            }
        }
        onDispose {
            window?.let {
                val lp = it.attributes
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                it.attributes = lp
            }
        }
    }
}
