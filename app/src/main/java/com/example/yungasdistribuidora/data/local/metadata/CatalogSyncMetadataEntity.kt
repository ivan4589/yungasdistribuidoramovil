package com.example.yungasdistribuidora.data.local.metadata

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_sync_metadata")
data class CatalogSyncMetadataEntity(
    @PrimaryKey val key: String,
    val lastSuccessfulSync: Long
)
