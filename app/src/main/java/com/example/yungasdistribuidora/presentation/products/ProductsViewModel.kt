package com.example.yungasdistribuidora.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.domain.model.AvailabilityStatus
import com.example.yungasdistribuidora.domain.model.Category
import com.example.yungasdistribuidora.domain.model.Product
import com.example.yungasdistribuidora.domain.model.SubCategory
import com.example.yungasdistribuidora.domain.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ProductSortOption {
    NAME_AZ,
    NAME_ZA,
    PRICE_LOW_HIGH,
    PRICE_HIGH_LOW,
    STOCK_HIGH_LOW
}

enum class AvailabilityFilter {
    ALL,
    AVAILABLE,
    LOW_STOCK,
    OUT_OF_STOCK
}

class ProductsViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _selectedSubCategoryId = MutableStateFlow<String?>(null)
    val selectedSubCategoryId = _selectedSubCategoryId.asStateFlow()

    private val _availabilityFilter = MutableStateFlow(AvailabilityFilter.ALL)
    val availabilityFilter = _availabilityFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(ProductSortOption.NAME_AZ)
    val sortOption = _sortOption.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _subCategories = MutableStateFlow<List<SubCategory>>(emptyList())
    val subCategories = _subCategories.asStateFlow()

    val lastSyncTime: StateFlow<Long?> = productRepository.getLastSyncTime()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _rawProducts = combine(
        _selectedCategoryId,
        _selectedSubCategoryId
    ) { catId, subCatId ->
        Pair(catId, subCatId)
    }.flatMapLatest { (catId, subCatId) ->
        productRepository.getProducts(catId, subCatId)
    }

    val products: StateFlow<List<Product>> = combine(
        _rawProducts,
        _searchQuery,
        _availabilityFilter,
        _sortOption
    ) { list, query, avFilter, sort ->
        var result = list

        // Search filter
        if (query.isNotBlank()) {
            val q = query.lowercase().trim()
            result = result.filter { p ->
                p.name.lowercase().contains(q) || p.code.lowercase().contains(q)
            }
        }

        // Availability filter
        result = when (avFilter) {
            AvailabilityFilter.AVAILABLE -> result.filter { it.availabilityStatus == AvailabilityStatus.AVAILABLE }
            AvailabilityFilter.LOW_STOCK -> result.filter { it.availabilityStatus == AvailabilityStatus.LOW_STOCK }
            AvailabilityFilter.OUT_OF_STOCK -> result.filter { it.availabilityStatus == AvailabilityStatus.OUT_OF_STOCK }
            AvailabilityFilter.ALL -> result
        }

        // Sorting
        result = when (sort) {
            ProductSortOption.NAME_AZ -> result.sortedBy { it.name }
            ProductSortOption.NAME_ZA -> result.sortedByDescending { it.name }
            ProductSortOption.PRICE_LOW_HIGH -> result.sortedBy { it.priceNormal }
            ProductSortOption.PRICE_HIGH_LOW -> result.sortedByDescending { it.priceNormal }
            ProductSortOption.STOCK_HIGH_LOW -> result.sortedByDescending { it.availableStock }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadCategoriesAndSubCategories()
        refreshCatalog(isInitial = true)
    }

    private fun loadCategoriesAndSubCategories() {
        viewModelScope.launch {
            productRepository.getCategories().collect { cats ->
                _categories.value = cats
            }
        }
        viewModelScope.launch {
            productRepository.getSubCategories(null).collect { subCats ->
                _subCategories.value = subCats
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(catId: String?) {
        _selectedCategoryId.value = catId
        _selectedSubCategoryId.value = null // reset subcategory on category change
    }

    fun setSelectedSubCategory(subCatId: String?) {
        _selectedSubCategoryId.value = subCatId
    }

    fun setAvailabilityFilter(filter: AvailabilityFilter) {
        _availabilityFilter.value = filter
    }

    fun setSortOption(sort: ProductSortOption) {
        _sortOption.value = sort
    }

    fun refreshCatalog(isInitial: Boolean = false) {
        viewModelScope.launch {
            if (isInitial) _isLoading.value = true else _isRefreshing.value = true
            _errorMessage.value = null

            val result = productRepository.refreshCatalog()
            result.onFailure {
                _errorMessage.value = it.message ?: "Error al sincronizar el catálogo"
            }

            if (isInitial) _isLoading.value = false else _isRefreshing.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
