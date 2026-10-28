package org.coiltester.core.model

import kotlinx.serialization.Serializable

/**
 * Sensitivity presets for detection
 */
@Serializable
enum class Sensitivity {
    LOW, MEDIUM, HIGH
}

/**
 * Magnetometer calibration accuracy (mirrors Android SensorManager accuracy levels)
 */
@Serializable
enum class CalibrationAccuracy {
    UNCALIBRATED, LOW, MEDIUM, HIGH
}

/**
 * Theme mode for Material 3
 */
@Serializable
enum class ThemeMode {
    LIGHT, DARK, SYSTEM, PURE_BLACK
}

/**
 * Magnetic field reading in microtesla (µT)
 */
@Serializable
data class MagneticFieldReading(
    val x: Double,
    val y: Double,
    val z: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val accuracy: Int = 2 // SENSOR_STATUS_ACCURACY_MEDIUM
) {
    val magnitude: Double
        get() = kotlin.math.sqrt(x * x + y * y + z * z)

    val hasSignificantField: Boolean
        get() = magnitude > 0.1 // µT threshold
}

/**
 * Detection configuration
 */
@Serializable
data class DetectionConfig(
    val sensitivity: Sensitivity = Sensitivity.MEDIUM,
    val sampleRateHz: Int = 200, // must stay above 2x the mains band to resolve it
    val bandPassFreqHz: Int = 50, // 50 or 60 Hz
    val acThreshold: Double = 0.5, // µT
    val dcThreshold: Double = 2.0, // µT
    val distanceGuideEnabled: Boolean = true
) {
    companion object {
        fun fromSensitivity(s: Sensitivity): DetectionConfig = when (s) {
            Sensitivity.LOW -> DetectionConfig(sensitivity = s, acThreshold = 0.5, dcThreshold = 60.0)
            Sensitivity.MEDIUM -> DetectionConfig(sensitivity = s, acThreshold = 0.5, dcThreshold = 120.0)
            Sensitivity.HIGH -> DetectionConfig(sensitivity = s, acThreshold = 1.0, dcThreshold = 200.0)
        }

        fun fromSettings(settings: AppSettings): DetectionConfig {
            val base = fromSensitivity(settings.sensitivity)
            return if (settings.advancedOptionsEnabled) {
                base.copy(
                    bandPassFreqHz = settings.bandPassFreqHz,
                    acThreshold = settings.acThreshold,
                    dcThreshold = settings.dcThreshold
                )
            } else {
                base.copy(bandPassFreqHz = settings.bandPassFreqHz)
            }
        }
    }
}

/**
 * Calibration data for sensor offset/scale correction
 */
@Serializable
data class CalibrationData(
    val offsetX: Double = 0.0,
    val offsetY: Double = 0.0,
    val offsetZ: Double = 0.0,
    val scaleFactor: Double = 1.0,
    val calibratedAt: Long = System.currentTimeMillis()
)

/**
 * Detection result
 */
@Serializable
enum class DetectionType {
    AC, DC
}

@Serializable
enum class DetectionStatus {
    IDLE, CALIBRATING, DETECTING, CONFIRMED_AC, CONFIRMED_DC, ERROR
}

@Serializable
data class DetectionResult(
    val status: DetectionStatus,
    val fieldReading: MagneticFieldReading?,
    val detectionType: DetectionType?,
    val confidence: Double, // 0.0..1.0
    val config: DetectionConfig,
    val calibration: CalibrationData?,
    val error: DetectionError?,
    val acRms: Double = 0.0, // µT, measured AC ripple
    val dcMean: Double = 0.0 // µT, measured DC field level
)

/**
 * Product catalog models
 */
@Serializable
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val description: String?,
    val specs: Map<String, String>,
    val imageAssetPath: String?,
    val sparePartIds: List<String>,
    val troubleshootingGuideId: String?
)

@Serializable
data class SparePart(
    val id: String,
    val productId: String,
    val partNumber: String,
    val description: String,
    val compatibility: String?,
    val imageAssetPath: String?
)

@Serializable
data class TroubleshootingStep(
    val title: String,
    val detail: String,
    val imageAssetPath: String?
)

@Serializable
data class TroubleshootingGuide(
    val id: String,
    val productId: String,
    val steps: List<TroubleshootingStep>
)

@Serializable
data class Refrigerant(
    val name: String,
    val gwp: Int,
    val safetyClass: String,
    val pressureTempChart: List<PressureTempPoint>,
    val applications: List<String>,
    val phaseOutDate: String?
)

@Serializable
data class PressureTempPoint(
    val tempC: Double,
    val pressureBar: Double
)

@Serializable
data class Campaign(
    val id: String,
    val title: String,
    val description: String,
    val validFrom: Long,
    val validTo: Long,
    val imageAssetPath: String?,
    val linkUrl: String?
)

/**
 * App settings (stored in DataStore)
 */
@Serializable
data class AppSettings(
    val sensitivity: Sensitivity = Sensitivity.MEDIUM,
    val language: String = "fr", // "fr" | "en" | "ar"
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val distanceGuideEnabled: Boolean = true,
    val lastCalibrationAt: Long? = null,
    val advancedOptionsEnabled: Boolean = false,
    val acThreshold: Double = 0.5,
    val dcThreshold: Double = 2.0,
    val bandPassFreqHz: Int = 50,
    val soundVolume: Float = 1.0f
) {
    fun copyWith(block: AppSettings.() -> Unit): AppSettings {
        return this.copy().also { it.block() }
    }
}