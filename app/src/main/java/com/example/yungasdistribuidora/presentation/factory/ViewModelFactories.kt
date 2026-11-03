package com.example.yungasdistribuidora.presentation.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.yungasdistribuidora.domain.repository.BonusRuleRepository
import com.example.yungasdistribuidora.domain.repository.ClientRepository
import com.example.yungasdistribuidora.domain.repository.PresaleRepository
import com.example.yungasdistribuidora.domain.repository.ProductRepository
import com.example.yungasdistribuidora.domain.repository.SalesRepository
import com.example.yungasdistribuidora.domain.usecase.CalculateBonusUseCase
import com.example.yungasdistribuidora.domain.usecase.CalculateProductPriceUseCase
import com.example.yungasdistribuidora.domain.usecase.RegisterPresaleUseCase
import com.example.yungasdistribuidora.presentation.clients.detail.ClientDetailViewModel
import com.example.yungasdistribuidora.presentation.clients.form.ClientFormViewModel
import com.example.yungasdistribuidora.presentation.clients.list.ClientsViewModel
import com.example.yungasdistribuidora.presentation.presale.NewPresaleViewModel
import com.example.yungasdistribuidora.presentation.productdetail.ProductDetailViewModel
import com.example.yungasdistribuidora.presentation.products.ProductsViewModel
import com.example.yungasdistribuidora.presentation.sales.MyPresalesViewModel
import com.example.yungasdistribuidora.presentation.sales.PresaleDetailViewModel

class ClientsViewModelFactory(
    private val clientRepository: ClientRepository,
    private val isAdmin: Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientsViewModel(clientRepository, isAdmin) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ClientDetailViewModelFactory(
    private val clientRepository: ClientRepository,
    private val clientId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientDetailViewModel(clientRepository, clientId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ClientFormViewModelFactory(
    private val clientRepository: ClientRepository,
    private val clientId: String?,
    private val canEdit: Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientFormViewModel(clientRepository, clientId, canEdit) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProductsViewModelFactory(
    private val productRepository: ProductRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductsViewModel(productRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProductDetailViewModelFactory(
    private val productRepository: ProductRepository,
    private val productId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductDetailViewModel(productRepository, productId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class NewPresaleViewModelFactory(
    private val clientRepository: ClientRepository,
    private val productRepository: ProductRepository,
    private val presaleRepository: PresaleRepository,
    private val bonusRuleRepository: BonusRuleRepository,
    private val isAdmin: Boolean,
    private val editingSaleId: String? = null,
    private val salesRepository: SalesRepository? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewPresaleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewPresaleViewModel(
                clientRepository,
                productRepository,
                presaleRepository,
                bonusRuleRepository,
                isAdmin,
                CalculateProductPriceUseCase(),
                CalculateBonusUseCase(),
                RegisterPresaleUseCase(),
                editingSaleId,
                salesRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MyPresalesViewModelFactory(
    private val salesRepository: SalesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPresalesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyPresalesViewModel(salesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PresaleDetailViewModelFactory(
    private val salesRepository: SalesRepository,
    private val saleId: String,
    private val isAdmin: Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PresaleDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PresaleDetailViewModel(salesRepository, saleId, isAdmin) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
