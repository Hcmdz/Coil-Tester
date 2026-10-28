package org.coiltester.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.coiltester.core.model.*
import org.coiltester.core.repository.DetectionRepository
import org.coiltester.core.repository.SettingsRepository
import org.coiltester.core.usecase.*

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val setSensitivityUseCase: SetSensitivityUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val setAdvancedOptionsUseCase: SetAdvancedOptionsUseCase,
    private val setAcThresholdUseCase: SetAcThresholdUseCase,
    private val setDcThresholdUseCase: SetDcThresholdUseCase,
    private val setFrequencyBandUseCase: SetFrequencyBandUseCase,
    private val setSoundVolumeUseCase: SetSoundVolumeUseCase,
    private val sensorProvider: DetectionRepository,
    private val scope: kotlinx.coroutines.CoroutineScope,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    init {
        scope.launch {
            settingsRepository.settings.collect { settings ->
                _state.value =
                    SettingsState(
                        settings = settings,
                        availableLanguages =
                            listOf(
                                LanguageOption("fr", "Français"),
                                LanguageOption("en", "English"),
                                LanguageOption("ar", "العربية"),
                            ),
                    )
            }
        }
    }

    fun sendIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetSensitivity -> scope.launch { setSensitivityUseCase(intent.s) }
            is SettingsIntent.SetLanguage -> scope.launch { setLanguageUseCase(intent.lang) }
            is SettingsIntent.SetTheme -> scope.launch { setThemeUseCase(intent.mode) }
            is SettingsIntent.SetAdvancedOptions -> scope.launch { setAdvancedOptionsUseCase(intent.enabled) }
            is SettingsIntent.SetAcThreshold -> scope.launch { setAcThresholdUseCase(intent.value) }
            is SettingsIntent.SetDcThreshold -> scope.launch { setDcThresholdUseCase(intent.value) }
            is SettingsIntent.SetFrequencyBand -> scope.launch { setFrequencyBandUseCase(intent.hz) }
            is SettingsIntent.SetSoundVolume -> scope.launch { setSoundVolumeUseCase(intent.volume) }
            is SettingsIntent.ToggleHaptic ->
                scope.launch {
                    settingsRepository.updateSettings { it.copy(hapticEnabled = intent.enabled) }
                }
            is SettingsIntent.ToggleSound ->
                scope.launch {
                    settingsRepository.updateSettings { it.copy(soundEnabled = intent.enabled) }
                }
            is SettingsIntent.ToggleDistanceGuide ->
                scope.launch {
                    settingsRepository.updateSettings { it.copy(distanceGuideEnabled = intent.enabled) }
                }
            is SettingsIntent.ResetCalibration -> scope.launch { settingsRepository.resetCalibration() }
            is SettingsIntent.Calibrate ->
                scope.launch {
                    val result = sensorProvider.calibrate()
                    if (result is org.coiltester.core.model.Result.Ok) {
                        settingsRepository.updateSettings { it.copy(lastCalibrationAt = System.currentTimeMillis()) }
                    }
                }
            is SettingsIntent.InjectTestAc -> scope.launch { sensorProvider.injectTestReading(intent.acMagnitude, 0.0) }
            is SettingsIntent.InjectTestDc -> scope.launch { sensorProvider.injectTestReading(0.0, intent.dcMagnitude) }
        }
    }
}

sealed interface SettingsIntent {
    data class SetSensitivity(val s: Sensitivity) : SettingsIntent

    data class SetLanguage(val lang: String) : SettingsIntent

    data class SetTheme(val mode: ThemeMode) : SettingsIntent

    data class SetAdvancedOptions(val enabled: Boolean) : SettingsIntent

    data class SetAcThreshold(val value: Double) : SettingsIntent

    data class SetDcThreshold(val value: Double) : SettingsIntent

    data class SetFrequencyBand(val hz: Int) : SettingsIntent

    data class SetSoundVolume(val volume: Float) : SettingsIntent

    data class ToggleHaptic(val enabled: Boolean) : SettingsIntent

    data class ToggleSound(val enabled: Boolean) : SettingsIntent

    data class ToggleDistanceGuide(val enabled: Boolean) : SettingsIntent

    object ResetCalibration : SettingsIntent

    object Calibrate : SettingsIntent

    data class InjectTestAc(val acMagnitude: Double) : SettingsIntent

    data class InjectTestDc(val dcMagnitude: Double) : SettingsIntent
}

data class SettingsState(
    val settings: AppSettings = AppSettings(),
    val availableLanguages: List<LanguageOption> =
        listOf(
            LanguageOption("fr", "Français"),
            LanguageOption("en", "English"),
            LanguageOption("ar", "العربية"),
        ),
)

data class LanguageOption(val code: String, val label: String)
