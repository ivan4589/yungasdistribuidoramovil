package com.example.yungasdistribuidora.domain.usecase

import com.example.yungasdistribuidora.domain.model.Product

class CalculateProductPriceUseCase {
    operator fun invoke(product: Product, clientType: String?, quantity: Double = 1.0): Double {
        val type = clientType?.uppercase() ?: "NORMAL"
        return when (type) {
            "CAMINO" -> product.priceCamino
            "ESPECIAL" -> product.priceEspecial
            else -> {
                if (product.priceMayorista != null && product.minQuantityWholesale != null && quantity >= product.minQuantityWholesale) {
                    product.priceMayorista
                } else {
                    product.priceNormal
                }
            }
        }
    }
}
