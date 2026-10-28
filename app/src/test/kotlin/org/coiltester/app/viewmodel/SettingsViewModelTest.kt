package org.coiltester.app.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.coiltester.core.model.AppSettings
import org.coiltester.core.model.CalibrationData
import org.coiltester.core.model.DetectionConfig
import org.coiltester.core.model.DetectionError
import org.coiltester.core.model.MagneticFieldReading
import org.coiltester.core.model.RepositoryError
import org.coiltester.core.model.Result
import org.coiltester.core.repository.DetectionRepository
import org.coiltester.core.repository.SettingsRepository
import org.coiltester.core.usecase.SetAcThresholdUseCase
import org.coiltester.core.usecase.SetAdvancedOptionsUseCase
import org.coiltester.core.usecase.SetDcThresholdUseCase
import org.coiltester.core.usecase.SetFrequencyBandUseCase
import org.coiltester.core.usecase.SetLanguageUseCase
import org.coiltester.core.usecase.SetSensitivityUseCase
import org.coiltester.core.usecase.SetSoundVolumeUseCase
import org.coiltester.core.usecase.SetThemeUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FakeSettingsRepository : SettingsRepository {
    private val _settings = MutableStateFlow(AppSettings())
    override val settings: StateFlow<AppSettings> = _settings

    override suspend fun getSettings(): AppSettings = _settings.value

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings): Result<Unit, RepositoryError> {
        _settings.value = transform(_settings.value)
        return Result.Ok(Unit)
    }

    override suspend fun resetCalibration(): Result<Unit, RepositoryError> = Result.Ok(Unit)
}

class FakeSensorProvider : DetectionRepository {
    override val magneticField: kotlinx.coroutines.flow.Flow<Result<MagneticFieldReading, DetectionError>> = emptyFlow()

    override fun isSensorAvailable(): Boolean = true

    override fun getSupportedSamplingRates(): List<Int> = listOf(10, 20, 50, 64, 100)

    override fun startSampling(config: DetectionConfig): Result<Unit, DetectionError> = Result.Ok(Unit)

    override fun stopSampling(): Result<Unit, DetectionError> = Result.Ok(Unit)

    override fun calibrate(): Result<CalibrationData, DetectionError> = Result.Ok(CalibrationData())

    override fun injectTestReading(
        acMagnitude: Double,
        dcMagnitude: Double,
    ) {
        // no-op
    }

    override fun setTestMode(enabled: Boolean) { }
}

class SettingsViewModelTest {
    @Test
    fun `SetLanguage updates settings language`() =
        runTest {
            val repo = FakeSettingsRepository()
            val sensorProvider = FakeSensorProvider()
            val scope = TestScope(StandardTestDispatcher(testScheduler))
            val vm =
                SettingsViewModel(
                    repo,
                    SetSensitivityUseCase(repo),
                    SetLanguageUseCase(repo),
                    SetThemeUseCase(repo),
                    SetAdvancedOptionsUseCase(repo),
                    SetAcThresholdUseCase(repo),
                    SetDcThresholdUseCase(repo),
                    SetFrequencyBandUseCase(repo),
                    SetSoundVolumeUseCase(repo),
                    sensorProvider,
                    scope,
                )
            vm.sendIntent(SettingsIntent.SetLanguage("en"))
            // allow coroutine to run
            scope.testScheduler.advanceUntilIdle()
            assertEquals("en", repo.settings.value.language)
        }
}
