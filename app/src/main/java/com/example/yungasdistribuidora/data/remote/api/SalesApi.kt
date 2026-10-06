package com.example.yungasdistribuidora.data.remote.api

import com.example.yungasdistribuidora.data.remote.dto.CreateSaleRequestDto
import com.example.yungasdistribuidora.data.remote.dto.SaleResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SalesApi {
    @POST("sales")
    suspend fun createSale(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: CreateSaleRequestDto
    ): Response<SaleResponseDto>
}
