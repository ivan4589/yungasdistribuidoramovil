package com.example.yungasdistribuidora.data.repository

import com.example.yungasdistribuidora.data.local.sale.SaleDao
import com.example.yungasdistribuidora.data.mapper.SaleMapper
import com.example.yungasdistribuidora.data.remote.api.ClientApi
import com.example.yungasdistribuidora.data.remote.api.SalesApi
import com.example.yungasdistribuidora.data.remote.dto.*
import com.example.yungasdistribuidora.domain.repository.SalesRepository
import com.example.yungasdistribuidora.util.ErrorMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

class SalesRepositoryImpl(
    private val salesApi: SalesApi,
    private val clientApi: ClientApi,
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
                Result.failure(Exception(extractSafeBackendMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override fun getSales(
        status: String?,
        paymentStatus: String?,
        dateFrom: String?,
        dateTo: String?,
        clientId: String?
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
        dateFrom: String?,
        dateTo: String?,
        clientId: String?
    ): Result<Unit> {
        return try {
            val response = salesApi.getSales(status, paymentStatus, dateFrom, dateTo, clientId)
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
                Result.failure(Exception(extractSafeBackendMessage(response)))
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
                    Result.failure(Exception(extractSafeBackendMessage(response)))
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
                Result.failure(Exception(extractSafeBackendMessage(response)))
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
                Result.failure(Exception(extractSafeBackendMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun cancelSale(idempotencyKey: String, id: String, reason: String): Result<SaleResponseDto> {
        return try {
            val response = salesApi.cancelSale(idempotencyKey, id, CancelSaleRequestDto(reason))
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val entity = SaleMapper.toEntity(dto)
                saleDao.insertSale(entity)
                Result.success(dto)
            } else {
                Result.failure(Exception(extractSafeBackendMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun sendWhatsapp(id: String, resend: Boolean): Result<WhatsAppDeliverySummary> {
        return try {
            val saleResponse = salesApi.getSaleById(id)
            if (!saleResponse.isSuccessful || saleResponse.body() == null) {
                return Result.failure(Exception(extractSafeBackendMessage(saleResponse)))
            }
            val sale = saleResponse.body()!!
            if (sale.status != "CONFIRMED") {
                return Result.failure(Exception("La venta debe estar confirmada para enviar el recibo por WhatsApp."))
            }
            val clientId = sale.clientId ?: return Result.failure(Exception("Preventa sin cliente asignado."))

            val clientResponse = clientApi.getClientById(clientId)
            if (!clientResponse.isSuccessful || clientResponse.body() == null) {
                return Result.failure(Exception(extractSafeBackendMessage(clientResponse)))
            }
            val clientDto = clientResponse.body()!!
            val whatsappClient = WhatsAppClientDto(clientDto.phone, clientDto.whatsappConsent)

            val contactsResponse = clientApi.getClientContacts(clientId)
            if (!contactsResponse.isSuccessful || contactsResponse.body() == null) {
                return Result.failure(Exception(extractSafeBackendMessage(contactsResponse)))
            }
            val contacts = contactsResponse.body()!!.map { WhatsAppContactDto(it.id, it.name, it.phone, it.isActive, it.whatsappConsent) }

            val recipients = eligibleWhatsAppRecipients(whatsappClient, contacts)
            if (recipients.isEmpty()) {
                return Result.failure(Exception("El cliente y sus contactos no tienen teléfonos con consentimiento de WhatsApp."))
            }

            val batchResponse = salesApi.sendWhatsappBatch(id, WhatsAppBatchRequest(recipients, resend))
            if (batchResponse.isSuccessful && batchResponse.body() != null) {
                val batchResult = batchResponse.body()!!
                val results = batchResult.results ?: recipients.map { WhatsAppRecipientResult(it, "UNKNOWN", "Sin respuesta del servidor") }
                Result.success(WhatsAppDeliverySummary(results))
            } else {
                Result.failure(Exception(extractSafeBackendMessage(batchResponse)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun downloadPdf(id: String, cancelled: Boolean): Result<ResponseBody> {
        return try {
            val response = salesApi.downloadPdf(
                id = id,
                cancelled = if (cancelled) true else null
            )

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception(extractSafeBackendMessage(response))
                )
            }

            val body = response.body()
                ?: return Result.failure(
                    Exception("El comprobante está vacío")
                )

            val contentType = body.contentType()?.toString().orEmpty()

            if (!contentType.contains("application/pdf", ignoreCase = true)) {
                body.close()
                return Result.failure(
                    Exception("El servidor no devolvió un archivo PDF válido")
                )
            }

            Result.success(body)
        } catch (e: Exception) {
            Result.failure(
                Exception(ErrorMapper.mapException(e))
            )
        }
    }

    override suspend fun startDelivery(idempotencyKey: String, id: String): Result<DeliveryResponseDto> {
        return try {
            val response = salesApi.startDelivery(idempotencyKey, id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(extractSafeBackendMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun getDeliveryDetails(id: String): Result<DeliveryResponseDto> {
        return try {
            val response = salesApi.getDeliveryDetails(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(extractSafeBackendMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun recordDeliveryEvent(idempotencyKey: String, id: String, request: DeliveryEventRequestDto): Result<DeliveryResponseDto> {
        return try {
            val response = salesApi.recordDeliveryEvent(idempotencyKey, id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else if (response.code() == 409) {
                Result.failure(Exception("Conflicto de revisión (409). Por favor recargue el pedido."))
            } else {
                Result.failure(Exception(extractSafeBackendMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun updateDeliveryQuantity(idempotencyKey: String, id: String, request: UpdateDeliveryQuantityRequestDto): Result<DeliveryResponseDto> {
        return try {
            val response = salesApi.updateDeliveryQuantity(idempotencyKey, id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else if (response.code() == 409) {
                Result.failure(Exception("Conflicto de revisión (409). Por favor recargue el pedido."))
            } else {
                Result.failure(Exception(extractSafeBackendMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun confirmDelivery(idempotencyKey: String, id: String): Result<SaleResponseDto> {
        return try {
            val response = salesApi.confirmDelivery(idempotencyKey, id)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val entity = SaleMapper.toEntity(dto)
                saleDao.insertSale(entity)
                Result.success(dto)
            } else {
                Result.failure(Exception(extractSafeBackendMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    private fun extractSafeBackendMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                try {
                    val json = JSONObject(errorBody)
                    val msg = json.optString("message")
                    if (msg.isNotBlank()) return msg
                    val err = json.optString("error")
                    if (err.isNotBlank()) return err
                } catch (e: Exception) {
                    // Not JSON
                }
                errorBody.take(200)
            } else {
                ErrorMapper.mapHttpCode(response.code())
            }
        } catch (e: Exception) {
            ErrorMapper.mapHttpCode(response.code())
        }
    }
}
