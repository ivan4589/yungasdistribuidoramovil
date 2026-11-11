package com.example.yungasdistribuidora.presentation.sales

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.data.remote.dto.*
import com.example.yungasdistribuidora.domain.repository.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class PresaleDetailViewModel(
    private val salesRepository: SalesRepository,
    private val saleId: String,
    private val isAdmin: Boolean
) : ViewModel() {

    private val _sale = MutableStateFlow<SaleResponseDto?>(null)
    val sale = _sale.asStateFlow()

    private val _delivery = MutableStateFlow<DeliveryResponseDto?>(null)
    val delivery = _delivery.asStateFlow()

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
            salesRepository.getDelivery(saleId).onSuccess {
                _delivery.value = it
            }.onFailure {
                // If delivery not started yet or 404, that's fine
            }
        }
    }

    fun startDelivery() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            salesRepository.startDelivery(saleId).onSuccess {
                _delivery.value = it
                _successMessage.value = "Entrega iniciada"
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al iniciar entrega"
            }
            _isLoading.value = false
        }
    }

    fun postDeliveryEvent(type: String, quantity: Int?, productId: String?, observations: String?) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            val req = DeliveryEventRequestDto(type, quantity, productId, observations)
            salesRepository.postDeliveryEvent(idempotencyKey, saleId, req).onSuccess {
                _delivery.value = it
                _successMessage.value = "Evento de entrega registrado"
                idempotencyKey = UUID.randomUUID().toString()
                loadSale()
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al registrar evento de entrega"
            }
            _isLoading.value = false
        }
    }

    fun updateDeliveryQuantity(productId: String, quantity: Int, expectedRevision: Int) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            val req = UpdateDeliveryQuantityRequestDto(productId, quantity, expectedRevision)
            salesRepository.updateDeliveryQuantity(saleId, req).onSuccess {
                _delivery.value = it
                _successMessage.value = "Cantidad actualizada"
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al actualizar cantidad (revisión desactualizada)"
            }
            _isLoading.value = false
        }
    }

    fun confirmSale() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            salesRepository.confirmSale(idempotencyKey, saleId).onSuccess {
                _sale.value = it
                _successMessage.value = "Venta confirmada exitosamente"
                idempotencyKey = UUID.randomUUID().toString()
                loadDelivery()
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al confirmar la venta"
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
            salesRepository.cancelSale(idempotencyKey, saleId).onSuccess {
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
            salesRepository.sendWhatsapp(saleId, resend).onSuccess { summary ->
                _successMessage.value = summary.message()
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
            salesRepository.downloadPdf(saleId).onSuccess { body ->
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
}
