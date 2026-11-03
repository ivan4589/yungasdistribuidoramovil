package com.example.yungasdistribuidora

import com.example.yungasdistribuidora.data.mapper.SaleMapper
import com.example.yungasdistribuidora.data.remote.dto.SaleResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SaleMapperTest {
    @Test
    fun cancelledPdfSurvivesOfflineRoundTrip() {
        val original = sale().copy(cancelledPdfUrl = "https://example.test/cancelled.pdf")
        val restored = SaleMapper.toResponseDto(SaleMapper.toEntity(original), emptyList(), emptyList())
        assertEquals(original.cancelledPdfUrl, restored.cancelledPdfUrl)
        assertEquals(55.0, restored.total, 0.0)
    }

    @Test
    fun absentCancelledPdfRemainsNull() {
        val restored = SaleMapper.toResponseDto(SaleMapper.toEntity(sale()), emptyList(), emptyList())
        assertNull(restored.cancelledPdfUrl)
    }

    private fun sale() = SaleResponseDto(
        id = "s1", saleNumber = "SALE-1", clientId = "c1", clientName = "Client",
        status = "PENDING", paymentStatus = "UNPAID", saleType = "POR_COBRAR",
        dueDate = null, subtotal = 55.0, discount = 0.0, total = 55.0,
        paidAmount = 0.0, balance = 55.0, observations = null, pdfUrl = null,
        sellerId = null, sellerName = null, details = emptyList(), bonuses = emptyList(),
        createdAt = "2026-11-03", updatedAt = "2026-11-03"
    )
}
