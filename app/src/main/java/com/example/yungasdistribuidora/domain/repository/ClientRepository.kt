package com.example.yungasdistribuidora.domain.repository

import com.example.yungasdistribuidora.data.remote.dto.client.CreateClientRequest
import com.example.yungasdistribuidora.domain.model.Client
import com.example.yungasdistribuidora.domain.model.ClientType
import com.example.yungasdistribuidora.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface ClientRepository {
    fun getClients(includeInactive: Boolean, locationId: String?, type: ClientType?): Flow<List<Client>>
    suspend fun refreshClients(includeInactive: Boolean, locationId: String?, type: ClientType?): Result<Unit>
    
    suspend fun getClientById(id: String): Result<Client>
    
    fun getLocationsFlow(): Flow<List<Location>>
    suspend fun getLocations(): Result<List<Location>>
    suspend fun refreshLocations(): Result<Unit>
    
    suspend fun createClient(request: CreateClientRequest): Result<Client>
    suspend fun updateClient(id: String, request: Map<String, Any?>): Result<Client>
    suspend fun deactivateClient(id: String, reason: String): Result<Client>
    suspend fun reactivateClient(id: String, reason: String): Result<Client>
    
    fun getLastSyncTime(): Flow<Long?>
}
