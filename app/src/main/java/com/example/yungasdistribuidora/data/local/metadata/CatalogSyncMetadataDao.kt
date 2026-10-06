package com.example.yungasdistribuidora.data.local.metadata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogSyncMetadataDao {
    @Query("SELECT * FROM catalog_sync_metadata WHERE `key` = :key")
    suspend fun getMetadata(key: String): CatalogSyncMetadataEntity?

    @Query("SELECT lastSuccessfulSync FROM catalog_sync_metadata WHERE `key` = :key")
    fun getLastSyncTimeFlow(key: String): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: CatalogSyncMetadataEntity)
}
