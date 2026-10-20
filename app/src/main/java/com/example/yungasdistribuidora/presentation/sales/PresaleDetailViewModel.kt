package com.example.yungasdistribuidora.presentation.sales

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.data.remote.dto.SaleResponseDto
import com.example.yungasdistribuidora.domain.repository.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
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

    fun downloadAndOpenPdf(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            salesRepository.downloadPdf(saleId).onSuccess { body ->
                try {
                    val file = File(context.cacheDir, "preventa_$saleId.pdf")
                    body.byteStream().use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Ver PDF de preventa"))
                } catch (e: Exception) {
                    _errorMessage.value = "Error al abrir el PDF: ${e.message}"
                }
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al descargar el PDF"
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
