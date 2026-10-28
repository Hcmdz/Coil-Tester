package org.coiltester.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import org.coiltester.app.di.AppRoute
import org.coiltester.app.ui.detection.MagneticToolScreen
import org.coiltester.app.ui.settings.SettingsScreen
import org.coiltester.app.viewmodel.DetectionViewModel
import org.coiltester.app.viewmodel.SettingsViewModel
import org.coiltester.core.model.DetectionStatus

@Composable
fun AppNavHost(
    navController: NavHostController,
    detectionViewModel: DetectionViewModel,
    settingsViewModel: SettingsViewModel,
) {
    NavHost(navController = navController, startDestination = "detection") {
        composable("detection") {
            val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
            val detectionState by detectionViewModel.state.collectAsStateWithLifecycle()
            val s = settingsState.settings
            val hasHighField =
                detectionState.detectionType != null &&
                    (
                        detectionState.status == DetectionStatus.CONFIRMED_AC ||
                            detectionState.status == DetectionStatus.CONFIRMED_DC
                    )
            val rotation = remember { mutableStateOf(0f) }
            LaunchedEffect(hasHighField) {
                if (hasHighField) {
                    while (true) {
                        delay(16)
                        rotation.value = (rotation.value + 2.88f) % 360f
                    }
                } else {
                    rotation.value = 0f
                }
            }
            MagneticToolScreen(
                advancedOptionsEnabled = s.advancedOptionsEnabled,
                distanceGuideEnabled = s.distanceGuideEnabled,
                soundEnabled = s.soundEnabled,
                hapticEnabled = s.hapticEnabled,
                isDetecting = detectionState.isDetecting,
                acValue = detectionState.lastAcMagnitude,
                dcValue = detectionState.lastDcMagnitude,
                acThreshold = s.acThreshold,
                dcThreshold = s.dcThreshold,
                rotationDegrees = rotation.value,
                calibrationAccuracy = detectionState.calibrationAccuracy,
                onSettingsClick = { navController.navigate("settings") },
                onAdvancedOptionsToggle = { enabled ->
                    settingsViewModel.sendIntent(org.coiltester.app.viewmodel.SettingsIntent.SetAdvancedOptions(enabled))
                },
                onSoundToggle = { enabled ->
                    settingsViewModel.sendIntent(org.coiltester.app.viewmodel.SettingsIntent.ToggleSound(enabled))
                },
                onHapticToggle = { enabled ->
                    settingsViewModel.sendIntent(org.coiltester.app.viewmodel.SettingsIntent.ToggleHaptic(enabled))
                },
                onAcThresholdChange = { v ->
                    settingsViewModel.sendIntent(org.coiltester.app.viewmodel.SettingsIntent.SetAcThreshold(v))
                },
                onDcThresholdChange = { v ->
                    settingsViewModel.sendIntent(org.coiltester.app.viewmodel.SettingsIntent.SetDcThreshold(v))
                },
                onToggleDetection = { isDetecting ->
                    if (isDetecting) {
                        detectionViewModel.sendIntent(org.coiltester.app.viewmodel.DetectionIntent.Stop)
                    } else {
                        detectionViewModel.sendIntent(
                            org.coiltester.app.viewmodel.DetectionIntent.Start(org.coiltester.core.model.DetectionConfig.fromSettings(s)),
                        )
                    }
                },
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigate = { route -> navController.navigate(route.toRouteString()) },
                onBack = { navController.popBackStack() },
            )
        }
    }
}

fun AppRoute.toRouteString(): String =
    when (this) {
        is AppRoute.Detection -> "detection"
        is AppRoute.Settings -> "settings"
        is AppRoute.About -> "about"
    }
