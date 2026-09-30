package com.example.yungasdistribuidora.data.local.location

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getLocationsFlow(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations ORDER BY name ASC")
    suspend fun getLocations(): List<LocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Query("DELETE FROM locations")
    suspend fun deleteAllLocations()
}
