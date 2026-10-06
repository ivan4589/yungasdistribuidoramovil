package com.example.yungasdistribuidora.data.repository

import com.example.yungasdistribuidora.data.remote.api.SalesApi
import com.example.yungasdistribuidora.data.remote.dto.CreateSaleRequestDto
import com.example.yungasdistribuidora.data.remote.dto.SaleResponseDto
import com.example.yungasdistribuidora.domain.repository.PresaleRepository
import com.example.yungasdistribuidora.util.ErrorMapper

class PresaleRepositoryImpl(
    private val salesApi: SalesApi
) : PresaleRepository {

    override suspend fun registerSale(
        idempotencyKey: String,
        request: CreateSaleRequestDto
    ): Result<SaleResponseDto> {
        return try {
            val response = salesApi.createSale(idempotencyKey, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }
}
