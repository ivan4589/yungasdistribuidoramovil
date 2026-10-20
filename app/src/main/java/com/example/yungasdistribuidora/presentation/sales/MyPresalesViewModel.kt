package com.example.yungasdistribuidora.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.data.remote.dto.SaleResponseDto
import com.example.yungasdistribuidora.domain.repository.SalesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MyPresalesViewModel(
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _sales = MutableStateFlow<List<SaleResponseDto>>(emptyList())
    val sales = _sales.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter = _statusFilter.asStateFlow()

    private val _paymentStatusFilter = MutableStateFlow<String?>(null)
    val paymentStatusFilter = _paymentStatusFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    val filteredSales = combine(_sales, _searchQuery, _statusFilter, _paymentStatusFilter) { list, query, status, payStatus ->
        list.filter { sale ->
            val matchesQuery = query.isBlank() ||
                sale.saleNumber.lowercase().contains(query.lowercase()) ||
                (sale.clientName?.lowercase()?.contains(query.lowercase()) == true) ||
                (sale.observations?.lowercase()?.contains(query.lowercase()) == true)

            val matchesStatus = status.isNullOrBlank() || sale.status == status
            val matchesPayStatus = payStatus.isNullOrBlank() || sale.paymentStatus == payStatus

            matchesQuery && matchesStatus && matchesPayStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        observeSales()
        refreshSales()
    }

    private fun observeSales() {
        salesRepository.getSales()
            .onEach { _sales.value = it }
            .launchIn(viewModelScope)
    }

    fun refreshSales() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            salesRepository.refreshSales().onFailure {
                _errorMessage.value = it.message ?: "Error al actualizar preventas"
            }
            _isLoading.value = false
        }
    }

    fun loadSales() {
        refreshSales()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String?) {
        _statusFilter.value = status
    }

    fun setPaymentStatusFilter(payStatus: String?) {
        _paymentStatusFilter.value = payStatus
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
