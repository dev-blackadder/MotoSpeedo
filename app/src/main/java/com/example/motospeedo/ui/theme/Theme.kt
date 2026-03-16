package com.example.motospeedo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val MotoSpeedoColorScheme = darkColorScheme(
    primary = Color(0xFF00FF88),
    onPrimary = Color.Black,
    secondary = Color(0xFF00CC66),
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF1A1A1A),
    onSurface = Color.White
)

@Composable
fun MotoSpeedoTheme(
    isNightMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val motoSpeedoColors = if (isNightMode) NightColors else DayColors

    CompositionLocalProvider(LocalMotoSpeedoColors provides motoSpeedoColors) {
        MaterialTheme(
            colorScheme = MotoSpeedoColorScheme,
            typography = Typography,
            content = content
        )
    }
}

object MotoSpeedoThemeAccessor {
    val colors: MotoSpeedoColors
        @Composable get() = LocalMotoSpeedoColors.current
}