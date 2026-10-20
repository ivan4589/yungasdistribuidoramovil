package com.example.yungasdistribuidora.presentation.presale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.data.local.bonus.BonusRuleEntity
import com.example.yungasdistribuidora.data.remote.dto.BonusAllocationRequestDto
import com.example.yungasdistribuidora.data.remote.dto.SaleResponseDto
import com.example.yungasdistribuidora.data.remote.dto.client.CreateClientRequest
import com.example.yungasdistribuidora.domain.model.*
import com.example.yungasdistribuidora.domain.repository.BonusRuleRepository
import com.example.yungasdistribuidora.domain.repository.ClientRepository
import com.example.yungasdistribuidora.domain.repository.PresaleRepository
import com.example.yungasdistribuidora.domain.repository.ProductRepository
import com.example.yungasdistribuidora.domain.usecase.CalculateBonusUseCase
import com.example.yungasdistribuidora.domain.usecase.CalculateProductPriceUseCase
import com.example.yungasdistribuidora.domain.usecase.RegisterPresaleUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class NewPresaleViewModel(
    private val clientRepository: ClientRepository,
    private val productRepository: ProductRepository,
    private val presaleRepository: PresaleRepository,
    private val bonusRuleRepository: BonusRuleRepository,
    private val isAdmin: Boolean = false,
    private val calculateProductPriceUseCase: CalculateProductPriceUseCase = CalculateProductPriceUseCase(),
    private val calculateBonusUseCase: CalculateBonusUseCase = CalculateBonusUseCase(),
    private val registerPresaleUseCase: RegisterPresaleUseCase = RegisterPresaleUseCase()
) : ViewModel() {

    private val _draft = MutableStateFlow(PresaleDraft())
    val draft = _draft.asStateFlow()

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients = _clients.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations = _locations.asStateFlow()

    private val _bonusRules = MutableStateFlow<List<BonusRuleEntity>>(emptyList())
    val bonusRules = _bonusRules.asStateFlow()

    val earnedBonuses: StateFlow<Map<String, Int>> = combine(_bonusRules, _draft) { rules, draft ->
        calculateBonusUseCase.calculate(rules, draft.items)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _allocatedBonuses = MutableStateFlow<Map<String, Int>>(emptyMap())
    val allocatedBonuses = _allocatedBonuses.asStateFlow()

    private val _clientSearchQuery = MutableStateFlow("")
    val clientSearchQuery = _clientSearchQuery.asStateFlow()

    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery = _productSearchQuery.asStateFlow()

    var selectedProductForAdd = MutableStateFlow<Product?>(null)
    var addQuantityInput = MutableStateFlow("1")
    var addPriceInput = MutableStateFlow("")

    var discountInput = MutableStateFlow("0")
    var paymentMethodInput = MutableStateFlow("CASH")
    var paymentReferenceInput = MutableStateFlow("")
    var dueDateInput = MutableStateFlow("")

    private val _isClientFormOpen = MutableStateFlow(false)
    val isClientFormOpen = _isClientFormOpen.asStateFlow()

    private val _isConfirmDialogVisible = MutableStateFlow(false)
    val isConfirmDialogVisible = _isConfirmDialogVisible.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _registeredSale = MutableStateFlow<SaleResponseDto?>(null)
    val registeredSale = _registeredSale.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private var idempotencyKey: String = UUID.randomUUID().toString()

    val filteredClients = combine(_clients, _clientSearchQuery) { list, query ->
        if (query.isBlank()) list
        else {
            val q = query.lowercase().trim()
            list.filter { it.fullName.lowercase().contains(q) || (it.alias?.lowercase()?.contains(q) == true) || (it.phone?.contains(q) == true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredProducts = combine(_products, _productSearchQuery) { list, query ->
        val inStockList = list.filter { it.availableStock > 0.0 }
        if (query.isBlank()) inStockList
        else {
            val q = query.lowercase().trim()
            inStockList.filter { it.name.lowercase().contains(q) || it.code.lowercase().contains(q) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subtotal: StateFlow<Double> = _draft.map { d ->
        d.items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalAmount: StateFlow<Double> = combine(
        subtotal,
        discountInput
    ) { sub, disc ->
        val discountVal = disc.toDoubleOrNull() ?: 0.0
        maxOf(sub - discountVal, 0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            launch {
                clientRepository.getClients(includeInactive = false, locationId = null, type = null).collect {
                    _clients.value = it
                }
            }
            launch {
                productRepository.getProducts(null, null).collect {
                    _products.value = it
                }
            }
            launch {
                clientRepository.getLocations().onSuccess {
                    _locations.value = it
                }
            }
            launch {
                bonusRuleRepository.getBonusRules().onSuccess {
                    _bonusRules.value = it
                }
            }
            _isLoading.value = false
        }
    }

    fun setClientSearchQuery(query: String) {
        _clientSearchQuery.value = query
    }

    fun setProductSearchQuery(query: String) {
        _productSearchQuery.value = query
    }

    fun selectClient(client: Client?) {
        val currentDraft = _draft.value
        val updatedItems = currentDraft.items.map { item ->
            val newPrice = calculateProductPriceUseCase(item.product, client?.type?.name, item.quantity.toDouble())
            item.copy(unitPrice = newPrice)
        }
        _draft.value = currentDraft.copy(client = client, items = updatedItems)
    }

    fun setModality(modality: SaleModality) {
        _draft.value = _draft.value.copy(modality = modality)
    }

    fun setObservations(obs: String) {
        _draft.value = _draft.value.copy(observations = obs)
    }

    fun selectProductForAdd(product: Product?) {
        selectedProductForAdd.value = product
        if (product != null) {
            val clientType = _draft.value.client?.type?.name
            val qty = addQuantityInput.value.toIntOrNull() ?: 1
            val calculatedPrice = calculateProductPriceUseCase(product, clientType, qty.toDouble())
            addPriceInput.value = calculatedPrice.toString()
        } else {
            addPriceInput.value = ""
        }
    }

    fun updateAddQuantity(qtyStr: String) {
        addQuantityInput.value = qtyStr
        val prod = selectedProductForAdd.value
        if (prod != null) {
            val clientType = _draft.value.client?.type?.name
            val qty = qtyStr.toIntOrNull() ?: 1
            val calculatedPrice = calculateProductPriceUseCase(prod, clientType, qty.toDouble())
            addPriceInput.value = calculatedPrice.toString()
        }
    }

    fun addSelectedProductToPresale() {
        val prod = selectedProductForAdd.value ?: return
        val qty = addQuantityInput.value.toIntOrNull() ?: 1
        if (qty < 1) return
        val price = addPriceInput.value.toDoubleOrNull() ?: prod.priceNormal

        val finalPrice = if (isAdmin) price else calculateProductPriceUseCase(prod, _draft.value.client?.type?.name, qty.toDouble())

        val currentDraft = _draft.value
        val existingItemIndex = currentDraft.items.indexOfFirst { it.product.id == prod.id }
        val newItems = if (existingItemIndex >= 0) {
            currentDraft.items.toMutableList().apply {
                val existing = this[existingItemIndex]
                val newQty = existing.quantity + qty
                val updatedPrice = if (isAdmin) finalPrice else calculateProductPriceUseCase(prod, currentDraft.client?.type?.name, newQty.toDouble())
                this[existingItemIndex] = existing.copy(quantity = newQty, unitPrice = updatedPrice)
            }
        } else {
            currentDraft.items + PresaleItem(prod, qty, finalPrice)
        }
        _draft.value = currentDraft.copy(items = newItems)

        selectedProductForAdd.value = null
        addQuantityInput.value = "1"
        addPriceInput.value = ""
    }

    fun updateItemDetails(productId: String, quantity: Int, unitPrice: Double) {
        if (quantity <= 0) {
            removeItem(productId)
            return
        }
        val currentDraft = _draft.value
        val newItems = currentDraft.items.map { item ->
            if (item.product.id == productId) {
                val finalPrice = if (isAdmin) unitPrice.coerceAtLeast(0.0) else item.unitPrice
                item.copy(quantity = quantity.coerceAtLeast(1), unitPrice = finalPrice)
            } else item
        }
        _draft.value = currentDraft.copy(items = newItems)
    }

    fun removeItem(productId: String) {
        val currentDraft = _draft.value
        val newItems = currentDraft.items.filter { it.product.id != productId }
        _draft.value = currentDraft.copy(items = newItems)
    }

    fun updateBonusAllocation(productId: String, quantity: Int) {
        val current = _allocatedBonuses.value.toMutableMap()
        if (quantity <= 0) {
            current.remove(productId)
        } else {
            current[productId] = quantity
        }
        _allocatedBonuses.value = current
    }

    fun openClientForm() {
        _isClientFormOpen.value = true
    }

    fun closeClientForm() {
        _isClientFormOpen.value = false
    }

    fun showConfirmDialog() {
        _isConfirmDialogVisible.value = true
    }

    fun hideConfirmDialog() {
        _isConfirmDialogVisible.value = false
    }

    fun createNewClient(
        fullName: String,
        alias: String?,
        type: ClientType,
        locationId: String,
        phone: String?,
        whatsappConsent: Boolean,
        additionalInfo: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val request = CreateClientRequest(
                fullName = fullName,
                alias = alias,
                type = type.name,
                locationId = locationId,
                phone = phone,
                whatsappConsent = whatsappConsent,
                additionalInfo = additionalInfo
            )
            clientRepository.createClient(request).onSuccess { createdClient ->
                selectClient(createdClient)
                _isClientFormOpen.value = false
                onSuccess()
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al registrar el cliente"
            }
        }
    }

    fun registerSale() {
        if (_isSubmitting.value) return
        val discVal = discountInput.value.toDoubleOrNull() ?: 0.0

        val allocatedList = _allocatedBonuses.value.map { (prodId, qty) -> BonusAllocationRequestDto(prodId, qty) }
        val productSubCategoryMap = _products.value.associate { it.id to (it.subCategoryId ?: "") }

        val validationResult = registerPresaleUseCase.validateAndBuildRequest(
            draft = _draft.value,
            isAdmin = isAdmin,
            discountInput = discVal,
            paymentMethod = paymentMethodInput.value,
            paymentReference = paymentReferenceInput.value,
            dueDateStr = dueDateInput.value,
            allocatedBonuses = allocatedList,
            earnedBonusesMap = earnedBonuses.value,
            productSubCategoryMap = productSubCategoryMap
        )

        if (validationResult.isFailure) {
            _errorMessage.value = validationResult.exceptionOrNull()?.message ?: "Error de validación"
            _isConfirmDialogVisible.value = false
            return
        }

        val requestDto = validationResult.getOrThrow()

        viewModelScope.launch {
            _isSubmitting.value = true
            _isConfirmDialogVisible.value = false
            _errorMessage.value = null

            val result = presaleRepository.registerSale(idempotencyKey, requestDto)
            result.onSuccess { saleResponse ->
                _registeredSale.value = saleResponse
                _draft.value = PresaleDraft()
                _allocatedBonuses.value = emptyMap()
                discountInput.value = "0"
                idempotencyKey = UUID.randomUUID().toString()
                productRepository.refreshCatalog()
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al registrar la preventa"
            }
            _isSubmitting.value = false
        }
    }

    fun resetRegisteredSale() {
        _registeredSale.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
