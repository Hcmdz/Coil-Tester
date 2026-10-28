package org.coiltester.core.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.coiltester.core.model.*
import org.coiltester.core.repository.*

/**
 * Extension for Result to map errors
 */
inline fun <T, E, E2> Result<T, E>.mapError(crossinline transform: (E) -> E2): Result<T, E2> = when (this) {
    is Result.Ok -> this
    is Result.Err -> Result.Err(transform(error))
}

/**
 * Calibration use case - single responsibility
 */
class CalibrationUseCase(
    private val detectionRepository: DetectionRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Result<CalibrationData, UseCaseError> {
        val calibrationResult = detectionRepository.calibrate()
            .mapError { UseCaseError.DetectionErr(it) }
        return when (calibrationResult) {
            is Result.Ok -> {
                val settingsResult = settingsRepository.updateSettings { it.copy(lastCalibrationAt = System.currentTimeMillis()) }
                    .mapError { UseCaseError.RepositoryErr(it) }
                when (settingsResult) {
                    is Result.Ok -> Result.Ok(calibrationResult.value)
                    is Result.Err -> Result.Err(settingsResult.error)
                }
            }
            is Result.Err -> Result.Err(calibrationResult.error)
        }
    }
}

/**
 * Sensitivity configuration use case
 */
class SetSensitivityUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(sensitivity: Sensitivity): Result<Unit, UseCaseError> {
        return settingsRepository.updateSettings { it.copy(sensitivity = sensitivity) }
            .mapError { UseCaseError.RepositoryErr(it) }
    }
}

/**
 * Language change use case
 */
class SetLanguageUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(language: String): Result<Unit, UseCaseError> {
        return if (language in setOf("fr", "en", "ar")) {
            settingsRepository.updateSettings { it.copy(language = language) }
                .mapError { UseCaseError.RepositoryErr(it) }
        } else {
            Result.Err(UseCaseError.ValidationFailed("language", "Unsupported language: $language"))
        }
    }
}

/**
 * Theme change use case
 */
class SetThemeUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(themeMode: ThemeMode): Result<Unit, UseCaseError> {
        return settingsRepository.updateSettings { it.copy(themeMode = themeMode) }
            .mapError { UseCaseError.RepositoryErr(it) }
    }
}

/**
 * Detection configuration use case - reads from settings and builds config
 */
class ConfigureDetectionUseCase(
    private val detectionRepository: DetectionRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Result<Unit, UseCaseError> {
        val settings = settingsRepository.settings.first()
        val config = DetectionConfig.fromSettings(settings)
        val result = detectionRepository.startSampling(config)
            .mapError { UseCaseError.DetectionErr(it) }
        if (result is Result.Ok) {
            settingsRepository.updateSettings { it.copy(lastCalibrationAt = null) }
        }
        return result
    }
}

/**
 * Stop detection use case
 */
class StopDetectionUseCase(
    private val detectionRepository: DetectionRepository
) {
    suspend operator fun invoke(): Result<Unit, UseCaseError> {
        return detectionRepository.stopSampling()
            .mapError { UseCaseError.DetectionErr(it) }
    }
}

/**
 * Advanced options toggle
 */
class SetAdvancedOptionsUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit, UseCaseError> {
        return settingsRepository.updateSettings { it.copy(advancedOptionsEnabled = enabled) }
            .mapError { UseCaseError.RepositoryErr(it) }
    }
}

/**
 * Custom AC threshold
 */
class SetAcThresholdUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(value: Double): Result<Unit, UseCaseError> {
        if (value.isNaN() || value < 0.0) {
            return Result.Err(UseCaseError.ValidationFailed("acThreshold", "Invalid AC threshold"))
        }
        return settingsRepository.updateSettings { it.copy(acThreshold = value) }
            .mapError { UseCaseError.RepositoryErr(it) }
    }
}

/**
 * Custom DC threshold
 */
class SetDcThresholdUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(value: Double): Result<Unit, UseCaseError> {
        if (value.isNaN() || value < 0.0) {
            return Result.Err(UseCaseError.ValidationFailed("dcThreshold", "Invalid DC threshold"))
        }
        return settingsRepository.updateSettings { it.copy(dcThreshold = value) }
            .mapError { UseCaseError.RepositoryErr(it) }
    }
}

/**
 * Frequency band selection (50/60 Hz)
 */
class SetFrequencyBandUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(hz: Int): Result<Unit, UseCaseError> {
        if (hz != 50 && hz != 60) {
            return Result.Err(UseCaseError.ValidationFailed("bandPassFreqHz", "Unsupported frequency: $hz"))
        }
        return settingsRepository.updateSettings { it.copy(bandPassFreqHz = hz) }
            .mapError { UseCaseError.RepositoryErr(it) }
    }
}

/**
 * Sound volume (0.0..1.0)
 */
class SetSoundVolumeUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(volume: Float): Result<Unit, UseCaseError> {
        val clamped = volume.coerceIn(0f, 1f)
        return settingsRepository.updateSettings { it.copy(soundVolume = clamped) }
            .mapError { UseCaseError.RepositoryErr(it) }
    }
}
