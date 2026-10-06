package com.example.yungasdistribuidora.domain.usecase

import com.example.yungasdistribuidora.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateProductPriceUseCaseTest {

    private val useCase = CalculateProductPriceUseCase()

    private val sampleProduct = Product(
        id = "p1",
        code = "PROD-1",
        name = "Producto Test",
        description = null,
        providerId = "prov-1",
        categoryId = "cat-1",
        categoryName = "Categoria",
        subCategoryId = null,
        subCategoryName = null,
        weight = null,
        priceNormal = 100.0,
        priceCamino = 90.0,
        priceEspecial = 80.0,
        priceMayorista = 70.0,
        minQuantityWholesale = 10.0,
        stock = 50.0,
        centralStock = 50.0,
        centralReservedStock = 0.0,
        centralAvailableStock = 50.0,
        minStock = 5.0,
        unit = "UND",
        reserveQuantity = 0.0,
        additionalInfo = null,
        imageUrl = null,
        isActive = true,
        deletedAt = null,
        createdAt = "2026-01-01",
        updatedAt = "2026-01-01"
    )

    @Test
    fun testNormalPrice() {
        val price = useCase(sampleProduct, "NORMAL", quantity = 1.0)
        assertEquals(100.0, price, 0.001)
    }

    @Test
    fun testCaminoPrice() {
        val price = useCase(sampleProduct, "CAMINO", quantity = 1.0)
        assertEquals(90.0, price, 0.001)
    }

    @Test
    fun testEspecialPrice() {
        val price = useCase(sampleProduct, "ESPECIAL", quantity = 1.0)
        assertEquals(80.0, price, 0.001)
    }

    @Test
    fun testWholesalePriceMet() {
        val price = useCase(sampleProduct, "NORMAL", quantity = 10.0)
        assertEquals(70.0, price, 0.001)
    }

    @Test
    fun testWholesalePriceNotMet() {
        val price = useCase(sampleProduct, "NORMAL", quantity = 5.0)
        assertEquals(100.0, price, 0.001)
    }
}
