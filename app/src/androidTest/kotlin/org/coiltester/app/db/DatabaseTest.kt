package org.coiltester.app.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import org.coiltester.data.db.CoilTesterDatabase
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    @Test
    fun seedDatabaseLoadsRefrigerants() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = CoilTesterDatabase.getInstance(context)
        var count = -1
        kotlinx.coroutines.runBlocking {
            count = db.refrigerantDao().getAll().first().size
        }
        assertTrue("Expected seed refrigerants but got " + count, count >= 1)
        db.close()
    }
}
