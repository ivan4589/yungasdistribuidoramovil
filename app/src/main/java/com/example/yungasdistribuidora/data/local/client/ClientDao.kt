package com.example.yungasdistribuidora.data.local.client

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("""
        SELECT * FROM clients 
        WHERE (:includeInactive = 1 OR isActive = 1)
        AND (:locationId IS NULL OR locationId = :locationId)
        AND (:type IS NULL OR type = :type)
        ORDER BY fullName ASC
    """)
    fun getClients(includeInactive: Boolean, locationId: String?, type: String?): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClientById(id: String): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity)

    @Query("DELETE FROM clients")
    suspend fun deleteAllClients()
}
