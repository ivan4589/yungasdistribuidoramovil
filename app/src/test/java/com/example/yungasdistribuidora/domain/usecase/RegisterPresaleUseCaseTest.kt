package com.example.yungasdistribuidora.domain.usecase

import com.example.yungasdistribuidora.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class RegisterPresaleUseCaseTest {

    private val useCase = RegisterPresaleUseCase()

    private val sampleClient = Client(
        id = "c1",
        fullName = "Tienda Don Mario",
        alias = "Don Mario",
        type = ClientType.NORMAL,
        locationId = "l1",
        locationName = "Chulumani",
        phone = "123456",
        whatsappConsent = true,
        additionalInfo = null,
        isActive = true,
        deletedAt = null,
        createdAt = "2026-01-01",
        updatedAt = "2026-01-01"
    )

    private val sampleProduct = Product(
        id = "p1",
        code = "PROD-1",
        name = "Producto Test",
        description = null,
        providerId = "prov-1",
        categoryId = "cat-1",
        categoryName = "Cat",
        subCategoryId = null,
        subCategoryName = null,
        weight = null,
        priceNormal = 100.0,
        priceCamino = 90.0,
        priceEspecial = 80.0,
        priceMayorista = null,
        minQuantityWholesale = null,
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
    fun testPorCobrarBuildsCorrectRequest() {
        val draft = PresaleDraft(
            client = sampleClient,
            items = listOf(PresaleItem(sampleProduct, 2.0, 100.0)),
            modality = SaleModality.POR_COBRAR,
            observations = "Test obs"
        )

        val result = useCase.validateAndBuildRequest(draft, isAdmin = false, discountInput = 0.0, paymentMethod = null, paymentReference = null, dueDateStr = null)
        assertTrue(result.isSuccess)
        val req = result.getOrNull()!!
        assertEquals("CASH", req.saleType)
        assertEquals(0.0, req.initialPayment!!, 0.001)
        assertNull(req.dueDate)
        assertNull(req.paymentMethod)
    }

    @Test
    fun testContadoRequiresPaymentMethod() {
        val draft = PresaleDraft(
            client = sampleClient,
            items = listOf(PresaleItem(sampleProduct, 1.0, 100.0)),
            modality = SaleModality.CONTADO
        )

        val result = useCase.validateAndBuildRequest(draft, isAdmin = false, discountInput = 0.0, paymentMethod = null, paymentReference = null, dueDateStr = null)
        assertTrue(result.isFailure)
        assertEquals("La modalidad Contado exige un método de pago", result.exceptionOrNull()?.message)
    }

    @Test
    fun testCreditRequiresDueDate() {
        val draft = PresaleDraft(
            client = sampleClient,
            items = listOf(PresaleItem(sampleProduct, 1.0, 100.0)),
            modality = SaleModality.CREDITO
        )

        val result = useCase.validateAndBuildRequest(draft, isAdmin = false, discountInput = 0.0, paymentMethod = "CASH", paymentReference = null, dueDateStr = null)
        assertTrue(result.isFailure)
        assertEquals("La fecha de vencimiento es obligatoria para crédito", result.exceptionOrNull()?.message)
    }

    @Test
    fun testVendorCannotApplyDiscount() {
        val draft = PresaleDraft(
            client = sampleClient,
            items = listOf(PresaleItem(sampleProduct, 1.0, 100.0)),
            modality = SaleModality.POR_COBRAR
        )

        val result = useCase.validateAndBuildRequest(draft, isAdmin = false, discountInput = 10.0, paymentMethod = null, paymentReference = null, dueDateStr = null)
        assertTrue(result.isFailure)
        assertEquals("Los vendedores no pueden aplicar descuentos", result.exceptionOrNull()?.message)
    }
}
