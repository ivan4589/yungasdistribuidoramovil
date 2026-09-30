package com.example.yungasdistribuidora.presentation.clients.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.domain.model.Client
import com.example.yungasdistribuidora.domain.repository.ClientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientDetailViewModel(
    private val clientRepository: ClientRepository,
    private val clientId: String
) : ViewModel() {

    private val _client = MutableStateFlow<Client?>(null)
    val client = _client.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage = _actionMessage.asStateFlow()

    init {
        loadClient()
    }

    fun loadClient() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = clientRepository.getClientById(clientId)
            result.onSuccess {
                _client.value = it
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al cargar el cliente"
            }
            _isLoading.value = false
        }
    }

    fun deactivateClient(reason: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = clientRepository.deactivateClient(clientId, reason)
            result.onSuccess {
                _client.value = it
                _actionMessage.value = "Cliente desactivado exitosamente"
                onSuccess()
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al desactivar cliente"
            }
            _isLoading.value = false
        }
    }

    fun reactivateClient(reason: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = clientRepository.reactivateClient(clientId, reason)
            result.onSuccess {
                _client.value = it
                _actionMessage.value = "Cliente reactivado exitosamente"
                onSuccess()
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al reactivar cliente"
            }
            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _actionMessage.value = null
    }
}
