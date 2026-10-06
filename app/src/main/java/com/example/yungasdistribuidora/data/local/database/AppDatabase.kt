package com.example.yungasdistribuidora.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.yungasdistribuidora.data.local.category.CategoryDao
import com.example.yungasdistribuidora.data.local.category.CategoryEntity
import com.example.yungasdistribuidora.data.local.client.ClientDao
import com.example.yungasdistribuidora.data.local.client.ClientEntity
import com.example.yungasdistribuidora.data.local.location.LocationDao
import com.example.yungasdistribuidora.data.local.location.LocationEntity
import com.example.yungasdistribuidora.data.local.metadata.CatalogSyncMetadataDao
import com.example.yungasdistribuidora.data.local.metadata.CatalogSyncMetadataEntity
import com.example.yungasdistribuidora.data.local.product.ProductDao
import com.example.yungasdistribuidora.data.local.product.ProductEntity
import com.example.yungasdistribuidora.data.local.subcategory.SubCategoryDao
import com.example.yungasdistribuidora.data.local.subcategory.SubCategoryEntity

@Database(
    entities = [
        ClientEntity::class,
        LocationEntity::class,
        ProductEntity::class,
        CategoryEntity::class,
        SubCategoryEntity::class,
        CatalogSyncMetadataEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun locationDao(): LocationDao
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subCategoryDao(): SubCategoryDao
    abstract fun catalogSyncMetadataDao(): CatalogSyncMetadataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yungas_distribuidora.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
