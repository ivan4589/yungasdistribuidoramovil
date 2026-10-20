package com.example.yungasdistribuidora.data.repository

import com.example.yungasdistribuidora.data.local.sale.SaleDao
import com.example.yungasdistribuidora.data.mapper.SaleMapper
import com.example.yungasdistribuidora.data.remote.api.SalesApi
import com.example.yungasdistribuidora.data.remote.dto.CreateSaleRequestDto
import com.example.yungasdistribuidora.data.remote.dto.SaleResponseDto
import com.example.yungasdistribuidora.domain.repository.SalesRepository
import com.example.yungasdistribuidora.util.ErrorMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.ResponseBody

class SalesRepositoryImpl(
    private val salesApi: SalesApi,
    private val saleDao: SaleDao
) : SalesRepository {

    override suspend fun registerSale(idempotencyKey: String, request: CreateSaleRequestDto): Result<SaleResponseDto> {
        return try {
            val response = salesApi.createSale(idempotencyKey, request)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val entity = SaleMapper.toEntity(dto)
                val details = SaleMapper.toDetailEntities(dto.id, dto.details)
                val bonuses = SaleMapper.toBonusEntities(dto.id, dto.bonuses)
                saleDao.insertSale(entity)
                saleDao.insertDetails(details)
                saleDao.insertBonuses(bonuses)
                Result.success(dto)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override fun getSales(
        status: String?,
        paymentStatus: String?,
        startDate: String?,
        endDate: String?,
        clientId: String?,
        sellerId: String?,
        search: String?
    ): Flow<List<SaleResponseDto>> {
        return saleDao.getAllSales().map { entities ->
            entities.map { entity ->
                val details = saleDao.getSaleDetails(entity.id)
                val bonuses = saleDao.getSaleBonuses(entity.id)
                SaleMapper.toResponseDto(entity, details, bonuses)
            }
        }
    }

    override suspend fun refreshSales(
        status: String?,
        paymentStatus: String?,
        startDate: String?,
        endDate: String?,
        clientId: String?,
        sellerId: String?,
        search: String?
    ): Result<Unit> {
        return try {
            val response = salesApi.getSales(status, paymentStatus, startDate, endDate, clientId, sellerId, search)
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!
                for (dto in list) {
                    val entity = SaleMapper.toEntity(dto)
                    val details = SaleMapper.toDetailEntities(dto.id, dto.details)
                    val bonuses = SaleMapper.toBonusEntities(dto.id, dto.bonuses)
                    saleDao.insertSale(entity)
                    saleDao.insertDetails(details)
                    saleDao.insertBonuses(bonuses)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun getSaleById(id: String): Result<SaleResponseDto> {
        return try {
            val response = salesApi.getSaleById(id)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val entity = SaleMapper.toEntity(dto)
                val details = SaleMapper.toDetailEntities(dto.id, dto.details)
                val bonuses = SaleMapper.toBonusEntities(dto.id, dto.bonuses)
                saleDao.insertSale(entity)
                saleDao.insertDetails(details)
                saleDao.insertBonuses(bonuses)
                Result.success(dto)
            } else {
                val localEntity = saleDao.getSaleById(id)
                if (localEntity != null) {
                    val details = saleDao.getSaleDetails(id)
                    val bonuses = saleDao.getSaleBonuses(id)
                    Result.success(SaleMapper.toResponseDto(localEntity, details, bonuses))
                } else {
                    Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
                }
            }
        } catch (e: Exception) {
            val localEntity = saleDao.getSaleById(id)
            if (localEntity != null) {
                val details = saleDao.getSaleDetails(id)
                val bonuses = saleDao.getSaleBonuses(id)
                Result.success(SaleMapper.toResponseDto(localEntity, details, bonuses))
            } else {
                Result.failure(Exception(ErrorMapper.mapException(e)))
            }
        }
    }

    override suspend fun updateSale(idempotencyKey: String, id: String, request: CreateSaleRequestDto): Result<SaleResponseDto> {
        return try {
            val response = salesApi.updateSale(idempotencyKey, id, request)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val entity = SaleMapper.toEntity(dto)
                val details = SaleMapper.toDetailEntities(dto.id, dto.details)
                val bonuses = SaleMapper.toBonusEntities(dto.id, dto.bonuses)
                saleDao.insertSale(entity)
                saleDao.insertDetails(details)
                saleDao.insertBonuses(bonuses)
                Result.success(dto)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun confirmSale(idempotencyKey: String, id: String): Result<SaleResponseDto> {
        return try {
            val response = salesApi.confirmSale(idempotencyKey, id)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val entity = SaleMapper.toEntity(dto)
                saleDao.insertSale(entity)
                Result.success(dto)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun cancelSale(idempotencyKey: String, id: String): Result<SaleResponseDto> {
        return try {
            val response = salesApi.cancelSale(idempotencyKey, id)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val entity = SaleMapper.toEntity(dto)
                saleDao.insertSale(entity)
                Result.success(dto)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun sendWhatsapp(id: String, resend: Boolean): Result<Unit> {
        return try {
            val response = salesApi.sendWhatsapp(id, mapOf("resend" to resend))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun downloadPdf(id: String): Result<ResponseBody> {
        return try {
            val response = salesApi.downloadPdf(id)
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
