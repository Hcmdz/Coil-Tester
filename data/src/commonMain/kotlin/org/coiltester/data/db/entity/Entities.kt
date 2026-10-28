package org.coiltester.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Entity(
    tableName = "products",
    indices = [Index("category"), Index("name")],
    primaryKeys = ["id"]
)
@Serializable
data class ProductEntity(
    val id: String,
    val name: String,
    val category: String,
    val description: String?,
    val specsJson: String,
    val imageAssetPath: String?,
    val sparePartIds: String, // CSV
    val troubleshootingGuideId: String?,
    val updatedAt: Long
)

@Entity(
    tableName = "spare_parts",
    indices = [Index("productId"), Index("partNumber")],
    primaryKeys = ["id"]
)
@Serializable
data class SparePartEntity(
    val id: String,
    val productId: String,
    val partNumber: String,
    val description: String,
    val compatibility: String?,
    val imageAssetPath: String?
)

@Entity(
    tableName = "troubleshooting_guides",
    primaryKeys = ["id"]
)
@Serializable
data class TroubleshootingGuideEntity(
    val id: String,
    val productId: String,
    val stepsJson: String,
    val updatedAt: Long
)

@Entity(
    tableName = "refrigerants",
    indices = [Index("name")],
    primaryKeys = ["name"]
)
@Serializable
data class RefrigerantEntity(
    val name: String,
    val gwp: Int,
    val safetyClass: String,
    val pressureTempChartJson: String,
    val applications: String, // CSV
    val phaseOutDate: String?
)

@Entity(
    tableName = "campaigns",
    indices = [Index("validFrom"), Index("validTo")],
    primaryKeys = ["id"]
)
@Serializable
data class CampaignEntity(
    val id: String,
    val title: String,
    val description: String,
    val validFrom: Long,
    val validTo: Long,
    val imageAssetPath: String?,
    val linkUrl: String?
)

/** Database version info for migration tracking */
@Entity(tableName = "db_info", primaryKeys = ["key"])
@Serializable
data class DbInfoEntity(
    val key: String, // "version" or "update_date_tick"
    val value: Long
)