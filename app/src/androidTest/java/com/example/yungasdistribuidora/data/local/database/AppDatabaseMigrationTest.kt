package com.example.yungasdistribuidora.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun migrate1ToLatest() = checkUpgrade(1)
    @Test fun migrate2ToLatest() = checkUpgrade(2)
    @Test fun migrate3ToLatest() = checkUpgrade(3)
    @Test fun migrate4WithCancelledPdfToLatest() = checkUpgrade(4)
    @Test fun migrate4WithoutCancelledPdfToLatest() = checkUpgrade(4, missingColumn = true)

    @Test
    fun freshDatabaseAndReopen() {
        val name = "migration-fresh-${System.nanoTime()}"
        try {
            openRoom(name).useRoom { room ->
                assertEquals(5, room.openHelper.writableDatabase.version)
            }
            openRoom(name).useRoom { room ->
                room.openHelper.writableDatabase.query("SELECT cancelledPdfUrl FROM sales").use {
                    assertEquals(0, it.count)
                }
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    private fun checkUpgrade(version: Int, missingColumn: Boolean = false) {
        val name = "migration-$version-${System.nanoTime()}"
        try {
            createLegacyDatabase(name, version, missingColumn)
            // Opening through Room performs real schema validation, not just SQL checks.
            openRoom(name).useRoom { room ->
                val db = room.openHelper.writableDatabase
                assertEquals(5, db.version)
                db.query("SELECT fullName FROM clients WHERE id = 'c1'").use {
                    assertTrue(it.moveToFirst())
                    assertEquals("Client 1", it.getString(0))
                }
                if (version >= 3) {
                    db.query("SELECT minQuantity, bonusQuantity, startDate, createdAt FROM bonus_rules WHERE id = 'b1'").use {
                        assertTrue(it.moveToFirst())
                        assertEquals(10, it.getInt(0))
                        assertEquals(1, it.getInt(1))
                        if (missingColumn) {
                            assertEquals("2026-01-01", it.getString(2))
                            assertTrue(it.isNull(3))
                        } else {
                            assertTrue(it.isNull(2))
                            assertEquals("2026-01-01", it.getString(3))
                        }
                    }
                    db.query("SELECT total, cancelledPdfUrl FROM sales WHERE id = 's1'").use {
                        assertTrue(it.moveToFirst())
                        assertEquals(55.0, it.getDouble(0), 0.0)
                        if (version == 4 && !missingColumn) {
                            assertEquals("https://example.test/cancelled.pdf", it.getString(1))
                        } else {
                            assertTrue(it.isNull(1))
                        }
                    }
                    db.query("SELECT quantity FROM sale_details WHERE id = 'd1'").use {
                        assertTrue(it.moveToFirst())
                        assertEquals(5, it.getInt(0))
                    }
                }
            }
            // Reopen to check the persisted Room identity after migration.
            openRoom(name).useRoom { assertEquals(5, it.openHelper.writableDatabase.version) }
        } finally {
            context.deleteDatabase(name)
        }
    }

    private inline fun <T> AppDatabase.useRoom(block: (AppDatabase) -> T): T =
        try { block(this) } finally { close() }

    private fun openRoom(name: String): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()

    private fun createLegacyDatabase(name: String, version: Int, missingColumn: Boolean) {
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(name)
            .callback(object : SupportSQLiteOpenHelper.Callback(version) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""CREATE TABLE clients (
                        id TEXT PRIMARY KEY NOT NULL, fullName TEXT NOT NULL, alias TEXT,
                        type TEXT NOT NULL, locationId TEXT NOT NULL, locationName TEXT,
                        phone TEXT, whatsappConsent INTEGER NOT NULL, additionalInfo TEXT,
                        isActive INTEGER NOT NULL, deletedAt TEXT, createdAt TEXT NOT NULL,
                        updatedAt TEXT NOT NULL)""")
                    db.execSQL("""CREATE TABLE locations (
                        id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL,
                        createdAt TEXT, updatedAt TEXT)""")
                    db.execSQL("""INSERT INTO clients VALUES (
                        'c1', 'Client 1', 'Alias', 'NORMAL', 'l1', 'Chulumani',
                        '123', 1, 'info', 1, NULL, '2026-01-01', '2026-01-01')""")
                    db.execSQL("INSERT INTO locations VALUES ('l1', 'Chulumani', NULL, NULL)")
                    if (version >= 2) MIGRATION_1_2.migrate(db)
                    if (version >= 3) {
                        MIGRATION_2_3.migrate(db)
                        if (missingColumn) {
                            db.execSQL("DROP TABLE bonus_rules")
                            db.execSQL("""CREATE TABLE bonus_rules (
                                id TEXT PRIMARY KEY NOT NULL, subCategoryId TEXT NOT NULL,
                                minQuantity INTEGER NOT NULL, bonusQuantity INTEGER NOT NULL,
                                startDate TEXT, endDate TEXT, isActive INTEGER NOT NULL)""")
                            db.execSQL("INSERT INTO bonus_rules VALUES ('b1', 'sub1', 10, 1, '2026-01-01', NULL, 1)")
                        } else {
                            db.execSQL("INSERT INTO bonus_rules VALUES ('b1', 'sub1', 10, 1, 1, '2026-01-01', '2026-01-02')")
                        }
                        db.execSQL("""INSERT INTO sales VALUES (
                            's1', 'SALE-1', 'c1', 'Client 1', 'PENDING', 'UNPAID',
                            'POR_COBRAR', NULL, 55, 0, 55, 0, 55, NULL, NULL, NULL,
                            NULL, '2026-01-01', '2026-01-01')""")
                        db.execSQL("INSERT INTO sale_details VALUES ('d1', 's1', 'p1', 'Product', 5, 11, 1, 55)")
                    }
                    if (version >= 4 && !missingColumn) {
                        MIGRATION_3_4.migrate(db)
                        db.execSQL("UPDATE sales SET cancelledPdfUrl = 'https://example.test/cancelled.pdf'")
                    }
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
                    error("Legacy fixture must not upgrade itself")
            }).build()
        FrameworkSQLiteOpenHelperFactory().create(config).use { it.writableDatabase }
    }
}
