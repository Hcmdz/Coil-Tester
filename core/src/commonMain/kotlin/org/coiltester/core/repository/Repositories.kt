package org.coiltester.core.repository

import kotlinx.coroutines.flow.Flow
import org.coiltester.core.model.*

/**
 * Product catalog repository
 */
interface ProductRepository {
    fun getByCategory(category: String): Flow<Result<List<Product>, RepositoryError>>
    fun getById(id: String): Flow<Result<Product?, RepositoryError>>
    fun search(query: String): Flow<Result<List<Product>, RepositoryError>>
    fun getAllCategories(): Flow<Result<List<String>, RepositoryError>>
    fun getSpareParts(productId: String): Flow<Result<List<SparePart>, RepositoryError>>
    fun getTroubleshootingGuide(productId: String): Flow<Result<TroubleshootingGuide?, RepositoryError>>
}

/**
 * Reference data repository (refrigerants, campaigns)
 */
interface ReferenceRepository {
    fun getRefrigerants(): Flow<Result<List<Refrigerant>, RepositoryError>>
    fun getActiveCampaigns(): Flow<Result<List<Campaign>, RepositoryError>>
}

/**
 * Settings repository (DataStore-backed)
 */
interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    suspend fun updateSettings(transform: (AppSettings) -> AppSettings): Result<Unit, RepositoryError>
    suspend fun resetCalibration(): Result<Unit, RepositoryError>
}

/**
 * Detection / sensor repository
 */
interface DetectionRepository {
    /** Magnetometer readings (x, y, z in µT) at configured rate */
    val magneticField: Flow<Result<MagneticFieldReading, DetectionError>>

    /** Start sensor sampling with config */
    fun startSampling(config: DetectionConfig): Result<Unit, DetectionError>

    /** Stop sensor sampling */
    fun stopSampling(): Result<Unit, DetectionError>

    /** One-shot calibration */
    fun calibrate(): Result<CalibrationData, DetectionError>

    /** Check if sensor is available */
    fun isSensorAvailable(): Boolean

    /** Get supported sampling rates */
    fun getSupportedSamplingRates(): List<Int>

    /** Inject test reading for AC/DC detection testing */
    fun injectTestReading(acMagnitude: Double, dcMagnitude: Double)

    /** Enable/disable test mode */
    fun setTestMode(enabled: Boolean)
}
