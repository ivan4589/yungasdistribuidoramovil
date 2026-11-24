package com.example.yungasdistribuidora.presentation.sales

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.data.remote.dto.*
import com.example.yungasdistribuidora.domain.model.Product
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

class PresaleDetailViewModel(
    private val salesRepository: SalesRepository,
    private val productRepository: ProductRepository?,
    private val saleId: String,
    val isAdmin: Boolean,
    private val calculateProductPriceUseCase: CalculateProductPriceUseCase = CalculateProductPriceUseCase()
) : ViewModel() {

    private val _sale = MutableStateFlow<SaleResponseDto?>(null)
    val sale = _sale.asStateFlow()

    private val _delivery = MutableStateFlow<DeliveryResponseDto?>(null)
    val delivery = _delivery.asStateFlow()

    private val _catalogProducts = MutableStateFlow<List<Product>>(emptyList())
    val catalogProducts = _catalogProducts.asStateFlow()

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

    fun calculatePrice(product: Product, quantity: Double): Double {
        val clientType = _sale.value?.saleType // or client type if available
        return calculateProductPriceUseCase(product, null, quantity)
    }

    fun loadSale() {
        viewModelScope.launch {
            _isLoading.value = true
            salesRepository.getSaleById(saleId).onSuccess {
                _sale.value = it
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

    fun updateDeliveryQuantities(
        updates: Map<String, Int>,
        unitPrices: Map<String, Double>,
        originalPrices: Map<String, Double>,
        initialRevision: Int,
        onComplete: () -> Unit
    ) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            var currentRevision = initialRevision
            var success = true
            for ((productId, qty) in updates) {
                val newPrice = unitPrices[productId]
                val origPrice = originalPrices[productId]
                val priceToSend = if (isAdmin && newPrice != null && newPrice != origPrice && newPrice > 0.0) newPrice else null

                val req = UpdateDeliveryQuantityRequestDto(
                    productId = productId,
                    quantity = qty,
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
                } else {
                    val err = result.exceptionOrNull()
                    _errorMessage.value = err?.message ?: "Error al actualizar producto $productId"
                    success = false
                    if (err?.message?.contains("409") == true) {
                        loadDelivery()
                        loadSale()
                    }
                    break
                }
                idempotencyKey = UUID.randomUUID().toString()
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
