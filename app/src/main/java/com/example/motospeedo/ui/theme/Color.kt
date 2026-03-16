package com.example.motospeedo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class MotoSpeedoColors(
    val accent: Color,
    val accentBright: Color,
    val accentDim: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val cardBorder: Color,
    val cardBorderActive: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDark: Color,
    val warning: Color,
    val error: Color,
    val errorBg: Color,
    val gpsGood: Color,
    val gpsWeak: Color,
    val gpsNoSignal: Color,
    val pauseColor: Color,
    val stopColor: Color,
    val overlayBg: Color,
)

val DayColors = MotoSpeedoColors(
    accent = Color(0xFF00CC66),
    accentBright = Color(0xFF00FF88),
    accentDim = Color(0xFF1A3D28),
    background = Color.Black,
    surface = Color(0xFF0D0D0D),
    surfaceElevated = Color(0xFF111111),
    cardBorder = Color(0xFF2A2A2A),
    cardBorderActive = Color(0xFF00CC66),
    textPrimary = Color.White,
    textSecondary = Color(0xFFB0B0B0),
    textMuted = Color(0xFF808080),
    textDark = Color(0xFF606060),
    warning = Color(0xFFFFCC00),
    error = Color(0xFFFF4444),
    errorBg = Color(0xFFCC3333),
    gpsGood = Color(0xFF00CC66),
    gpsWeak = Color(0xFFFFCC00),
    gpsNoSignal = Color(0xFFCC3333),
    pauseColor = Color(0xFFFFCC00),
    stopColor = Color(0xFFFF6666),
    overlayBg = Color(0xCC0D0D0D),
)

val NightColors = DayColors.copy(
    accent = Color(0xFFFF8800),
    accentBright = Color(0xFFFF9900),
    accentDim = Color(0xFF3D2A1A),
    cardBorderActive = Color(0xFFFF8800),
    gpsGood = Color(0xFFFF8800),
)

val LocalMotoSpeedoColors = staticCompositionLocalOf { DayColors }