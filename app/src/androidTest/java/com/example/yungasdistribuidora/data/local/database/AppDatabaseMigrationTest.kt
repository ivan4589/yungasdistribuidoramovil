package com.example.yungasdistribuidora.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.yungasdistribuidora.data.local.sale.SaleBonusEntity
import com.example.yungasdistribuidora.data.local.sale.SaleDetailEntity
import com.example.yungasdistribuidora.data.local.sale.SaleEntity
import kotlinx.coroutines.runBlocking
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
    @Test fun migrate5ToLatest() = checkUpgrade(5)
    @Test fun migrate6ToLatest() = checkUpgrade(6)
    @Test fun migrate6LegacyNonNullPaymentColumnsToLatest() =
        checkUpgrade(6, forceLegacySalesNullability = true)

    @Test fun migrateWithForeignKeysEnabled() = checkForeignKeyModes(true)
    @Test fun migrateWithForeignKeysDisabled() = checkForeignKeyModes(false)

    private fun checkForeignKeyModes(enabled: Boolean) {
        for (version in listOf(5, 6)) {
            val name = "migration-fk-$version-${System.nanoTime()}"
            try {
                createLegacyDatabase(name, version, false, forceLegacySalesNullability = version == 6)
                val config = SupportSQLiteOpenHelper.Configuration.builder(context)
                    .name(name)
                    .callback(object : SupportSQLiteOpenHelper.Callback(version) {
                        override fun onConfigure(db: SupportSQLiteDatabase) {
                            db.setForeignKeyConstraintsEnabled(enabled)
                        }
                        override fun onCreate(db: SupportSQLiteDatabase) = error("Missing fixture")
                        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
                            error("Unexpected fixture version")
                    }).build()
                val before = FrameworkSQLiteOpenHelperFactory().create(config).use { helper ->
                    val db = helper.writableDatabase
                    assertEquals(if (enabled) 1 else 0, scalar(db, "PRAGMA foreign_keys"))
                    assertEquals(version, db.version)
                    db.execSQL("""
                        INSERT INTO sales (id, saleNumber, clientId, status, paymentStatus, saleType,
                            subtotal, discount, total, paidAmount, balance, createdAt, updatedAt)
                        VALUES ('s2', 'SALE-2', 'c1', 'PENDING', 'UNPAID', 'CASH',
                            45, 5, 40, 3, 37, '2026-01-01', '2026-01-02')
                    """.trimIndent())
                    db.execSQL("INSERT INTO sale_details VALUES ('d2', 's1', 'p1', 'Product', 1, 11, 1, 11)")
                    db.execSQL("INSERT INTO sale_details VALUES ('d3', 's2', 'p1', 'Product', 3, 15, 0, 45)")
                    db.execSQL("INSERT INTO sale_bonuses VALUES ('bonus2', 's2', 'p1', 'Product', 1)")
                    val rows = snapshot(db)
                    val migration = if (version == 5) MIGRATION_5_6 else MIGRATION_6_7
                    // Simulate an interruption after all SQL has run but before commit.
                    db.beginTransaction()
                    try {
                        migration.migrate(db)
                        db.version = version + 1
                        assertEquals(rows, snapshot(db))
                    } finally {
                        db.endTransaction()
                    }
                    assertEquals(version, db.version)
                    assertEquals(rows, snapshot(db))
                    db.beginTransaction()
                    try {
                        migration.migrate(db)
                        db.version = version + 1
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                    assertEquals(rows, snapshot(db))
                    assertEquals(0, scalar(db, "SELECT COUNT(*) FROM pragma_foreign_key_check"))
                    rows
                }
                // Room must accept the final schema and reopen without altering rows.
                repeat(2) {
                    openRoom(name).useRoom { room ->
                        val db = room.openHelper.writableDatabase
                        assertEquals(7, db.version)
                        assertEquals(before, snapshot(db))
                        assertEquals(0, scalar(db, "SELECT COUNT(*) FROM pragma_foreign_key_check"))
                    }
                }
            } finally {
                context.deleteDatabase(name)
            }
        }
    }

    @Test
    fun repeatedBatchesAndPartialResponsesPreserveSaleLines() = runBlocking {
        val name = "migration-sync-${System.nanoTime()}"
        try {
            openRoom(name).useRoom { room ->
                val dao = room.saleDao()
                val sale = SaleEntity("s1", "SALE-1", "c1", null, "PENDING", null,
                    "CASH", null, 55.0, 0.0, 55.0, null, null, null, null, null, null,
                    "2026-01-01", "2026-01-01")
                val detail = SaleDetailEntity("d1", "s1", "p1", null, 5, 11.0, true, 55.0)
                val bonus = SaleBonusEntity("b1", "s1", "p1", null, 2)
                repeat(2) {
                    dao.saveRemoteBatch(listOf(sale), listOf(listOf(detail)), listOf(listOf(bonus)))
                }
                val other = sale.copy(id = "s2", saleNumber = "SALE-2")
                val otherDetail = detail.copy(id = "d2", saleId = "s2")
                dao.saveCompleteSale(other, listOf(otherDetail), emptyList())
                dao.saveRemoteBatch(listOf(sale.copy(status = "CONFIRMED")), listOf(null), listOf(null))
                assertEquals("CONFIRMED", dao.getSaleById("s1")?.status)
                assertEquals(listOf(detail), dao.getSaleDetails("s1"))
                assertEquals(listOf(bonus), dao.getSaleBonuses("s1"))
                assertEquals(listOf(otherDetail), dao.getSaleDetails("s2"))
                // One invalid row must not steal an ID from another sale or erase local rows.
                try {
                    dao.saveCompleteSale(sale, listOf(detail.copy(id = "d2")), emptyList())
                    fail("Expected cross-sale primary key collision to roll back")
                } catch (_: android.database.sqlite.SQLiteConstraintException) {
                    assertEquals("CONFIRMED", dao.getSaleById("s1")?.status)
                    assertEquals(listOf(detail), dao.getSaleDetails("s1"))
                    assertEquals(listOf(bonus), dao.getSaleBonuses("s1"))
                }
                assertEquals(0, scalar(room.openHelper.writableDatabase, "SELECT COUNT(*) FROM pragma_foreign_key_check"))
            }
        } finally {
            context.deleteDatabase(name)
        }
        Unit
    }

    private fun scalar(db: SupportSQLiteDatabase, sql: String): Int = db.query(sql).use {
        assertTrue(it.moveToFirst())
        it.getInt(0)
    }

    private fun snapshot(db: SupportSQLiteDatabase): Map<String, List<List<String?>>> =
        listOf("sales", "sale_details", "sale_bonuses").associateWith { table ->
            // Explicit ordering of rows and columns from the historic schema.
            db.query("SELECT * FROM ${table} ORDER BY id").use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(List(cursor.columnCount) { index ->
                            if (cursor.isNull(index)) null else cursor.getString(index)
                        })
                    }
                }
            }
        }

    @Test
    fun freshDatabaseAndReopen() {
        val name = "migration-fresh-${System.nanoTime()}"
        try {
            openRoom(name).useRoom { room ->
                assertEquals(7, room.openHelper.writableDatabase.version)
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

    private fun checkUpgrade(version: Int, missingColumn: Boolean = false, forceLegacySalesNullability: Boolean = false) {
        val name = "migration-$version-${System.nanoTime()}"
        try {
            createLegacyDatabase(name, version, missingColumn, forceLegacySalesNullability)
            // Opening through Room performs real schema validation, not just SQL checks.
            openRoom(name).useRoom { room ->
                val db = room.openHelper.writableDatabase
                assertEquals(7, db.version)
                db.query("SELECT fullName FROM clients WHERE id = 'c1'").use {
                    assertTrue(it.moveToFirst())
                    assertEquals("Client 1", it.getString(0))
                }
                if (version >= 3) {
                    db.query("SELECT minQuantity, bonusQuantity, startDate, createdAt FROM bonus_rules WHERE id = 'b1'").use {
                        assertTrue(it.moveToFirst())
                        assertEquals(10, it.getInt(0))
                        assertEquals(1, it.getInt(1))
                        if (version == 3 || version == 4) {
                            if (missingColumn) {
                                assertEquals("2026-01-01", it.getString(2))
                                assertTrue(it.isNull(3))
                            } else {
                                assertTrue(it.isNull(2))
                                assertEquals("2026-01-01", it.getString(3))
                            }
                        }
                    }
                    db.query("SELECT total, cancelledPdfUrl FROM sales WHERE id = 's1'").use {
                        assertTrue(it.moveToFirst())
                        assertEquals(55.0, it.getDouble(0), 0.0)
                        if (version >= 4 && !missingColumn) {
                            assertEquals("https://example.test/cancelled.pdf", it.getString(1))
                        } else {
                            // in v5 or missingColumn, may be null
                            assertTrue(it.isNull(1) || it.getString(1) == "https://example.test/cancelled.pdf")
                        }
                    }
                    db.query("SELECT quantity FROM sale_details WHERE id = 'd1'").use {
                        assertTrue(it.moveToFirst())
                        assertEquals(5, it.getInt(0))
                    }
                    db.query("SELECT quantity FROM sale_bonuses WHERE id = 'bonus1'").use {
                        assertTrue("Bonificaciones perdidas durante migración", it.moveToFirst())
                        assertEquals(2, it.getInt(0))
                    }
                    db.query("PRAGMA foreign_key_check").use {
                        assertFalse("Foreign key violation detected after migration", it.moveToFirst())
                    }
                }
            }
            // Reopen to check the persisted Room identity after migration.
            openRoom(name).useRoom { assertEquals(7, it.openHelper.writableDatabase.version) }
        } finally {
            context.deleteDatabase(name)
        }
    }

    private inline fun <T> AppDatabase.useRoom(block: (AppDatabase) -> T): T =
        try { block(this) } finally { close() }

    private fun openRoom(name: String): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .build()

    private fun createLegacyDatabase(name: String, version: Int, missingColumn: Boolean, forceLegacySalesNullability: Boolean) {
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
                        db.execSQL("INSERT INTO sale_bonuses VALUES ('bonus1', 's1', 'p1', 'Product', 2)")
                    }
                    if (version >= 4 && !missingColumn) {
                        MIGRATION_3_4.migrate(db)
                        db.execSQL("UPDATE sales SET cancelledPdfUrl = 'https://example.test/cancelled.pdf'")
                    }
                    if (version >= 5) {
                        MIGRATION_4_5.migrate(db)
                    }
                    // Historic v6 installations could still carry the v5 NOT NULL schema.
                    if (version >= 6 && !forceLegacySalesNullability) {
                        MIGRATION_5_6.migrate(db)
                    }
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
                    error("Legacy fixture must not upgrade itself")
            }).build()
        FrameworkSQLiteOpenHelperFactory().create(config).use { it.writableDatabase }
    }
}
