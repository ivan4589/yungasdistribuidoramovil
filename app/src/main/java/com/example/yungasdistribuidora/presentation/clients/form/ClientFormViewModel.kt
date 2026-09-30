package com.example.yungasdistribuidora.presentation.clients.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.data.remote.dto.client.CreateClientRequest
import com.example.yungasdistribuidora.domain.model.ClientType
import com.example.yungasdistribuidora.domain.model.Location
import com.example.yungasdistribuidora.domain.repository.ClientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientFormViewModel(
    private val clientRepository: ClientRepository,
    private val clientId: String? = null
) : ViewModel() {

    var fullName = MutableStateFlow("")
    var alias = MutableStateFlow("")
    var selectedType = MutableStateFlow(ClientType.NORMAL)
    var selectedLocationId = MutableStateFlow("")
    var phone = MutableStateFlow("")
    var whatsappConsent = MutableStateFlow(false)
    var additionalInfo = MutableStateFlow("")

    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations = _locations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess = _saveSuccess.asStateFlow()

    init {
        loadLocations()
        if (clientId != null) {
            loadClient(clientId)
        }
    }

    private fun loadLocations() {
        viewModelScope.launch {
            clientRepository.getLocations().onSuccess { locs ->
                _locations.value = locs
                if (selectedLocationId.value.isBlank() && locs.isNotEmpty()) {
                    selectedLocationId.value = locs.first().id
                }
            }
        }
    }

    private fun loadClient(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            clientRepository.getClientById(id).onSuccess { client ->
                fullName.value = client.fullName
                alias.value = client.alias ?: ""
                selectedType.value = client.type
                selectedLocationId.value = client.locationId
                phone.value = client.phone ?: ""
                whatsappConsent.value = client.whatsappConsent
                additionalInfo.value = client.additionalInfo ?: ""
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al cargar cliente para edición"
            }
            _isLoading.value = false
        }
    }

    fun saveClient() {
        val name = fullName.value.trim()
        if (name.isEmpty()) {
            _errorMessage.value = "El nombre completo es obligatorio"
            return
        }
        if (name.length > 160) {
            _errorMessage.value = "El nombre no puede exceder los 160 caracteres"
            return
        }
        if (selectedLocationId.value.isBlank()) {
            _errorMessage.value = "Debe seleccionar una localidad"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null

            if (clientId == null) {
                // Create
                val request = CreateClientRequest(
                    fullName = name,
                    alias = alias.value.takeIf { it.isNotBlank() },
                    type = selectedType.value.name,
                    locationId = selectedLocationId.value,
                    phone = phone.value.takeIf { it.isNotBlank() },
                    whatsappConsent = whatsappConsent.value,
                    additionalInfo = additionalInfo.value.takeIf { it.isNotBlank() }
                )
                clientRepository.createClient(request).onSuccess {
                    _saveSuccess.value = true
                }.onFailure {
                    _errorMessage.value = it.message ?: "Error al crear cliente"
                }
            } else {
                // Update
                val updateMap = mapOf(
                    "fullName" to name,
                    "alias" to alias.value.takeIf { it.isNotBlank() },
                    "type" to selectedType.value.name,
                    "locationId" to selectedLocationId.value,
                    "phone" to phone.value.takeIf { it.isNotBlank() },
                    "whatsappConsent" to whatsappConsent.value,
                    "additionalInfo" to additionalInfo.value.takeIf { it.isNotBlank() }
                )
                clientRepository.updateClient(clientId, updateMap).onSuccess {
                    _saveSuccess.value = true
                }.onFailure {
                    _errorMessage.value = it.message ?: "Error al actualizar cliente"
                }
            }
            _isSaving.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
