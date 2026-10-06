package com.example.yungasdistribuidora.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO clients (id, fullName, alias, type, locationId, locationName, phone, whatsappConsent, additionalInfo, isActive, deletedAt, createdAt, updatedAt) VALUES ('c1', 'Client 1', 'Alias', 'NORMAL', 'l1', 'Chulumani', '123', 1, 'info', 1, null, '2026-01-01', '2026-01-01')")
            execSQL("INSERT INTO locations (id, name, createdAt, updatedAt) VALUES ('l1', 'Chulumani', '2026-01-01', '2026-01-01')")
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        val cursorClients = db.query("SELECT * FROM clients")
        assertTrue(cursorClients.moveToFirst())
        assertEquals("Client 1", cursorClients.getString(cursorClients.getColumnIndexOrThrow("fullName")))
        cursorClients.close()

        val cursorProducts = db.query("SELECT * FROM products")
        assertNotNull(cursorProducts)
        cursorProducts.close()

        val cursorMetadata = db.query("SELECT * FROM catalog_sync_metadata")
        assertNotNull(cursorMetadata)
        cursorMetadata.close()
    }
}
