package com.example.motospeedo.ui

import android.app.TimePickerDialog
import com.example.motospeedo.BuildConfig
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motospeedo.R
import com.example.motospeedo.ui.theme.MotoSpeedoThemeAccessor
import com.example.motospeedo.util.UnitConverter
import com.example.motospeedo.viewmodel.DashboardViewModel

@Composable
fun SettingsScreen(viewModel: DashboardViewModel, onBack: () -> Unit) {
    val colors = MotoSpeedoThemeAccessor.colors
    val smoothingAlpha by viewModel.smoothingAlpha.collectAsState()
    val speedWarning by viewModel.speedWarningThreshold.collectAsState()
    val autoStart by viewModel.autoStart.collectAsState()
    val keepBright by viewModel.keepBright.collectAsState()
    val speedAlertEnabled by viewModel.speedAlertEnabled.collectAsState()
    val nightModeEnabled by viewModel.nightModeEnabled.collectAsState()
    val autoNightEnabled by viewModel.autoNightEnabled.collectAsState()
    val nightStartHour by viewModel.nightStartHour.collectAsState()
    val nightStartMinute by viewModel.nightStartMinute.collectAsState()
    val nightEndHour by viewModel.nightEndHour.collectAsState()
    val nightEndMinute by viewModel.nightEndMinute.collectAsState()
    val isMetric by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back",
                tint = colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Settings", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader("RIDE", colors.accent)

        SettingsSwitchRow(
            label = "Auto-start trip",
            description = "Start trip automatically when movement is detected (~5 km/h)",
            checked = autoStart,
            onCheckedChange = { viewModel.setAutoStart(it) },
            accentColor = colors.accent
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Speed warning threshold  \u2014  ${speedWarning.toInt()} ${UnitConverter.speedUnit(isMetric.isMetric)}",
            fontSize = 15.sp,
            color = colors.textPrimary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Speed readout flashes red above this value. Set to 0 to disable.",
            fontSize = 13.sp,
            color = colors.textMuted,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Slider(
            value = speedWarning,
            onValueChange = { viewModel.setSpeedWarningThreshold(it) },
            valueRange = 0f..250f,
            steps = 49,
            colors = SliderDefaults.colors(
                thumbColor = colors.accentBright,
                activeTrackColor = colors.accent,
                inactiveTrackColor = colors.cardBorder
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSwitchRow(
            label = "Audible speed alert",
            description = "Plays 3 beeps through the speaker when you cross the speed warning threshold",
            checked = speedAlertEnabled,
            onCheckedChange = { viewModel.setSpeedAlertEnabled(it) },
            accentColor = colors.accent
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(color = colors.cardBorder)

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("GPS & DISPLAY", colors.accent)

        SettingsSwitchRow(
            label = "Max brightness during trip",
            description = "Pins screen to full brightness \u2014 useful in sunlight on a handlebar mount",
            checked = keepBright,
            onCheckedChange = { viewModel.setKeepBright(it) },
            accentColor = colors.accent
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Speed smoothing  \u2014  ${String.format("%.2f", smoothingAlpha)}",
            fontSize = 15.sp,
            color = colors.textPrimary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Lower = smoother but slower to react. Higher = snappier but jitterier.",
            fontSize = 13.sp,
            color = colors.textMuted,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Slider(
            value = smoothingAlpha,
            onValueChange = { viewModel.setSmoothingAlpha(it) },
            valueRange = 0.1f..0.95f,
            colors = SliderDefaults.colors(
                thumbColor = colors.accentBright,
                activeTrackColor = colors.accent,
                inactiveTrackColor = colors.cardBorder
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(color = colors.cardBorder)

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("NIGHT MODE", colors.accent)

        SettingsSwitchRow(
            label = "Night mode",
            description = "Switches the display to amber tones \u2014 easier on the eyes after dark",
            checked = nightModeEnabled,
            onCheckedChange = { viewModel.setNightModeEnabled(it) },
            accentColor = colors.accent
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSwitchRow(
            label = "Auto-activate by time",
            description = "Automatically enable night mode between the times below",
            checked = autoNightEnabled,
            onCheckedChange = { viewModel.setAutoNightEnabled(it) },
            accentColor = colors.accent
        )

        if (autoNightEnabled) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("From", fontSize = 14.sp, color = colors.textMuted)
                TimePickerButton(nightStartHour, nightStartMinute, colors.accent) { h, m ->
                    viewModel.setNightStartTime(h, m)
                }
                Text("To", fontSize = 14.sp, color = colors.textMuted)
                TimePickerButton(nightEndHour, nightEndMinute, colors.accent) { h, m ->
                    viewModel.setNightEndTime(h, m)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(color = colors.cardBorder)

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("ABOUT", colors.accent)

        Spacer(modifier = Modifier.height(8.dp))

        Text("MotoSpeedo", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
        Text("Version ${BuildConfig.VERSION_NAME}", fontSize = 14.sp, color = colors.textMuted)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "A glanceable GPS speedometer for motorcycle riders. Designed for handlebar-mounted phones with large, high-contrast readouts.",
            fontSize = 14.sp,
            color = colors.textSecondary,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun SectionHeader(title: String, accentColor: Color) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = accentColor,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color
) {
    val colors = MotoSpeedoThemeAccessor.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(label, fontSize = 15.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
            Text(description, fontSize = 12.sp, color = colors.textMuted, lineHeight = 16.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.textPrimary,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = colors.textMuted,
                uncheckedTrackColor = colors.cardBorder
            )
        )
    }
}

@Composable
private fun TimePickerButton(hour: Int, minute: Int, accentColor: Color, onTimeSelected: (Int, Int) -> Unit) {
    val context = LocalContext.current
    TextButton(onClick = {
        TimePickerDialog(context, { _, h, m -> onTimeSelected(h, m) }, hour, minute, true).show()
    }) {
        Text(
            text = String.format("%02d:%02d", hour, minute),
            color = accentColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
