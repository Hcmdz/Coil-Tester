package org.coiltester.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.coiltester.app.platform.HapticService
import org.coiltester.app.platform.SoundService
import org.coiltester.core.model.*
import org.coiltester.core.repository.DetectionRepository
import org.coiltester.core.repository.SettingsRepository
import org.coiltester.core.sensor.DetectionEngine
import org.coiltester.core.usecase.*

class DetectionViewModel(
    private val detectionRepository: DetectionRepository,
    private val configureDetectionUseCase: ConfigureDetectionUseCase,
    private val stopDetectionUseCase: StopDetectionUseCase,
    private val calibrationUseCase: CalibrationUseCase,
    private val setSensitivityUseCase: SetSensitivityUseCase,
    private val soundService: SoundService,
    private val hapticService: HapticService,
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope,
) : ViewModel() {
    private val intentChannel = Channel<DetectionIntent>(Channel.UNLIMITED)

    private var engine: DetectionEngine? = null

    private val _state = MutableStateFlow(DetectionState())
    val state: StateFlow<DetectionState> = _state

    init {
        observeIntents()
        observeSensor()
        observeSettingsChanges()
        scope.launch {
            kotlinx.coroutines.delay(100)
            if (!_state.value.isDetecting) {
                sendIntent(DetectionIntent.Start(_state.value.config))
            }
        }
    }

    fun sendIntent(intent: DetectionIntent) {
        intentChannel.trySend(intent)
    }

    private fun observeIntents() {
        scope.launch {
            intentChannel.consumeEach { intent ->
                when (intent) {
                    is DetectionIntent.Start -> onStart()
                    is DetectionIntent.Stop -> onStop()
                    is DetectionIntent.Calibrate -> onCalibrate()
                    is DetectionIntent.SetSensitivity -> onSetSensitivity(intent.sensitivity)
                    is DetectionIntent.ToggleHaptic -> onToggleHaptic(intent.enabled)
                    is DetectionIntent.ToggleSound -> onToggleSound(intent.enabled)
                    is DetectionIntent.ToggleDistanceGuide -> onToggleDistanceGuide(intent.enabled)
                }
            }
        }
    }

    private fun observeSensor() {
        scope.launch {
            detectionRepository.magneticField
                .onEach { result ->
                    result.onOk { reading ->
                        val eng = engine ?: return@onOk
                        val prevStatus = _state.value.status
                        eng.onSensorReading(reading)
                        val er = eng.state.value
                        _state.update {
                            it.copy(
                                status = er.status,
                                fieldReading = er.fieldReading,
                                detectionType = er.detectionType,
                                confidence = er.confidence,
                                config = er.config,
                                calibration = er.calibration,
                                calibrationAccuracy = reading.accuracy.toCalibrationAccuracy(),
                                lastAcMagnitude = er.acRms,
                                lastDcMagnitude = er.dcMean,
                                error = er.error,
                            )
                        }
                        if (prevStatus != er.status) {
                            when (er.status) {
                                DetectionStatus.CONFIRMED_AC -> {
                                    soundService.playDetectionSound(DetectionType.AC)
                                    hapticService.triggerDetectionFeedback(DetectionType.AC)
                                }
                                DetectionStatus.CONFIRMED_DC -> {
                                    soundService.playDetectionSound(DetectionType.DC)
                                    hapticService.triggerDetectionFeedback(DetectionType.DC)
                                }
                                DetectionStatus.ERROR -> {
                                    soundService.playErrorSound()
                                    hapticService.triggerErrorFeedback()
                                }
                                else -> {}
                            }
                        }
                    }.onErr { error ->
                        _state.update { it.copy(error = null, status = DetectionStatus.ERROR) }
                        soundService.playErrorSound()
                    }
                }
                .launchIn(scope)
        }
    }

    private fun observeSettingsChanges() {
        scope.launch {
            settingsRepository.settings
                .onEach { settings ->
                    // Keep feedback services in sync with persisted settings.
                    soundService.enabled = settings.soundEnabled
                    soundService.volume = settings.soundVolume
                    hapticService.enabled = settings.hapticEnabled
                    val newConfig = DetectionConfig.fromSettings(settings)
                    val currentConfig = _state.value.config
                    if (currentConfig != newConfig) {
                        _state.update { it.copy(config = newConfig) }
                        if (_state.value.isDetecting) {
                            engine = DetectionEngine(newConfig, _state.value.calibration ?: CalibrationData())
                        }
                    }
                }
                .launchIn(scope)
        }
    }

    private fun onStart() {
        val config = _state.value.config
        if (_state.value.isDetecting) {
            return
        }
        engine = DetectionEngine(config, CalibrationData())
        scope.launch {
            val result = configureDetectionUseCase()
            result
                .onOk { _state.update { it.copy(status = DetectionStatus.DETECTING) } }
                .onErr {
                        error ->
                    _state.update { it.copy(error = null, status = DetectionStatus.ERROR) }
                    soundService.playErrorSound()
                    hapticService.triggerErrorFeedback()
                }
        }
    }

    private fun onStop() {
        engine = null
        scope.launch {
            val result = stopDetectionUseCase()
            result
                .onOk {
                    _state.update {
                        it.copy(
                            status = DetectionStatus.IDLE,
                            fieldReading = null,
                            detectionType = null,
                            confidence = 0.0,
                            lastAcMagnitude = 0.0,
                            lastDcMagnitude = 0.0,
                        )
                    }
                }
                .onErr { error -> _state.update { it.copy(error = null) } }
        }
    }

    private fun onCalibrate() {
        engine = DetectionEngine(_state.value.config, CalibrationData())
        _state.update { it.copy(status = DetectionStatus.CALIBRATING) }
        scope.launch {
            calibrationUseCase()
                .onOk { calibration ->
                    _state.update { it.copy(calibration = calibration, status = DetectionStatus.DETECTING) }
                    soundService.playCalibrationSound()
                    hapticService.triggerCalibrationFeedback()
                }
                .onErr { error ->
                    _state.update { it.copy(error = null, status = DetectionStatus.ERROR) }
                    soundService.playErrorSound()
                    hapticService.triggerErrorFeedback()
                }
        }
    }

    private fun onSetSensitivity(sensitivity: Sensitivity) {
        scope.launch {
            setSensitivityUseCase(sensitivity)
                .onOk {
                    val newConfig = DetectionConfig.fromSensitivity(sensitivity)
                    _state.update { it.copy(config = newConfig) }
                }
        }
    }

    private fun onToggleHaptic(enabled: Boolean) {
        scope.launch {
            settingsRepository.updateSettings { it.copy(hapticEnabled = enabled) }
        }
    }

    private fun onToggleSound(enabled: Boolean) {
        scope.launch {
            settingsRepository.updateSettings { it.copy(soundEnabled = enabled) }
        }
    }

    private fun onToggleDistanceGuide(enabled: Boolean) {
        _state.update { it.copy(config = it.config.copy(distanceGuideEnabled = enabled)) }
    }
}

sealed interface DetectionIntent {
    data class Start(val config: DetectionConfig) : DetectionIntent

    object Stop : DetectionIntent

    object Calibrate : DetectionIntent

    data class SetSensitivity(val sensitivity: Sensitivity) : DetectionIntent

    data class ToggleHaptic(val enabled: Boolean) : DetectionIntent

    data class ToggleSound(val enabled: Boolean) : DetectionIntent

    data class ToggleDistanceGuide(val enabled: Boolean) : DetectionIntent
}

data class DetectionState(
    val status: DetectionStatus = DetectionStatus.IDLE,
    val fieldReading: MagneticFieldReading? = null,
    val detectionType: DetectionType? = null,
    val confidence: Double = 0.0,
    val config: DetectionConfig = DetectionConfig(),
    val calibration: CalibrationData? = null,
    val calibrationAccuracy: CalibrationAccuracy = CalibrationAccuracy.MEDIUM,
    val lastAcMagnitude: Double = 0.0,
    val lastDcMagnitude: Double = 0.0,
    val error: DetectionError? = null,
) {
    val isDetecting: Boolean get() = status != DetectionStatus.IDLE && status != DetectionStatus.ERROR
    val showDistanceGuide: Boolean get() = config.distanceGuideEnabled && status != DetectionStatus.IDLE
}

private fun Int.toCalibrationAccuracy(): org.coiltester.core.model.CalibrationAccuracy =
    when (this) {
        0 -> org.coiltester.core.model.CalibrationAccuracy.UNCALIBRATED
        1 -> org.coiltester.core.model.CalibrationAccuracy.LOW
        2 -> org.coiltester.core.model.CalibrationAccuracy.MEDIUM
        3 -> org.coiltester.core.model.CalibrationAccuracy.HIGH
        else -> org.coiltester.core.model.CalibrationAccuracy.MEDIUM
    }
