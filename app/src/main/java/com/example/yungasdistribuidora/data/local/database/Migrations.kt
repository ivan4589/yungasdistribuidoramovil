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
