package com.example.yungasdistribuidora.data.repository

import com.example.yungasdistribuidora.data.local.sale.SaleDao
import com.example.yungasdistribuidora.data.mapper.SaleMapper
import com.example.yungasdistribuidora.data.remote.api.SalesApi
import com.example.yungasdistribuidora.data.remote.dto.CreateSaleRequestDto
import com.example.yungasdistribuidora.data.remote.dto.SaleResponseDto
import com.example.yungasdistribuidora.data.remote.dto.*
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
                saleDao.saveCompleteSale(entity, details, bonuses)
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
                    saleDao.saveCompleteSale(entity, details, bonuses)
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
                saleDao.saveCompleteSale(entity, details, bonuses)
                Result.success(dto)
            } else {
                if (response.code() in 400..499) return Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
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
            val response = salesApi.updateSale(idempotencyKey, id, request.forUpdate())
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val entity = SaleMapper.toEntity(dto)
                val details = SaleMapper.toDetailEntities(dto.id, dto.details)
                val bonuses = SaleMapper.toBonusEntities(dto.id, dto.bonuses)
                saleDao.saveCompleteSale(entity, details, bonuses)
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
                saleDao.saveCompleteSale(entity, SaleMapper.toDetailEntities(dto.id, dto.details), SaleMapper.toBonusEntities(dto.id, dto.bonuses))
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
                saleDao.saveCompleteSale(entity, SaleMapper.toDetailEntities(dto.id, dto.details), SaleMapper.toBonusEntities(dto.id, dto.bonuses))
                Result.success(dto)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun sendWhatsapp(id: String, resend: Boolean): Result<WhatsAppDeliverySummary> {
        return try {
            // Read authoritative state; do not send from an outdated offline sale.
            val saleResponse = salesApi.getSaleById(id)
            if (!saleResponse.isSuccessful) return Result.failure(Exception(ErrorMapper.mapHttpCode(saleResponse.code())))
            val sale = saleResponse.body() ?: return Result.failure(Exception("No se pudo leer la venta"))
            if (sale.status != "CONFIRMED") return Result.failure(Exception("Confirma la preventa antes de enviar WhatsApp"))
            val clientResponse = salesApi.getWhatsAppClient(sale.clientId)
            val contactsResponse = salesApi.getWhatsAppContacts(sale.clientId)
            if (!clientResponse.isSuccessful || !contactsResponse.isSuccessful) {
                return Result.failure(Exception("No se pudieron cargar todos los contactos. No se envió ningún mensaje."))
            }
            val client = clientResponse.body() ?: return Result.failure(Exception("Cliente no disponible"))
            val contacts = contactsResponse.body() ?: return Result.failure(Exception("Contactos no disponibles"))
            val recipients = eligibleWhatsAppRecipients(client, contacts)
            if (recipients.isEmpty()) return Result.failure(Exception("No hay contactos activos con teléfono y autorización para WhatsApp"))
            val results = mutableListOf<WhatsAppRecipientResult>()
            for (batch in recipients.chunked(20)) {
                val response = try { salesApi.sendWhatsapp(id, WhatsAppBatchRequest(batch, resend)) }
                catch (e: kotlinx.coroutines.CancellationException) { throw e }
                catch (_: Exception) {
                    results += batch.map { WhatsAppRecipientResult(it, "UNKNOWN", "Conexión interrumpida; revisa el historial antes de reintentar") }
                    continue
                }
                val returned = if (response.isSuccessful) response.body()?.results.orEmpty() else emptyList()
                results += batch.map { recipient ->
                    returned.firstOrNull { it.recipient == recipient }
                        ?: WhatsAppRecipientResult(recipient, "UNKNOWN", "Resultado no confirmado; revisa el historial antes de reintentar")
                }
            }
            Result.success(WhatsAppDeliverySummary(results))
        } catch (e: kotlinx.coroutines.CancellationException) { throw e
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

    override suspend fun startDelivery(id: String): Result<DeliveryResponseDto> {
        return try {
            val response = salesApi.startDelivery(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun getDelivery(id: String): Result<DeliveryResponseDto> {
        return try {
            val response = salesApi.getDelivery(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun postDeliveryEvent(idempotencyKey: String, id: String, request: DeliveryEventRequestDto): Result<DeliveryResponseDto> {
        return try {
            val response = salesApi.postDeliveryEvent(idempotencyKey, id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun updateDeliveryQuantity(id: String, request: UpdateDeliveryQuantityRequestDto): Result<DeliveryResponseDto> {
        return try {
            val response = salesApi.updateDeliveryQuantity(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun getRealtimeAccess(): Result<RealtimeAccessDto> {
        return try {
            val response = salesApi.getRealtimeAccess()
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
