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
                minQuantity INTEGER NOT NULL,
                bonusQuantity INTEGER NOT NULL,
                startDate TEXT,
                endDate TEXT,
                isActive INTEGER NOT NULL
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
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_sale_details_saleId ON sale_details(saleId)
        """)
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
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_sale_bonuses_saleId ON sale_bonuses(saleId)
        """)
    }
}
