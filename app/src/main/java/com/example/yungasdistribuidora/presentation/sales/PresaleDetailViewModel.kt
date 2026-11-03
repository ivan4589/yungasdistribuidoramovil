package com.example.yungasdistribuidora.presentation.sales

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.data.remote.dto.SaleResponseDto
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    private var idempotencyKey: String = UUID.randomUUID().toString()

    init {
        loadSale()
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

    fun confirmSale() {
        if (_isLoading.value || _sale.value?.status != "PENDING") return
        viewModelScope.launch {
            _isLoading.value = true
            salesRepository.confirmSale(idempotencyKey, saleId).onSuccess {
                _sale.value = it
                _successMessage.value = "Preventa confirmada exitosamente"
                idempotencyKey = UUID.randomUUID().toString()
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al confirmar la preventa"
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
