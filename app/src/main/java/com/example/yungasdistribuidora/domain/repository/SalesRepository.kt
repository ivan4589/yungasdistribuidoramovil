package com.example.yungasdistribuidora.domain.repository

import com.example.yungasdistribuidora.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody

interface SalesRepository : PresaleRepository {
    override suspend fun registerSale(idempotencyKey: String, request: CreateSaleRequestDto): Result<SaleResponseDto>
    fun getSales(
        status: String? = null,
        paymentStatus: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        clientId: String? = null
    ): Flow<List<SaleResponseDto>>

    suspend fun refreshSales(
        status: String? = null,
        paymentStatus: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        clientId: String? = null
    ): Result<Unit>

    suspend fun getSaleById(id: String): Result<SaleResponseDto>
    suspend fun updateSale(idempotencyKey: String, id: String, request: CreateSaleRequestDto): Result<SaleResponseDto>
    suspend fun confirmSale(idempotencyKey: String, id: String): Result<SaleResponseDto>
    suspend fun cancelSale(idempotencyKey: String, id: String, reason: String): Result<SaleResponseDto>
    suspend fun sendWhatsapp(id: String, resend: Boolean): Result<Unit>
    suspend fun downloadPdf(id: String, cancelled: Boolean = false): Result<ResponseBody>

    suspend fun startDelivery(idempotencyKey: String, id: String): Result<DeliveryResponseDto>
    suspend fun getDeliveryDetails(id: String): Result<DeliveryResponseDto>
    suspend fun recordDeliveryEvent(idempotencyKey: String, id: String, request: DeliveryEventRequestDto): Result<DeliveryResponseDto>
    suspend fun updateDeliveryQuantity(idempotencyKey: String, id: String, request: UpdateDeliveryQuantityRequestDto): Result<DeliveryResponseDto>
    suspend fun confirmDelivery(idempotencyKey: String, id: String): Result<SaleResponseDto>
}
