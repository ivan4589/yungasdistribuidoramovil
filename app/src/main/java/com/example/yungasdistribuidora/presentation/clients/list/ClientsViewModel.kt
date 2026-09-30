package com.example.yungasdistribuidora.presentation.clients.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.domain.model.Client
import com.example.yungasdistribuidora.domain.model.ClientType
import com.example.yungasdistribuidora.domain.model.Location
import com.example.yungasdistribuidora.domain.repository.ClientRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClientsViewModel(
    private val clientRepository: ClientRepository,
    private val isAdmin: Boolean
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<ClientType?>(null)
    val selectedType = _selectedType.asStateFlow()

    private val _selectedLocationId = MutableStateFlow<String?>(null)
    val selectedLocationId = _selectedLocationId.asStateFlow()

    private val _includeInactive = MutableStateFlow(false)
    val includeInactive = _includeInactive.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations = _locations.asStateFlow()

    val lastSyncTime: StateFlow<Long?> = clientRepository.getLastSyncTime()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _clientsFlow = combine(
        _includeInactive,
        _selectedLocationId,
        _selectedType
    ) { inactive, locId, type ->
        Triple(inactive, locId, type)
    }.flatMapLatest { (inactive, locId, type) ->
        val effectiveInactive = if (isAdmin) inactive else false
        clientRepository.getClients(effectiveInactive, locId, type)
    }

    val clients: StateFlow<List<Client>> = combine(
        _clientsFlow,
        _searchQuery
    ) { clientList, query ->
        if (query.isBlank()) {
            clientList
        } else {
            val q = query.lowercase().trim()
            clientList.filter { client ->
                client.fullName.lowercase().contains(q) ||
                (client.alias?.lowercase()?.contains(q) == true) ||
                (client.phone?.contains(q) == true) ||
                (client.locationName?.lowercase()?.contains(q) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadLocations()
        refreshData(isInitial = true)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedType(type: ClientType?) {
        _selectedType.value = type
    }

    fun setSelectedLocationId(locId: String?) {
        _selectedLocationId.value = locId
    }

    fun setIncludeInactive(include: Boolean) {
        if (isAdmin) {
            _includeInactive.value = include
            refreshData(isInitial = false)
        }
    }

    fun loadLocations() {
        viewModelScope.launch {
            clientRepository.getLocations().onSuccess { locs ->
                _locations.value = locs
            }
        }
    }

    fun refreshData(isInitial: Boolean = false) {
        viewModelScope.launch {
            if (isInitial) _isLoading.value = true else _isRefreshing.value = true
            _errorMessage.value = null

            val effectiveInactive = if (isAdmin) _includeInactive.value else false
            val result = clientRepository.refreshClients(effectiveInactive, _selectedLocationId.value, _selectedType.value)
            clientRepository.refreshLocations()

            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al actualizar clientes"
            }

            if (isInitial) _isLoading.value = false else _isRefreshing.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
