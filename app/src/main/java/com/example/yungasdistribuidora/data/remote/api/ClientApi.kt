package com.example.yungasdistribuidora.data.remote.api

import com.example.yungasdistribuidora.data.remote.dto.client.*
import retrofit2.Response
import retrofit2.http.*

interface ClientApi {
    @GET("clients")
    suspend fun getClients(
        @Query("locationId") locationId: String?,
        @Query("type") type: String?,
        @Query("includeInactive") includeInactive: Boolean?
    ): Response<List<ClientDto>>

    @GET("clients/{id}")
    suspend fun getClientById(
        @Path("id") id: String
    ): Response<ClientDto>

    @POST("clients")
    suspend fun createClient(
        @Body request: CreateClientRequest
    ): Response<ClientDto>

    @PATCH("clients/{id}")
    suspend fun updateClient(
        @Path("id") id: String,
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): Response<ClientDto>

    @PATCH("clients/{id}/deactivate")
    suspend fun deactivateClient(
        @Path("id") id: String,
        @Body request: DeactivateClientRequest
    ): Response<ClientDto>

    @PATCH("clients/{id}/reactivate")
    suspend fun reactivateClient(
        @Path("id") id: String,
        @Body request: ReactivateClientRequest
    ): Response<ClientDto>
}
