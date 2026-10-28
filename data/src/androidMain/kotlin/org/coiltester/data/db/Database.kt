package org.coiltester.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.coiltester.data.db.dao.*
import org.coiltester.data.db.entity.*

@Database(
    entities = [
        ProductEntity::class,
        SparePartEntity::class,
        TroubleshootingGuideEntity::class,
        RefrigerantEntity::class,
        CampaignEntity::class,
        DbInfoEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class CoilTesterDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun sparePartDao(): SparePartDao
    abstract fun troubleshootingDao(): TroubleshootingDao
    abstract fun refrigerantDao(): RefrigerantDao
    abstract fun campaignDao(): CampaignDao
    abstract fun dbInfoDao(): DbInfoDao

    companion object {
        @Volatile
        private var INSTANCE: CoilTesterDatabase? = null

        fun getInstance(context: Context): CoilTesterDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    CoilTesterDatabase::class.java,
                    "coil_tester.db"
                )
                    .createFromAsset("databases/coil_tester.db")
                    .build()
                INSTANCE = db
                db
            }
        }
    }
}
