package org.coiltester.core.usecase

import org.coiltester.core.model.*
import org.coiltester.core.repository.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName

class UseCaseTest {

    @Test
    @DisplayName("SetSensitivityUseCase should update settings")
    fun setSensitivity() = runTest {
        val settingsRepo = FakeSettingsRepository()
        val useCase = SetSensitivityUseCase(settingsRepo)

        val result = useCase(Sensitivity.HIGH)
        assertTrue(result.isOk())
        assertEquals(Sensitivity.HIGH, settingsRepo.current.sensitivity)
    }

    @Test
    @DisplayName("SetLanguageUseCase should reject unsupported language")
    fun rejectUnsupportedLanguage() = runTest {
        val settingsRepo = FakeSettingsRepository()
        val useCase = SetLanguageUseCase(settingsRepo)

        val result = useCase("xx")
        assertTrue(result.isErr())
    }

    @Test
    @DisplayName("SetLanguageUseCase should accept supported language")
    fun acceptSupportedLanguage() = runTest {
        val settingsRepo = FakeSettingsRepository()
        val useCase = SetLanguageUseCase(settingsRepo)

        val result = useCase("en")
        assertTrue(result.isOk())
        assertEquals("en", settingsRepo.current.language)
    }

    @Test
    @DisplayName("SetThemeUseCase should update theme")
    fun setTheme() = runTest {
        val settingsRepo = FakeSettingsRepository()
        val useCase = SetThemeUseCase(settingsRepo)

        val result = useCase(ThemeMode.DARK)
        assertTrue(result.isOk())
        assertEquals(ThemeMode.DARK, settingsRepo.current.themeMode)
    }

    @Test
    @DisplayName("StopDetectionUseCase should stop sampling")
    fun stopDetection() = runTest {
        val detectionRepo = FakeDetectionRepository()
        val useCase = StopDetectionUseCase(detectionRepo)

        val result = useCase()
        assertTrue(result.isOk())
    }
}

// Fake implementations for testing
class FakeSettingsRepository : SettingsRepository {
    var current = AppSettings()
    override val settings: kotlinx.coroutines.flow.Flow<AppSettings> = kotlinx.coroutines.flow.flowOf(current)

    override suspend fun getSettings(): AppSettings = current

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings): Result<Unit, RepositoryError> {
        current = transform(current)
        return Result.Ok(Unit)
    }

    override suspend fun resetCalibration(): Result<Unit, RepositoryError> {
        current = current.copy(lastCalibrationAt = null)
        return Result.Ok(Unit)
    }
}

class FakeDetectionRepository : DetectionRepository {
    override val magneticField: kotlinx.coroutines.flow.Flow<Result<MagneticFieldReading, DetectionError>> =
        kotlinx.coroutines.flow.flowOf(Result.Ok(MagneticFieldReading(0.0, 0.0, 0.0)))

    override fun startSampling(config: DetectionConfig): Result<Unit, DetectionError> = Result.Ok(Unit)
    override fun stopSampling(): Result<Unit, DetectionError> = Result.Ok(Unit)
    override fun calibrate(): Result<CalibrationData, DetectionError> = Result.Ok(CalibrationData())
    override fun isSensorAvailable(): Boolean = true
    override fun getSupportedSamplingRates(): List<Int> = listOf(10, 20, 50, 100)
    override fun injectTestReading(acMagnitude: Double, dcMagnitude: Double) = Unit
    override fun setTestMode(enabled: Boolean) = Unit
}