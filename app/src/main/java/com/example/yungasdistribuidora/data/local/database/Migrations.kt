package com.example.yungasdistribuidora.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS products (
                id TEXT PRIMARY KEY NOT NULL,
                code TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                providerId TEXT NOT NULL,
                categoryId TEXT NOT NULL,
                subCategoryId TEXT,
                weight TEXT,
                priceNormal REAL NOT NULL,
                priceCamino REAL NOT NULL,
                priceEspecial REAL NOT NULL,
                priceMayorista REAL,
                minQuantityWholesale REAL,
                stock REAL NOT NULL,
                centralStock REAL,
                centralReservedStock REAL,
                centralAvailableStock REAL,
                minStock REAL NOT NULL,
                unit TEXT NOT NULL,
                reserveQuantity REAL NOT NULL,
                additionalInfo TEXT,
                imageUrl TEXT,
                isActive INTEGER NOT NULL,
                deletedAt TEXT,
                createdAt TEXT NOT NULL,
                updatedAt TEXT NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS categories (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                createdAt TEXT,
                updatedAt TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS subcategories (
                id TEXT PRIMARY KEY NOT NULL,
                categoryId TEXT NOT NULL,
                name TEXT NOT NULL,
                createdAt TEXT,
                updatedAt TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS catalog_sync_metadata (
                `key` TEXT PRIMARY KEY NOT NULL,
                lastSuccessfulSync INTEGER NOT NULL
            )
        """)
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS bonus_rules (
                id TEXT PRIMARY KEY NOT NULL,
                subCategoryId TEXT NOT NULL,
                minimumQuantity INTEGER NOT NULL,
                bonusQuantity INTEGER NOT NULL,
                isActive INTEGER NOT NULL,
                createdAt TEXT,
                updatedAt TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS sales (
                id TEXT PRIMARY KEY NOT NULL,
                saleNumber TEXT NOT NULL,
                clientId TEXT NOT NULL,
                clientName TEXT,
                status TEXT NOT NULL,
                paymentStatus TEXT NOT NULL,
                saleType TEXT NOT NULL,
                dueDate TEXT,
                subtotal REAL NOT NULL,
                discount REAL NOT NULL,
                total REAL NOT NULL,
                paidAmount REAL NOT NULL,
                balance REAL NOT NULL,
                observations TEXT,
                pdfUrl TEXT,
                sellerId TEXT,
                sellerName TEXT,
                createdAt TEXT NOT NULL,
                updatedAt TEXT NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS sale_details (
                id TEXT PRIMARY KEY NOT NULL,
                saleId TEXT NOT NULL,
                productId TEXT NOT NULL,
                productName TEXT,
                quantity INTEGER NOT NULL,
                unitPrice REAL NOT NULL,
                manualPrice INTEGER NOT NULL,
                subtotal REAL NOT NULL,
                FOREIGN KEY(saleId) REFERENCES sales(id) ON DELETE CASCADE
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_sale_details_saleId ON sale_details(saleId)")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS sale_bonuses (
                id TEXT PRIMARY KEY NOT NULL,
                saleId TEXT NOT NULL,
                productId TEXT NOT NULL,
                productName TEXT,
                quantity INTEGER NOT NULL,
                FOREIGN KEY(saleId) REFERENCES sales(id) ON DELETE CASCADE
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_sale_bonuses_saleId ON sale_bonuses(saleId)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE sales ADD COLUMN cancelledPdfUrl TEXT")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val hasCancelledPdfUrl = db.query("PRAGMA table_info(`sales`)").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            var found = false
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == "cancelledPdfUrl") found = true
            }
            found
        }
        if (!hasCancelledPdfUrl) {
            db.execSQL("ALTER TABLE sales ADD COLUMN cancelledPdfUrl TEXT")
        }
        val columns = db.query("PRAGMA table_info(`bonus_rules`)").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            buildSet { while (cursor.moveToNext()) add(cursor.getString(nameIndex)) }
        }
        val quantity = when {
            "minQuantity" in columns -> "minQuantity"
            "minimumQuantity" in columns -> "minimumQuantity"
            else -> error("Unknown bonus_rules schema: no minimum quantity")
        }
        fun nullableColumn(name: String) = if (name in columns) "`$name`" else "NULL"
        db.execSQL("""CREATE TABLE bonus_rules_v5 (
            id TEXT PRIMARY KEY NOT NULL, subCategoryId TEXT NOT NULL,
            minQuantity INTEGER NOT NULL, bonusQuantity INTEGER NOT NULL,
            startDate TEXT, endDate TEXT, isActive INTEGER NOT NULL,
            createdAt TEXT, updatedAt TEXT
        )""")
        db.execSQL("""INSERT INTO bonus_rules_v5
            SELECT id, subCategoryId, `$quantity`, bonusQuantity,
            ${nullableColumn("startDate")}, ${nullableColumn("endDate")}, isActive,
            ${nullableColumn("createdAt")}, ${nullableColumn("updatedAt")}
            FROM bonus_rules""")
        db.execSQL("DROP TABLE bonus_rules")
        db.execSQL("ALTER TABLE bonus_rules_v5 RENAME TO bonus_rules")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Room runs migrations in a transaction; changing PRAGMA foreign_keys is ineffective.
        // Back up dependents before replacing their parent table.
        db.execSQL("CREATE TEMP TABLE _sale_details_preserved AS SELECT * FROM sale_details")
        db.execSQL("CREATE TEMP TABLE _sale_bonuses_preserved AS SELECT * FROM sale_bonuses")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS sales_v6 (
                id TEXT PRIMARY KEY NOT NULL,
                saleNumber TEXT NOT NULL,
                clientId TEXT NOT NULL,
                clientName TEXT,
                status TEXT NOT NULL,
                paymentStatus TEXT,
                saleType TEXT NOT NULL,
                dueDate TEXT,
                subtotal REAL NOT NULL,
                discount REAL NOT NULL,
                total REAL NOT NULL,
                paidAmount REAL,
                balance REAL,
                observations TEXT,
                pdfUrl TEXT,
                sellerId TEXT,
                sellerName TEXT,
                createdAt TEXT NOT NULL,
                updatedAt TEXT NOT NULL,
                cancelledPdfUrl TEXT
            )
        """)
        db.execSQL("""
            INSERT INTO sales_v6 (
                id, saleNumber, clientId, clientName, status, paymentStatus, saleType, dueDate,
                subtotal, discount, total, paidAmount, balance, observations, pdfUrl, sellerId,
                sellerName, createdAt, updatedAt, cancelledPdfUrl
            )
            SELECT 
                id, saleNumber, clientId, clientName, status, paymentStatus, saleType, dueDate,
                subtotal, discount, total, paidAmount, balance, observations, pdfUrl, sellerId,
                sellerName, createdAt, updatedAt, cancelledPdfUrl
            FROM sales
        """)
        db.execSQL("DROP TABLE sales")
        db.execSQL("ALTER TABLE sales_v6 RENAME TO sales")
        // Restoring the parent alone is insufficient if DROP TABLE cascaded children.
        db.execSQL("INSERT INTO sale_details SELECT * FROM _sale_details_preserved")
        db.execSQL("INSERT INTO sale_bonuses SELECT * FROM _sale_bonuses_preserved")
        db.execSQL("DROP TABLE _sale_details_preserved")
        db.execSQL("DROP TABLE _sale_bonuses_preserved")
        db.query("PRAGMA foreign_key_check").use { cursor ->
            check(!cursor.moveToFirst()) { "Invalid sale foreign keys after migration 5 to 6" }
        }
    }
}
