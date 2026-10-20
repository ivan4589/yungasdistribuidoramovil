package com.example.yungasdistribuidora.domain.repository

import com.example.yungasdistribuidora.data.remote.dto.CreateSaleRequestDto
import com.example.yungasdistribuidora.data.remote.dto.SaleResponseDto
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody

interface SalesRepository : PresaleRepository {
    fun getSales(
        status: String? = null,
        paymentStatus: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        clientId: String? = null,
        sellerId: String? = null,
        search: String? = null
    ): Flow<List<SaleResponseDto>>

    suspend fun refreshSales(
        status: String? = null,
        paymentStatus: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        clientId: String? = null,
        sellerId: String? = null,
        search: String? = null
    ): Result<Unit>

    suspend fun getSaleById(id: String): Result<SaleResponseDto>
    suspend fun updateSale(idempotencyKey: String, id: String, request: CreateSaleRequestDto): Result<SaleResponseDto>
    suspend fun confirmSale(idempotencyKey: String, id: String): Result<SaleResponseDto>
    suspend fun cancelSale(idempotencyKey: String, id: String): Result<SaleResponseDto>
    suspend fun sendWhatsapp(id: String, resend: Boolean): Result<Unit>
    suspend fun downloadPdf(id: String): Result<ResponseBody>
}
