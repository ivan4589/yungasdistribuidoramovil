package com.example.yungasdistribuidora.data.repository

import android.content.Context
import com.example.yungasdistribuidora.data.local.client.ClientDao
import com.example.yungasdistribuidora.data.local.location.LocationDao
import com.example.yungasdistribuidora.data.mapper.*
import com.example.yungasdistribuidora.data.remote.api.ClientApi
import com.example.yungasdistribuidora.data.remote.api.LocationApi
import com.example.yungasdistribuidora.data.remote.dto.client.CreateClientRequest
import com.example.yungasdistribuidora.data.remote.dto.client.DeactivateClientRequest
import com.example.yungasdistribuidora.data.remote.dto.client.ReactivateClientRequest
import com.example.yungasdistribuidora.domain.model.Client
import com.example.yungasdistribuidora.domain.model.ClientType
import com.example.yungasdistribuidora.domain.model.Location
import com.example.yungasdistribuidora.domain.repository.ClientRepository
import com.example.yungasdistribuidora.util.ErrorMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class ClientRepositoryImpl(
    private val clientApi: ClientApi,
    private val locationApi: LocationApi,
    private val clientDao: ClientDao,
    private val locationDao: LocationDao,
    private val context: Context
) : ClientRepository {

    private val prefs = context.getSharedPreferences("client_sync_prefs", Context.MODE_PRIVATE)
    private val _lastSyncTime = MutableStateFlow<Long?>(prefs.getLong("last_sync_time", 0L).takeIf { it > 0L })

    override fun getClients(includeInactive: Boolean, locationId: String?, type: ClientType?): Flow<List<Client>> {
        return clientDao.getClients(includeInactive, locationId, type?.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshClients(includeInactive: Boolean, locationId: String?, type: ClientType?): Result<Unit> {
        return try {
            val response = clientApi.getClients(locationId, type?.name, includeInactive)
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                clientDao.insertClients(dtos.map { it.toEntity() })
                val now = System.currentTimeMillis()
                prefs.edit().putLong("last_sync_time", now).apply()
                _lastSyncTime.value = now
                Result.success(Unit)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun getClientById(id: String): Result<Client> {
        return try {
            // First check local or fetch remote
            val response = clientApi.getClientById(id)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                clientDao.insertClient(dto.toEntity())
                Result.success(dto.toDomain())
            } else {
                val local = clientDao.getClientById(id)
                if (local != null) {
                    Result.success(local.toDomain())
                } else {
                    Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
                }
            }
        } catch (e: Exception) {
            val local = clientDao.getClientById(id)
            if (local != null) {
                Result.success(local.toDomain())
            } else {
                Result.failure(Exception(ErrorMapper.mapException(e)))
            }
        }
    }

    override fun getLocationsFlow(): Flow<List<Location>> {
        return locationDao.getLocationsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getLocations(): Result<List<Location>> {
        return try {
            val response = locationApi.getLocations()
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                locationDao.insertLocations(dtos.map { it.toEntity() })
                Result.success(dtos.map { it.toDomain() })
            } else {
                val local = locationDao.getLocations()
                if (local.isNotEmpty()) {
                    Result.success(local.map { it.toDomain() })
                } else {
                    Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
                }
            }
        } catch (e: Exception) {
            val local = locationDao.getLocations()
            if (local.isNotEmpty()) {
                Result.success(local.map { it.toDomain() })
            } else {
                Result.failure(Exception(ErrorMapper.mapException(e)))
            }
        }
    }

    override suspend fun refreshLocations(): Result<Unit> {
        return try {
            val response = locationApi.getLocations()
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                locationDao.insertLocations(dtos.map { it.toEntity() })
                Result.success(Unit)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun createClient(request: CreateClientRequest): Result<Client> {
        return try {
            val response = clientApi.createClient(request)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                clientDao.insertClient(dto.toEntity())
                Result.success(dto.toDomain())
            } else {
                val code = response.code()
                val errorMsg = if (code == 409) {
                    "Ya existe un cliente activo con el mismo teléfono"
                } else {
                    ErrorMapper.mapHttpCode(code)
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun updateClient(id: String, request: Map<String, Any?>): Result<Client> {
        return try {
            val response = clientApi.updateClient(id, request)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                clientDao.insertClient(dto.toEntity())
                Result.success(dto.toDomain())
            } else {
                val code = response.code()
                val errorMsg = if (code == 409) {
                    "Ya existe un cliente activo con el mismo teléfono"
                } else {
                    ErrorMapper.mapHttpCode(code)
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun deactivateClient(id: String, reason: String): Result<Client> {
        return try {
            val response = clientApi.deactivateClient(id, DeactivateClientRequest(reason))
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                clientDao.insertClient(dto.toEntity())
                Result.success(dto.toDomain())
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun reactivateClient(id: String, reason: String): Result<Client> {
        return try {
            val response = clientApi.reactivateClient(id, ReactivateClientRequest(reason))
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                clientDao.insertClient(dto.toEntity())
                Result.success(dto.toDomain())
            } else {
                val code = response.code()
                val errorMsg = if (code == 409) {
                    "Ya existe un cliente activo con el mismo teléfono"
                } else {
                    ErrorMapper.mapHttpCode(code)
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override fun getLastSyncTime(): Flow<Long?> = _lastSyncTime.asStateFlow()
}
