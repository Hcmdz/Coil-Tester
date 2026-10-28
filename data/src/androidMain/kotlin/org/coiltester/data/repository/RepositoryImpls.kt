package org.coiltester.data.repository

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.coiltester.core.model.*
import org.coiltester.core.repository.*
import org.coiltester.data.db.dao.*
import org.coiltester.data.db.entity.*

/**
 * ProductRepository implementation using Room
 */
class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val sparePartDao: SparePartDao,
    private val troubleshootingDao: TroubleshootingDao
) : ProductRepository {

    override fun getByCategory(category: String): Flow<Result<List<Product>, RepositoryError>> {
        return productDao.getByCategory(category)
            .map { entities ->
                try { Result.Ok(entities.map { toProduct(it) }) }
                catch (e: Exception) { Result.Err(RepositoryError.SerializationError(e.message ?: "decode failed", e.toString())) }
            }
    }

    override fun getById(id: String): Flow<Result<Product?, RepositoryError>> {
        return productDao.getById(id)
            .map { entity ->
                try { Result.Ok(entity?.let { toProduct(it) }) }
                catch (e: Exception) { Result.Err(RepositoryError.SerializationError(e.message ?: "decode failed", e.toString())) }
            }
    }

    override fun search(query: String): Flow<Result<List<Product>, RepositoryError>> {
        return productDao.search(query)
            .map { entities ->
                try { Result.Ok(entities.map { toProduct(it) }) }
                catch (e: Exception) { Result.Err(RepositoryError.SerializationError(e.message ?: "decode failed", e.toString())) }
            }
    }

    override fun getAllCategories(): Flow<Result<List<String>, RepositoryError>> {
        return productDao.getAllCategories()
            .map { Result.Ok(it) }
    }

    override fun getSpareParts(productId: String): Flow<Result<List<SparePart>, RepositoryError>> {
        return sparePartDao.getByProduct(productId)
            .map { entities ->
                try { Result.Ok(entities.map { toSparePart(it) }) }
                catch (e: Exception) { Result.Err(RepositoryError.SerializationError(e.message ?: "decode failed", e.toString())) }
            }
    }

    override fun getTroubleshootingGuide(productId: String): Flow<Result<TroubleshootingGuide?, RepositoryError>> {
        return troubleshootingDao.getByProduct(productId)
            .map { entity ->
                try { Result.Ok(entity?.let { toGuide(it) }) }
                catch (e: Exception) { Result.Err(RepositoryError.SerializationError(e.message ?: "decode failed", e.toString())) }
            }
    }

    private fun toProduct(e: ProductEntity): Product = Product(
        id = e.id,
        name = e.name,
        category = e.category,
        description = e.description,
        specs = kotlinx.serialization.json.Json.decodeFromString(e.specsJson),
        imageAssetPath = e.imageAssetPath,
        sparePartIds = e.sparePartIds.split(",").filter { it.isNotBlank() },
        troubleshootingGuideId = e.troubleshootingGuideId
    )

    private fun toSparePart(e: SparePartEntity): SparePart = SparePart(
        id = e.id,
        productId = e.productId,
        partNumber = e.partNumber,
        description = e.description,
        compatibility = e.compatibility,
        imageAssetPath = e.imageAssetPath
    )

    private fun toGuide(e: TroubleshootingGuideEntity): TroubleshootingGuide = TroubleshootingGuide(
        id = e.id,
        productId = e.productId,
        steps = kotlinx.serialization.json.Json.decodeFromString(e.stepsJson)
    )
}

/**
 * ReferenceRepository implementation
 */
class ReferenceRepositoryImpl(
    private val refrigerantDao: RefrigerantDao,
    private val campaignDao: CampaignDao
) : ReferenceRepository {

    override fun getRefrigerants(): Flow<Result<List<Refrigerant>, RepositoryError>> {
        return refrigerantDao.getAll()
            .map { entities ->
                try { Result.Ok(entities.map { toRefrigerant(it) }) }
                catch (e: Exception) { Result.Err(RepositoryError.SerializationError(e.message ?: "decode failed", e.toString())) }
            }
    }

    override fun getActiveCampaigns(): Flow<Result<List<Campaign>, RepositoryError>> {
        return campaignDao.getActive(System.currentTimeMillis())
            .map { entities ->
                try { Result.Ok(entities.map { toCampaign(it) }) }
                catch (e: Exception) { Result.Err(RepositoryError.SerializationError(e.message ?: "decode failed", e.toString())) }
            }
    }

    private fun toRefrigerant(e: RefrigerantEntity): Refrigerant = Refrigerant(
        name = e.name,
        gwp = e.gwp,
        safetyClass = e.safetyClass,
        pressureTempChart = kotlinx.serialization.json.Json.decodeFromString(e.pressureTempChartJson),
        applications = e.applications.split(",").filter { it.isNotBlank() },
        phaseOutDate = e.phaseOutDate
    )

    private fun toCampaign(e: CampaignEntity): Campaign = Campaign(
        id = e.id,
        title = e.title,
        description = e.description,
        validFrom = e.validFrom,
        validTo = e.validTo,
        imageAssetPath = e.imageAssetPath,
        linkUrl = e.linkUrl
    )
}

/**
 * SettingsRepository implementation using DataStore
 */
class SettingsRepositoryImpl(
    private val dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
) : SettingsRepository {

    // Serializes read-modify-write cycles so concurrent intents cannot drop each other's fields.
    private val writeLock = Mutex()

    override val settings: Flow<AppSettings> = dataStore.data
        .map { prefs ->
            AppSettings(
                sensitivity = prefs[PreferencesKeys.SENSITIVITY]?.let { Sensitivity.valueOf(it) } ?: Sensitivity.MEDIUM,
                language = prefs[PreferencesKeys.LANGUAGE] ?: "fr",
                hapticEnabled = prefs[PreferencesKeys.HAPTIC_ENABLED] ?: true,
                soundEnabled = prefs[PreferencesKeys.SOUND_ENABLED] ?: true,
                themeMode = prefs[PreferencesKeys.THEME_MODE]?.let { name ->
                    ThemeMode.entries.firstOrNull { it.name == name } ?: ThemeMode.SYSTEM
                } ?: ThemeMode.SYSTEM,
                distanceGuideEnabled = prefs[PreferencesKeys.DISTANCE_GUIDE] ?: true,
                lastCalibrationAt = prefs[PreferencesKeys.LAST_CALIBRATION],
                advancedOptionsEnabled = prefs[PreferencesKeys.ADVANCED_OPTIONS] ?: false,
                acThreshold = prefs[PreferencesKeys.AC_THRESHOLD]?.toDoubleOrNull() ?: 0.5,
                dcThreshold = prefs[PreferencesKeys.DC_THRESHOLD]?.toDoubleOrNull() ?: 2.0,
                bandPassFreqHz = prefs[PreferencesKeys.BAND_PASS_FREQ]?.toIntOrNull() ?: 50,
                soundVolume = prefs[PreferencesKeys.SOUND_VOLUME]?.toFloatOrNull() ?: 1.0f
            )
        }
        .catch { emit(AppSettings()) }

    override suspend fun getSettings(): AppSettings = settings.first()

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings): Result<Unit, RepositoryError> {
        return try {
            writeLock.withLock {
                val current = settings.first()
                val updated = transform(current)
                dataStore.edit { prefs ->
                    prefs[PreferencesKeys.SENSITIVITY] = updated.sensitivity.name
                    prefs[PreferencesKeys.LANGUAGE] = updated.language
                    prefs[PreferencesKeys.HAPTIC_ENABLED] = updated.hapticEnabled
                    prefs[PreferencesKeys.SOUND_ENABLED] = updated.soundEnabled
                    prefs[PreferencesKeys.THEME_MODE] = updated.themeMode.name
                    prefs[PreferencesKeys.DISTANCE_GUIDE] = updated.distanceGuideEnabled
                    updated.lastCalibrationAt?.let { prefs[PreferencesKeys.LAST_CALIBRATION] = it }
                    prefs[PreferencesKeys.ADVANCED_OPTIONS] = updated.advancedOptionsEnabled
                    prefs[PreferencesKeys.AC_THRESHOLD] = updated.acThreshold.toString()
                    prefs[PreferencesKeys.DC_THRESHOLD] = updated.dcThreshold.toString()
                    prefs[PreferencesKeys.BAND_PASS_FREQ] = updated.bandPassFreqHz.toString()
                    prefs[PreferencesKeys.SOUND_VOLUME] = updated.soundVolume.toString()
                }
            }
            Result.Ok(Unit)
        } catch (e: Exception) {
            Result.Err(RepositoryError.DbError(e.message ?: "Failed to update settings", e.toString()))
        }
    }

    override suspend fun resetCalibration(): Result<Unit, RepositoryError> {
        return try {
            dataStore.edit { prefs ->
                prefs.remove(PreferencesKeys.LAST_CALIBRATION)
            }
            Result.Ok(Unit)
        } catch (e: Exception) {
            Result.Err(RepositoryError.DbError(e.message ?: "Failed to reset calibration", e.toString()))
        }
    }
}

/**
 * Preferences keys
 */
object PreferencesKeys {
    val SENSITIVITY = androidx.datastore.preferences.core.stringPreferencesKey("sensitivity")
    val LANGUAGE = androidx.datastore.preferences.core.stringPreferencesKey("language")
    val HAPTIC_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("haptic_enabled")
    val SOUND_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("sound_enabled")
    val THEME_MODE = androidx.datastore.preferences.core.stringPreferencesKey("theme_mode")
    val DISTANCE_GUIDE = androidx.datastore.preferences.core.booleanPreferencesKey("distance_guide")
    val LAST_CALIBRATION = androidx.datastore.preferences.core.longPreferencesKey("last_calibration")
    val ADVANCED_OPTIONS = androidx.datastore.preferences.core.booleanPreferencesKey("advanced_options")
    val AC_THRESHOLD = androidx.datastore.preferences.core.stringPreferencesKey("ac_threshold")
    val DC_THRESHOLD = androidx.datastore.preferences.core.stringPreferencesKey("dc_threshold")
    val BAND_PASS_FREQ = androidx.datastore.preferences.core.stringPreferencesKey("band_pass_freq")
    val SOUND_VOLUME = androidx.datastore.preferences.core.stringPreferencesKey("sound_volume")
}
