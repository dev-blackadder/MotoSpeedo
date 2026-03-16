package com.example.motospeedo

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.motospeedo.ui.DashboardScreen
import com.example.motospeedo.ui.PermissionScreen
import com.example.motospeedo.ui.RideHistoryScreen
import com.example.motospeedo.ui.SettingsScreen
import com.example.motospeedo.ui.theme.MotoSpeedoTheme
import com.example.motospeedo.viewmodel.DashboardViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: DashboardViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            MotoSpeedoTheme(isNightMode = uiState.isNightMode) {
                val locationPermissions = buildList {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                val permissionsState = rememberMultiplePermissionsState(locationPermissions)

                if (permissionsState.allPermissionsGranted) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onOpenSettings = { navController.navigate("settings") },
                                onOpenHistory = { navController.navigate("history") }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable("history") {
                            RideHistoryScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                    }
                } else {
                    PermissionScreen(permissionsState = permissionsState)
                }
            }
        }
    }
}