package com.example.yungasdistribuidora.data.remote.api

import com.example.yungasdistribuidora.data.remote.dto.CreateSaleRequestDto
import com.example.yungasdistribuidora.data.remote.dto.SaleResponseDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface SalesApi {
    @POST("sales")
    suspend fun createSale(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: CreateSaleRequestDto
    ): Response<SaleResponseDto>

    @GET("sales")
    suspend fun getSales(
        @Query("status") status: String?,
        @Query("paymentStatus") paymentStatus: String?,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?,
        @Query("clientId") clientId: String?,
        @Query("sellerId") sellerId: String?,
        @Query("search") search: String?
    ): Response<List<SaleResponseDto>>

    @GET("sales/{id}")
    suspend fun getSaleById(@Path("id") id: String): Response<SaleResponseDto>

    @PATCH("sales/{id}")
    suspend fun updateSale(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Path("id") id: String,
        @Body request: CreateSaleRequestDto
    ): Response<SaleResponseDto>

    @PATCH("sales/{id}/confirm")
    suspend fun confirmSale(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Path("id") id: String
    ): Response<SaleResponseDto>

    @PATCH("sales/{id}/cancel")
    suspend fun cancelSale(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Path("id") id: String
    ): Response<SaleResponseDto>

    @POST("sales/{id}/whatsapp")
    suspend fun sendWhatsapp(
        @Path("id") id: String,
        @Body body: Map<String, Boolean>
    ): Response<Unit>

    @GET("sales/{id}/pdf")
    suspend fun downloadPdf(
        @Path("id") id: String
    ): Response<ResponseBody>
}
