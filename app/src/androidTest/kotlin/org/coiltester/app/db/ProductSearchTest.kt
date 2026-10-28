package org.coiltester.app.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.coiltester.data.db.CoilTesterDatabase
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductSearchTest {
    @Test
    fun searchReturnsResultsForKnownTerm() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = CoilTesterDatabase.getInstance(context)
        runBlocking {
            val results = db.productDao().search("").first()
            assertTrue("Expected seeded products but got " + results.size, results.isNotEmpty())
        }
        db.close()
    }
}
