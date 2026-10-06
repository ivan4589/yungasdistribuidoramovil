package com.example.yungasdistribuidora.domain.repository

import com.example.yungasdistribuidora.data.remote.dto.CreateSaleRequestDto
import com.example.yungasdistribuidora.data.remote.dto.SaleResponseDto

interface PresaleRepository {
    suspend fun registerSale(
        idempotencyKey: String,
        request: CreateSaleRequestDto
    ): Result<SaleResponseDto>
}
