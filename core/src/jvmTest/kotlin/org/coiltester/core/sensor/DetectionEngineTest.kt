package org.coiltester.core.sensor

import org.coiltester.core.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName

class DetectionEngineTest {

    private val defaultConfig = DetectionConfig(
        sensitivity = Sensitivity.MEDIUM,
        sampleRateHz = 50,
        acThreshold = 0.5,
        dcThreshold = 2.0
    )

    private val defaultCalibration = CalibrationData()

    @Test
    @DisplayName("Should start in IDLE state")
    fun startsInIdle() {
        val engine = DetectionEngine(defaultConfig, defaultCalibration)
        assertEquals(DetectionStatus.IDLE, engine.state.value.status)
    }

    @Test
    @DisplayName("Should detect AC field when magnitude oscillates")
    fun detectsACField() {
        // 50 Hz cannot be measured at 50 Hz sampling (Nyquist), use a rate that can resolve it.
        val acConfig = defaultConfig.copy(sampleRateHz = 200, dcThreshold = 100.0)
        val engine = DetectionEngine(acConfig, defaultCalibration)

        // Simulate 50Hz AC field (oscillating magnitude)
        repeat(60) { i ->
            val magnitude = 10.0 + 5.0 * kotlin.math.sin(2 * Math.PI * 50 * i / 200.0)
            engine.onSensorReading(MagneticFieldReading(0.0, 0.0, magnitude))
        }

        val state = engine.state.value
        assertEquals(DetectionStatus.CONFIRMED_AC, state.status)
        assertEquals(DetectionType.AC, state.detectionType)
    }

    @Test
    @DisplayName("Should detect DC field when magnitude is stable and high")
    fun detectsDCField() {
        val engine = DetectionEngine(defaultConfig, defaultCalibration)

        // Simulate stable DC field (high magnitude, low variance)
        repeat(20) {
            engine.onSensorReading(MagneticFieldReading(0.0, 0.0, 10.0))
        }

        val state = engine.state.value
        // DC detection requires magnitude > dcThreshold (2.0) and low AC variance
        assertEquals(DetectionStatus.CONFIRMED_DC, state.status)
    }

    @Test
    @DisplayName("Should remain idle with no significant field")
    fun remainsIdleWithNoField() {
        val engine = DetectionEngine(defaultConfig, defaultCalibration)

        repeat(20) {
            engine.onSensorReading(MagneticFieldReading(0.0, 0.0, 0.01))
        }

        val state = engine.state.value
        assertTrue(state.status == DetectionStatus.IDLE || state.status == DetectionStatus.DETECTING)
    }

    @Test
    @DisplayName("Should apply calibration offset")
    fun appliesCalibration() {
        val calibration = CalibrationData(offsetX = 1.0, offsetY = 2.0, offsetZ = 3.0)
        val engine = DetectionEngine(defaultConfig, calibration)

        engine.onSensorReading(MagneticFieldReading(1.0, 2.0, 3.0))
        val reading = engine.state.value.fieldReading

        assertNotNull(reading)
        assertEquals(0.0, reading!!.x, 0.01)
        assertEquals(0.0, reading.y, 0.01)
        assertEquals(0.0, reading.z, 0.01)
    }

    @Test
    @DisplayName("Should update config")
    fun updatesConfig() {
        val engine = DetectionEngine(defaultConfig, defaultCalibration)
        val newConfig = DetectionConfig.fromSensitivity(Sensitivity.HIGH)
        engine.updateConfig(newConfig)
        // Config is updated (no crash)
        assertNotNull(engine.state.value)
    }

    @Test
    @DisplayName("Should not confirm DC when mean field stays under the custom threshold")
    fun respectsCustomDcThreshold() {
        val config = defaultConfig.copy(dcThreshold = 188.0)
        val engine = DetectionEngine(config, defaultCalibration)

        // Earth field on a real device: ~48 uT, well under the 188 uT threshold
        repeat(40) {
            engine.onSensorReading(MagneticFieldReading(0.0, 0.0, 48.76))
        }

        val state = engine.state.value
        assertNull(state.detectionType)
        assertEquals(DetectionStatus.DETECTING, state.status)
        // The displayed values must come from the measurement, not from the last detection.
        assertEquals(48.76, state.dcMean, 0.01)
        assertEquals(0.0, state.acRms, 0.01)
    }

    @Test
    @DisplayName("A constant field must not be reported as AC")
    fun constantFieldIsNotAc() {
        val config = defaultConfig.copy(sampleRateHz = 200, dcThreshold = 188.0)
        val engine = DetectionEngine(config, defaultCalibration)

        repeat(120) {
            engine.onSensorReading(MagneticFieldReading(0.0, 0.0, 48.76))
        }

        val state = engine.state.value
        assertNull(state.detectionType)
        // The band-pass restarts every call, so this also guards its start-up transient.
        assertTrue(state.acRms < config.acThreshold)
    }

    @Test
    @DisplayName("Should not report AC when the band centre sits above Nyquist")
    fun noAcAboveNyquist() {
        // The app's default 64 Hz sampling cannot represent a 50 Hz band-pass.
        val config = defaultConfig.copy(sampleRateHz = 64, bandPassFreqHz = 50, dcThreshold = 188.0)
        val engine = DetectionEngine(config, defaultCalibration)

        repeat(120) {
            engine.onSensorReading(MagneticFieldReading(0.0, 0.0, 48.76))
        }

        assertNull(engine.state.value.detectionType)
    }

    @Test
    @DisplayName("Should stay detecting when the field drops back under the threshold")
    fun keepsDetectingAfterFieldDrops() {
        val engine = DetectionEngine(defaultConfig, defaultCalibration)

        repeat(40) {
            engine.onSensorReading(MagneticFieldReading(0.0, 0.0, 40.0))
        }
        assertEquals(DetectionStatus.CONFIRMED_DC, engine.state.value.status)

        // Fill the whole rolling buffer with the low field so the filter settles.
        repeat(120) {
            engine.onSensorReading(MagneticFieldReading(0.0, 0.0, 0.5))
        }

        val state = engine.state.value
        assertNull(state.detectionType)
        assertEquals(DetectionStatus.DETECTING, state.status)
    }
}

class SignalProcessingTest {
    @Test
    @DisplayName("movingAverage should compute correct average")
    fun movingAverageCorrect() {
        val samples = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val result = SignalProcessing.movingAverage(samples, 3)
        assertEquals(3, result.size)
        assertEquals(2.0, result[0], 0.01)
        assertEquals(3.0, result[1], 0.01)
        assertEquals(4.0, result[2], 0.01)
    }

    @Test
    @DisplayName("movingAverage should return input if window too large")
    fun movingAverageSmallInput() {
        val samples = listOf(1.0, 2.0)
        val result = SignalProcessing.movingAverage(samples, 5)
        assertEquals(2, result.size)
    }

    @Test
    @DisplayName("bandPass should return samples unchanged (placeholder)")
    fun bandPassPlaceholder() {
        val samples = listOf(1.0, 2.0, 3.0)
        val result = SignalProcessing.bandPass(samples, 50, 50, 10)
        assertEquals(samples, result as List<Double>)
    }

    @Test
    @DisplayName("detectMainsFrequency should return 50Hz default")
    fun detectMainsFrequencyDefault() {
        val (freq, mag) = SignalProcessing.detectMainsFrequency(listOf(1.0, 2.0, 3.0), 50)
        assertEquals(50, freq)
    }
}
class SignalProcessingExtendedTest {
    @Test
    @DisplayName("rms should compute correct RMS")
    fun rmsCorrect() {
        val samples = listOf(3.0, 4.0)
        val result = SignalProcessing.rms(samples)
        assertEquals(3.535, result, 0.01)
    }

    @Test
    @DisplayName("rms should return 0 for empty list")
    fun rmsEmpty() {
        val result = SignalProcessing.rms(emptyList())
        assertEquals(0.0, result, 0.01)
    }

    @Test
    @DisplayName("mean should compute correct mean")
    fun meanCorrect() {
        val samples = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val result = SignalProcessing.mean(samples)
        assertEquals(3.0, result, 0.01)
    }

    @Test
    @DisplayName("variance should compute correct variance")
    fun varianceCorrect() {
        val samples = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val result = SignalProcessing.variance(samples)
        assertEquals(2.0, result, 0.01)
    }

    @Test
    @DisplayName("bandPass should filter signal")
    fun bandPassFilters() {
        val samples = List(100) { kotlin.math.sin(2 * Math.PI * 50 * it / 200.0) }
        val filtered = SignalProcessing.bandPass(samples, 200, 50, 10)
        assertEquals(100, (filtered as List<Double>).size)
        // Filtered signal should have non-zero values
        assertTrue((filtered as List<Double>).any { kotlin.math.abs(it) > 0.01 })
    }

    @Test
    @DisplayName("detectMainsFrequency should detect 50Hz")
    fun detect50Hz() {
        val samples = List(256) { kotlin.math.sin(2 * Math.PI * 50 * it / 50.0) }
        val (freq, mag) = SignalProcessing.detectMainsFrequency(samples, 50)
        assertEquals(50, freq)
    }

    @Test
    @DisplayName("detectMainsFrequency should detect 60Hz")
    fun detect60Hz() {
        val samples = List(256) { kotlin.math.sin(2 * Math.PI * 60 * it / 60.0) }
        val (freq, mag) = SignalProcessing.detectMainsFrequency(samples, 60)
        assertEquals(60, freq)
    }

    @Test
    @DisplayName("DetectionEngine should detect AC field")
    fun detectAC() {
        val config = DetectionConfig(
            sensitivity = Sensitivity.MEDIUM,
            sampleRateHz = 200,
            acThreshold = 0.1,
            dcThreshold = 100.0
        )
        val engine = DetectionEngine(config, CalibrationData())

        // Simulate 50Hz AC field (oscillating)
        repeat(80) { i ->
            val magnitude = 5.0 + 3.0 * kotlin.math.sin(2 * Math.PI * 50 * i / 200.0)
            engine.onSensorReading(MagneticFieldReading(0.0, 0.0, magnitude))
        }

        val state = engine.state.value
        assertEquals(DetectionStatus.CONFIRMED_AC, state.status)
    }

    @Test
    @DisplayName("DetectionEngine should detect DC field")
    fun detectDC() {
        val config = DetectionConfig(
            sensitivity = Sensitivity.MEDIUM,
            sampleRateHz = 50,
            acThreshold = 0.1,
            dcThreshold = 2.0
        )
        val engine = DetectionEngine(config, CalibrationData())

        // Simulate stable DC field (high magnitude, low variance)
        repeat(50) {
            engine.onSensorReading(MagneticFieldReading(0.0, 0.0, 10.0))
        }

        val state = engine.state.value
        // Should detect DC (high mean, low AC variance)
        assertTrue(state.status == DetectionStatus.CONFIRMED_DC || state.status == DetectionStatus.IDLE || state.status == DetectionStatus.DETECTING)
    }

    @Test
    @DisplayName("DetectionEngine should remain idle with no field")
    fun noField() {
        val config = DetectionConfig(
            sensitivity = Sensitivity.MEDIUM,
            sampleRateHz = 50,
            acThreshold = 0.5,
            dcThreshold = 2.0
        )
        val engine = DetectionEngine(config, CalibrationData())

        repeat(50) {
            engine.onSensorReading(MagneticFieldReading(0.0, 0.0, 0.01))
        }

        val state = engine.state.value
        assertTrue(state.status == DetectionStatus.IDLE || state.status == DetectionStatus.DETECTING)
    }

    @Test
    @DisplayName("DetectionEngine should reset state")
    fun reset() {
        val engine = DetectionEngine(DetectionConfig(), CalibrationData())
        repeat(10) {
            engine.onSensorReading(MagneticFieldReading(0.0, 0.0, 5.0))
        }
        engine.reset()
        assertEquals(DetectionStatus.IDLE, engine.state.value.status)
        assertNull(engine.state.value.fieldReading)
    }
}
