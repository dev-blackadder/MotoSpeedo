package com.example.motospeedo.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motospeedo.ui.theme.MotoSpeedoThemeAccessor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(permissionsState: MultiplePermissionsState, onBack: () -> Unit = {}) {
    val colors = MotoSpeedoThemeAccessor.colors
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("\u2190 Back", color = colors.textDark, fontSize = 14.sp)
        }
        Text(
            text = "MotoSpeedo",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accentBright
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Location permission is required to display your speed, heading, and trip distance.",
            fontSize = 18.sp,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        if (permissionsState.shouldShowRationale) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "You previously denied location access. MotoSpeedo needs GPS to function as a speedometer.",
                fontSize = 16.sp,
                color = colors.warning,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { permissionsState.launchMultiplePermissionRequest() },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent
            )
        ) {
            Text(
                text = "GRANT LOCATION ACCESS",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        ) {
            Text(
                text = "Open App Settings",
                fontSize = 16.sp,
                color = colors.textMuted
            )
        }
    }
}
