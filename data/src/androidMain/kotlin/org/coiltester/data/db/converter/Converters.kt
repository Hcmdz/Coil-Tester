package org.coiltester.data.db.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.coiltester.core.model.*

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromMap(map: Map<String, String>?): String? = map?.let { json.encodeToString(it) }

    @TypeConverter
    fun toMap(jsonString: String?): Map<String, String>? = jsonString?.let { runCatching { json.decodeFromString<Map<String, String>>(it) }.getOrNull() }

    @TypeConverter
    fun fromList(list: List<String>?): String? = list?.joinToString(",")

    @TypeConverter
    fun toList(csv: String?): List<String> = csv?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

    @TypeConverter
    fun fromPressureTempChart(chart: List<PressureTempPoint>?): String? = chart?.let { json.encodeToString(it) }

    @TypeConverter
    fun toPressureTempChart(jsonString: String?): List<PressureTempPoint> = jsonString?.let { runCatching { json.decodeFromString<List<PressureTempPoint>>(it) }.getOrNull() } ?: emptyList()

    @TypeConverter
    fun fromTroubleshootingSteps(steps: List<TroubleshootingStep>?): String? = steps?.let { json.encodeToString(it) }

    @TypeConverter
    fun toTroubleshootingSteps(jsonString: String?): List<TroubleshootingStep> = jsonString?.let { runCatching { json.decodeFromString<List<TroubleshootingStep>>(it) }.getOrNull() } ?: emptyList()

    @TypeConverter
    fun fromSensitivity(sensitivity: Sensitivity?): String? = sensitivity?.name

    @TypeConverter
    fun toSensitivity(name: String?): Sensitivity? = name?.let { n -> Sensitivity.values().firstOrNull { it.name == n } }

    @TypeConverter
    fun fromThemeMode(mode: ThemeMode?): String? = mode?.name

    @TypeConverter
    fun toThemeMode(name: String?): ThemeMode? = name?.let { n -> ThemeMode.entries.firstOrNull { it.name == n } }
}