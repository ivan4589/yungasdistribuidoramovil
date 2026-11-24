package com.example.yungasdistribuidora.data.remote.api

import com.example.yungasdistribuidora.data.remote.dto.*
import com.example.yungasdistribuidora.data.remote.dto.CancelSaleRequestDto
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
        @Query("dateFrom") dateFrom: String?,
        @Query("dateTo") dateTo: String?,
        @Query("clientId") clientId: String?
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
        @Path("id") id: String,
        @Body request: CancelSaleRequestDto
    ): Response<SaleResponseDto>

    @POST("sales/{id}/whatsapp")
    suspend fun sendWhatsapp(
        @Path("id") id: String,
        @Body body: Map<String, Boolean>
    ): Response<Unit>

    @Streaming
    @GET("documents/sales/{id}")
    suspend fun downloadPdf(
        @Path("id") id: String,
        @Query("cancelled") cancelled: Boolean? = null
    ): Response<ResponseBody>

    @POST("sales/{id}/delivery/start")
    suspend fun startDelivery(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Path("id") id: String
    ): Response<DeliveryResponseDto>

    @GET("sales/{id}/delivery")
    suspend fun getDeliveryDetails(
        @Path("id") id: String
    ): Response<DeliveryResponseDto>

    @POST("sales/{id}/delivery/events")
    suspend fun recordDeliveryEvent(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Path("id") id: String,
        @Body request: DeliveryEventRequestDto
    ): Response<DeliveryResponseDto>

    @PATCH("sales/{id}/delivery/quantity")
    suspend fun updateDeliveryQuantity(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Path("id") id: String,
        @Body request: UpdateDeliveryQuantityRequestDto
    ): Response<DeliveryResponseDto>

    @PATCH("sales/{id}/delivery/confirm")
    suspend fun confirmDelivery(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Path("id") id: String
    ): Response<SaleResponseDto>

    @GET("realtime/access")
    suspend fun getRealtimeAccess(): Response<RealtimeAccessDto>
}
