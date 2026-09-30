package com.example.yungasdistribuidora.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.yungasdistribuidora.data.local.client.ClientDao
import com.example.yungasdistribuidora.data.local.client.ClientEntity
import com.example.yungasdistribuidora.data.local.location.LocationDao
import com.example.yungasdistribuidora.data.local.location.LocationEntity

@Database(entities = [ClientEntity::class, LocationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yungas_distribuidora.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
