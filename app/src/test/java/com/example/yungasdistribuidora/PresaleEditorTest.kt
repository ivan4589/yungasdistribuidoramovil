package com.example.yungasdistribuidora

import com.example.yungasdistribuidora.data.remote.dto.*
import com.example.yungasdistribuidora.domain.model.*
import com.example.yungasdistribuidora.domain.repository.*
import com.example.yungasdistribuidora.presentation.presale.NewPresaleViewModel
import com.google.gson.Gson
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PresaleEditorTest {
    private val clients = mockk<ClientRepository>()
    private val products = mockk<ProductRepository>()
    private val sales = mockk<SalesRepository>()
    private val bonuses = mockk<BonusRuleRepository>()
    private val product = mockk<Product>(relaxed = true)
    private val client = mockk<Client>(relaxed = true)
    private val pending get() = Gson().fromJson("""{
        "id":"s1","saleNumber":"TEST-1","clientId":"c1","status":"PENDING",
        "saleType":"CASH","paymentStatus":"PENDING","createdAt":"2026-11-03","updatedAt":"2026-11-03",
        "discount":0,"observations":"Original",
        "details":[{"id":"d1","productId":"p1","quantity":5,"unitPrice":11,"subtotal":55}],
        "bonuses":[]
    }""", SaleResponseDto::class.java)

    @Before fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { client.id } returns "c1"
        every { product.id } returns "p1"
        every { product.availableStock } returns 0.0
        every { clients.getClients(any(), any(), any()) } returns flowOf(listOf(client))
        every { products.getProducts(any(), any()) } returns flowOf(listOf(product))
        coEvery { clients.getLocations() } returns Result.success(emptyList())
        coEvery { clients.getClientById("c1") } returns Result.success(client)
        coEvery { products.refreshCatalog() } returns Result.success(Unit)
        coEvery { products.getProductById("p1") } returns Result.success(product)
        coEvery { bonuses.getBonusRules() } returns Result.success(emptyList())
        coEvery { sales.getSaleById("s1") } returns Result.success(pending)
        coEvery { sales.updateSale(any(), any(), any()) } returns Result.success(pending)
    }
    @After fun cleanup() { Dispatchers.resetMain() }
    private fun editor(admin: Boolean = true) = NewPresaleViewModel(clients, products, sales, bonuses,
        admin, editingSaleId = "s1", salesRepository = sales)

    @Test fun loadsPendingAndUpdatesInsteadOfCreating() {
        val vm = editor()
        assertEquals(5, vm.draft.value.items.single().quantity)
        assertEquals(11.0, vm.draft.value.items.single().unitPrice, 0.0)
        vm.setObservations("Modificada")
        vm.registerSale()
        coVerify(exactly = 1) { sales.updateSale(any(), "s1", match { it.observations == "Modificada" && it.details.single().unitPrice == 11.0 }) }
        coVerify(exactly = 0) { sales.registerSale(any(), any()) }
    }

    @Test fun confirmedSaleCannotBeEdited() {
        coEvery { sales.getSaleById("s1") } returns Result.success(pending.copy(status = "CONFIRMED"))
        val vm = editor()
        assertTrue(vm.errorMessage.value!!.contains("pendientes"))
        vm.registerSale()
        coVerify(exactly = 0) { sales.updateSale(any(), any(), any()) }
    }

    @Test fun vendorCannotOverridePriceInEditor() {
        val vm = editor(false)
        vm.updateItemDetails("p1", 5, 1.0)
        assertEquals(11.0, vm.draft.value.items.single().unitPrice, 0.0)
        vm.registerSale()
        coVerify { sales.updateSale(any(), "s1", match { !it.details.single().manualPrice }) }
    }

    @Test fun adminCanChangePrice() {
        val vm = editor()
        vm.updateItemDetails("p1", 5, 10.0)
        vm.registerSale()
        coVerify { sales.updateSale(any(), "s1", match { it.details.single().manualPrice && it.details.single().unitPrice == 10.0 }) }
    }
}
