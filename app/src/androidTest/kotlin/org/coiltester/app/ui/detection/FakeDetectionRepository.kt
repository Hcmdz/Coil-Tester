package org.coiltester.app.ui.detection

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.coiltester.core.model.CalibrationData
import org.coiltester.core.model.DetectionConfig
import org.coiltester.core.model.DetectionError
import org.coiltester.core.model.MagneticFieldReading
import org.coiltester.core.model.Result
import org.coiltester.core.repository.DetectionRepository

class FakeDetectionRepository : DetectionRepository {
    override val magneticField: Flow<Result<MagneticFieldReading, DetectionError>> = emptyFlow()

    override fun startSampling(config: DetectionConfig): Result<Unit, DetectionError> = Result.Ok(Unit)

    override fun stopSampling(): Result<Unit, DetectionError> = Result.Ok(Unit)

    override fun calibrate(): Result<CalibrationData, DetectionError> =
        Result.Ok(CalibrationData())

    override fun isSensorAvailable(): Boolean = true

    override fun getSupportedSamplingRates(): List<Int> = listOf(50, 100, 200)

    override fun injectTestReading(acMagnitude: Double, dcMagnitude: Double) = Unit

    override fun setTestMode(enabled: Boolean) = Unit
}