package com.example.yungasdistribuidora.domain.usecase

import com.example.yungasdistribuidora.data.local.bonus.BonusRuleEntity
import com.example.yungasdistribuidora.domain.model.PresaleItem
import com.example.yungasdistribuidora.domain.model.Product
import org.junit.Assert.*
import org.junit.Test

class CalculateBonusUseCaseTest {

    private val useCase = CalculateBonusUseCase()

    private val sampleProduct = Product(
        id = "p1",
        code = "PROD-1",
        name = "Soda",
        description = null,
        providerId = "prov-1",
        categoryId = "cat-1",
        categoryName = "Bebidas",
        subCategoryId = "sub-1",
        subCategoryName = "Gaseosas",
        weight = null,
        priceNormal = 10.0,
        priceCamino = 9.0,
        priceEspecial = 8.0,
        priceMayorista = null,
        minQuantityWholesale = null,
        stock = 100.0,
        centralStock = 100.0,
        centralReservedStock = 0.0,
        centralAvailableStock = 100.0,
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
    fun testCalculateBonusEarnedCorrectly() {
        val rules = listOf(
            BonusRuleEntity(
                id = "r1",
                subCategoryId = "sub-1",
                minQuantity = 10,
                bonusQuantity = 2,
                startDate = null,
                endDate = null,
                isActive = true
            )
        )

        val items = listOf(
            PresaleItem(sampleProduct, quantity = 25, unitPrice = 10.0)
        )

        val earned = useCase.calculate(rules, items)
        assertEquals(4, earned["sub-1"])
    }

    @Test
    fun testCalculateBonusBelowMinimum() {
        val rules = listOf(
            BonusRuleEntity(
                id = "r1",
                subCategoryId = "sub-1",
                minQuantity = 10,
                bonusQuantity = 2,
                startDate = null,
                endDate = null,
                isActive = true
            )
        )

        val items = listOf(
            PresaleItem(sampleProduct, quantity = 5, unitPrice = 10.0)
        )

        val earned = useCase.calculate(rules, items)
        assertTrue(earned.isEmpty())
    }
}
