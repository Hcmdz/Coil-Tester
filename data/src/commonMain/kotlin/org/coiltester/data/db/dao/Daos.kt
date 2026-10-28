package org.coiltester.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.coiltester.data.db.entity.*

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE category = :category ORDER BY name")
    fun getByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getById(id: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' LIMIT 50")
    fun search(query: String): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT category FROM products ORDER BY category")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Transaction
    @Query("SELECT * FROM products WHERE id IN (:ids)")
    fun getByIds(ids: List<String>): Flow<List<ProductEntity>>

    @Query("DELETE FROM products")
    suspend fun clearAll()
}

@Dao
interface SparePartDao {
    @Query("SELECT * FROM spare_parts WHERE productId = :productId")
    fun getByProduct(productId: String): Flow<List<SparePartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(parts: List<SparePartEntity>)

    @Query("DELETE FROM spare_parts")
    suspend fun clearAll()
}

@Dao
interface TroubleshootingDao {
    @Query("SELECT * FROM troubleshooting_guides WHERE productId = :productId")
    fun getByProduct(productId: String): Flow<TroubleshootingGuideEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(guides: List<TroubleshootingGuideEntity>)

    @Query("DELETE FROM troubleshooting_guides")
    suspend fun clearAll()
}

@Dao
interface RefrigerantDao {
    @Query("SELECT * FROM refrigerants ORDER BY name")
    fun getAll(): Flow<List<RefrigerantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(refrigerants: List<RefrigerantEntity>)

    @Query("DELETE FROM refrigerants")
    suspend fun clearAll()
}

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns WHERE validFrom <= :now AND validTo >= :now ORDER BY validFrom")
    fun getActive(now: Long): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM campaigns ORDER BY validFrom DESC")
    fun getAll(): Flow<List<CampaignEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(campaigns: List<CampaignEntity>)

    @Query("DELETE FROM campaigns")
    suspend fun clearAll()
}

@Dao
interface DbInfoDao {
    @Query("SELECT * FROM db_info WHERE key = :key")
    fun get(key: String): Flow<DbInfoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(info: DbInfoEntity)

    @Query("DELETE FROM db_info")
    suspend fun clearAll()
}