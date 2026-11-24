package com.example.yungasdistribuidora.presentation.sales

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.data.remote.dto.*
import com.example.yungasdistribuidora.domain.model.Product
import com.example.yungasdistribuidora.domain.repository.ClientRepository
import com.example.yungasdistribuidora.domain.repository.ProductRepository
import com.example.yungasdistribuidora.domain.repository.SalesRepository
import com.example.yungasdistribuidora.domain.usecase.CalculateProductPriceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.util.*

data class EditDeliveryLine(
    val productId: String,
    val name: String,
    val ordered: Int,
    val delivered: Int,
    val unitPrice: Double,
    val isNew: Boolean = false
)

class PresaleDetailViewModel(
    private val salesRepository: SalesRepository,
    private val productRepository: ProductRepository?,
    private val clientRepository: ClientRepository,
    private val saleId: String,
    val isAdmin: Boolean,
    private val calculateProductPriceUseCase: CalculateProductPriceUseCase = CalculateProductPriceUseCase()
) : ViewModel() {

    private val _sale = MutableStateFlow<SaleResponseDto?>(null)
    val sale = _sale.asStateFlow()

    private val _delivery = MutableStateFlow<DeliveryResponseDto?>(null)
    val delivery = _delivery.asStateFlow()

    private val _clientType = MutableStateFlow<String?>(null)
    val clientType = _clientType.asStateFlow()

    private val _catalogProducts = MutableStateFlow<List<Product>>(emptyList())
    val catalogProducts = _catalogProducts.asStateFlow()

    private val _editDraftLines = MutableStateFlow<List<EditDeliveryLine>>(emptyList())
    val editDraftLines = _editDraftLines.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    private var idempotencyKey: String = UUID.randomUUID().toString()

    init {
        loadSale()
        loadDelivery()
        loadCatalog()
    }

    private fun loadCatalog() {
        if (productRepository == null) return
        viewModelScope.launch {
            productRepository.getProducts(null, null).collect { list ->
                _catalogProducts.value = list.filter { it.isActive }
            }
        }
    }

    fun initEditDraft(products: List<DeliveryProductDto>, saleDetailsMap: Map<String, Double>) {
        val lines = products.map { p ->
            val pId = p.productId ?: ""
            val price = p.unitPrice ?: saleDetailsMap[pId] ?: 0.0
            EditDeliveryLine(
                productId = pId,
                name = p.name ?: pId,
                ordered = p.ordered ?: 0,
                delivered = p.delivered ?: 0,
                unitPrice = price,
                isNew = false
            )
        }
        _editDraftLines.value = lines
    }

    fun updateDraftItem(productId: String, ordered: Int, unitPrice: Double) {
        val current = _editDraftLines.value.toMutableList()
        val index = current.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(ordered = ordered, unitPrice = unitPrice)
            _editDraftLines.value = current
        }
    }

    fun addProductToDraft(product: Product, ordered: Int, unitPrice: Double) {
        val current = _editDraftLines.value.toMutableList()
        val index = current.indexOfFirst { it.productId == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(ordered = existing.ordered + ordered, unitPrice = if (isAdmin) unitPrice else existing.unitPrice)
        } else {
            current.add(
                EditDeliveryLine(
                    productId = product.id,
                    name = product.name,
                    ordered = ordered,
                    delivered = 0,
                    unitPrice = unitPrice,
                    isNew = true
                )
            )
        }
        _editDraftLines.value = current
    }

    fun removeDraftItem(productId: String) {
        val current = _editDraftLines.value.toMutableList()
        current.removeAll { it.productId == productId }
        _editDraftLines.value = current
    }

    fun calculatePrice(product: Product, quantity: Double): Double {
        return calculateProductPriceUseCase(product, _clientType.value, quantity)
    }

    fun loadSale() {
        viewModelScope.launch {
            _isLoading.value = true
            salesRepository.getSaleById(saleId).onSuccess { sale ->
                _sale.value = sale
                clientRepository.getClientById(sale.clientId).onSuccess { client ->
                    _clientType.value = client.type.name
                }
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al cargar la venta"
            }
            _isLoading.value = false
        }
    }

    fun loadDelivery() {
        viewModelScope.launch {
            salesRepository.getDeliveryDetails(saleId).onSuccess {
                _delivery.value = it
            }.onFailure {
                // Ignore if not started yet
            }
        }
    }

    fun startDelivery() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            salesRepository.startDelivery(idempotencyKey, saleId).onSuccess {
                _delivery.value = it
                _successMessage.value = "Entrega iniciada"
                idempotencyKey = UUID.randomUUID().toString()
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al iniciar entrega"
            }
            _isLoading.value = false
        }
    }

    fun recordDeliveryEvent(productId: String, kind: String, quantity: Int, expectedRevision: Int) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            val req = DeliveryEventRequestDto(productId = productId, kind = kind, quantity = quantity, expectedRevision = expectedRevision)
            salesRepository.recordDeliveryEvent(idempotencyKey, saleId, req).onSuccess {
                _delivery.value = it
                _successMessage.value = if (kind == "DELIVER") "Entrega registrada" else "Devolución registrada"
                idempotencyKey = UUID.randomUUID().toString()
                loadSale()
                loadDelivery()
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Error al registrar evento"
                if (err.message?.contains("409") == true) {
                    loadDelivery()
                }
            }
            _isLoading.value = false
        }
    }

    fun updateDeliveryQuantity(productId: String, quantity: Int, expectedRevision: Int, unitPrice: Double? = null) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            val priceToSend = if (isAdmin && unitPrice != null && unitPrice > 0.0) unitPrice else null
            val req = UpdateDeliveryQuantityRequestDto(productId = productId, quantity = quantity, expectedRevision = expectedRevision, unitPrice = priceToSend)
            salesRepository.updateDeliveryQuantity(idempotencyKey, saleId, req).onSuccess {
                _delivery.value = it
                _successMessage.value = "Cantidad actualizada"
                idempotencyKey = UUID.randomUUID().toString()
                loadSale()
                loadDelivery()
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Error al actualizar cantidad"
                if (err.message?.contains("409") == true) {
                    loadDelivery()
                }
            }
            _isLoading.value = false
        }
    }

    fun saveEditDraft(
        originalProducts: List<DeliveryProductDto>,
        originalPrices: Map<String, Double>,
        initialRevision: Int,
        onComplete: () -> Unit
    ) {
        if (_isLoading.value) return
        val draftLines = _editDraftLines.value
        if (draftLines.isEmpty()) {
            _errorMessage.value = "El pedido no puede quedarse sin productos."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            var currentRevision = initialRevision
            var success = true
            var appliedChanges = 0

            // Send only modified lines. New lines need their ADMIN price explicitly:
            // comparing a new price to itself used to silently discard the override.
            val originalById = originalProducts.associateBy { it.productId }
            for (line in draftLines) {
                val original = originalById[line.productId]
                if (line.productId.isBlank() || line.ordered <= 0 ||
                    !line.unitPrice.isFinite() || line.unitPrice <= 0.0 ||
                    java.math.BigDecimal.valueOf(line.unitPrice).scale() > 2) {
                    _errorMessage.value = "Cantidad o precio inválido: ${line.name}"
                    success = false
                    break
                }
                val originalPrice = originalPrices[line.productId]
                val priceChanged = originalPrice != null && line.unitPrice != originalPrice
                val quantityChanged = original == null || line.ordered != (original.ordered ?: 0)
                if (!quantityChanged && !priceChanged) continue
                val priceToSend = if (isAdmin && (original == null || priceChanged)) line.unitPrice else null

                val req = UpdateDeliveryQuantityRequestDto(
                    productId = line.productId,
                    quantity = line.ordered,
                    expectedRevision = currentRevision,
                    unitPrice = priceToSend
                )
                val result = salesRepository.updateDeliveryQuantity(idempotencyKey, saleId, req)
                if (result.isSuccess) {
                    val deliveryDto = result.getOrNull()
                    if (deliveryDto?.revision != null) {
                        currentRevision = deliveryDto.revision
                    }
                    _delivery.value = deliveryDto
                    appliedChanges++
                } else {
                    val err = result.exceptionOrNull()
                    _errorMessage.value = err?.message ?: "Error al actualizar producto ${line.name}"
                    success = false
                    if (err?.message?.contains("409") == true) {
                        loadDelivery()
                        loadSale()
                    }
                    break
                }
                idempotencyKey = UUID.randomUUID().toString()
            }

            // 2. Process removals (products that were in original delivery but are no longer in draft lines with delivered == 0)
            if (success) {
                val draftIds = draftLines.map { it.productId }.toSet()
                for (orig in originalProducts) {
                    val pId = orig.productId ?: ""
                    if (!draftIds.contains(pId) && (orig.delivered ?: 0) == 0) {
                        val req = UpdateDeliveryQuantityRequestDto(
                            productId = pId,
                            quantity = 0,
                            expectedRevision = currentRevision,
                            unitPrice = null
                        )
                        val result = salesRepository.updateDeliveryQuantity(idempotencyKey, saleId, req)
                        if (result.isSuccess) {
                            val deliveryDto = result.getOrNull()
                            if (deliveryDto?.revision != null) {
                                currentRevision = deliveryDto.revision
                            }
                            _delivery.value = deliveryDto
                            appliedChanges++
                        } else {
                            val err = result.exceptionOrNull()
                            _errorMessage.value = err?.message ?: "Error al retirar producto"
                            success = false
                            break
                        }
                        idempotencyKey = UUID.randomUUID().toString()
                    }
                }
            }

            if (!success && appliedChanges > 0) {
                _errorMessage.value = "Se aplicaron $appliedChanges modificaciones antes de un error. " +
                    "Actualiza el pedido y verifica los cambios antes de reintentar."
                loadSale()
                loadDelivery()
            }
            if (success) {
                _successMessage.value = "Pedido actualizado correctamente"
                loadSale()
                loadDelivery()
                onComplete()
            }
            _isLoading.value = false
        }
    }

    fun confirmSale() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            val apiCall = if (_delivery.value?.deliveryMode == true) {
                salesRepository.confirmDelivery(idempotencyKey, saleId)
            } else {
                salesRepository.confirmSale(idempotencyKey, saleId)
            }
            apiCall.onSuccess {
                _sale.value = it
                _successMessage.value = "Venta confirmada exitosamente"
                idempotencyKey = UUID.randomUUID().toString()
                loadDelivery()
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Error al confirmar la venta"
            }
            _isLoading.value = false
        }
    }

    fun cancelSale() {
        if (!isAdmin) {
            _errorMessage.value = "Solo el administrador puede cancelar preventas"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            salesRepository.cancelSale(idempotencyKey, saleId, "Cancelado por el administrador").onSuccess {
                _sale.value = it
                _successMessage.value = "Preventa cancelada"
                idempotencyKey = UUID.randomUUID().toString()
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al cancelar la preventa"
            }
            _isLoading.value = false
        }
    }

    fun sendWhatsapp(resend: Boolean) {
        if (_isLoading.value || _sale.value?.status != "CONFIRMED") return
        viewModelScope.launch {
            _isLoading.value = true
            salesRepository.sendWhatsapp(saleId, resend).onSuccess {
                _successMessage.value = "Mensaje de WhatsApp enviado"
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al enviar WhatsApp"
            }
            _isLoading.value = false
        }
    }

    fun downloadAndOpenPdf(context: Context, destination: Uri) {
        if (_isLoading.value || _sale.value?.status != "CONFIRMED") return
        viewModelScope.launch {
            _isLoading.value = true
            val isCancelled = _sale.value?.status == "CANCELLED"
            salesRepository.downloadPdf(saleId, isCancelled).onSuccess { body ->
                try {
                    withContext(Dispatchers.IO) {
                        body.use {
                            val bytes = it.bytes()
                            require(bytes.size >= 5 && bytes.take(5).toByteArray().toString(Charsets.US_ASCII) == "%PDF-") {
                                "El servidor no devolvió un PDF válido"
                            }
                            val output = context.contentResolver.openOutputStream(destination, "w")
                                ?: error("No se pudo abrir el destino seleccionado")
                            output.use { stream -> stream.write(bytes) }
                        }
                    }
                    _successMessage.value = "PDF descargado en la ubicación seleccionada"
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(destination, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, "Ver PDF de preventa"))
                    } catch (e: ActivityNotFoundException) {
                        _successMessage.value = "PDF guardado. Instala un lector de PDF para abrirlo."
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error al abrir el PDF: ${e.message}"
                }
            }.onFailure { err ->
                _errorMessage.value = "Fallo al descargar PDF: ${err.message}"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    companion object {
        fun String.normalize(): String {
            val normalizer = Normalizer.normalize(this, Normalizer.Form.NFD)
            return Regex("\\p{InCombiningDiacriticalMarks}+").replace(normalizer, "").lowercase()
        }
    }
}
