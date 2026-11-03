package com.example.yungasdistribuidora.data.remote.api

import com.example.yungasdistribuidora.data.remote.dto.CreateSaleRequestDto
import com.example.yungasdistribuidora.data.remote.dto.SaleResponseDto
import com.example.yungasdistribuidora.data.remote.dto.*
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
        @Body request: UpdateSaleRequestDto
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

    @GET("clients/{id}")
    suspend fun getWhatsAppClient(@Path("id") id: String): Response<WhatsAppClientDto>

    @GET("clients/{id}/contacts")
    suspend fun getWhatsAppContacts(@Path("id") id: String): Response<List<WhatsAppContactDto>>

    @POST("sales/{id}/whatsapp/recipients")
    suspend fun sendWhatsapp(
        @Path("id") id: String,
        @Body body: WhatsAppBatchRequest
    ): Response<WhatsAppBatchResponse>

    @GET("documents/sales/{id}")
    suspend fun downloadPdf(
        @Path("id") id: String
    ): Response<ResponseBody>
}
