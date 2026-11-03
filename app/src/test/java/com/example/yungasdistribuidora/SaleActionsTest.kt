package com.example.yungasdistribuidora

import com.example.yungasdistribuidora.data.local.sale.SaleDao
import com.example.yungasdistribuidora.data.remote.api.SalesApi
import com.example.yungasdistribuidora.data.remote.dto.*
import com.example.yungasdistribuidora.data.repository.SalesRepositoryImpl
import com.google.gson.JsonParser
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SaleActionsTest {
    private val server = MockWebServer()
    private lateinit var api: SalesApi
    private lateinit var repository: SalesRepositoryImpl
    @Before fun setup() {
        server.start()
        api = Retrofit.Builder().baseUrl(server.url("/api/"))
            .addConverterFactory(GsonConverterFactory.create()).build().create(SalesApi::class.java)
        repository = SalesRepositoryImpl(api, mockk<SaleDao>(relaxed = true))
    }
    @After fun close() { server.shutdown() }
    private fun response(json: String, code: Int = 200) = server.enqueue(MockResponse().setResponseCode(code).setBody(json))

    @Test fun pdfUsesProtectedDocumentEndpoint() = runBlocking {
        response("%PDF-1.7\nmock")
        val body = repository.downloadPdf("sale-1").getOrThrow()
        body.use { assertTrue(it.string().startsWith("%PDF-")) }
        assertEquals("/api/documents/sales/sale-1", server.takeRequest().path)
    }

    @Test fun pdfDoesNotAcceptUnauthorizedResponse() = runBlocking {
        response("{}", 403)
        assertTrue(repository.downloadPdf("sale-1").isFailure)
    }

    @Test fun updateSendsOnlyFieldsAllowedByBackendAndKeepsManualPrice() = runBlocking {
        response("{}", 400)
        val request = CreateSaleRequestDto("c1", listOf(SaleDetailRequestDto("p1", 5, 11.0, true)),
            emptyList(), 0.0, "", "CASH", null, 55.0, "CASH", "payment")
        api.updateSale("edit-key", "s1", request.forUpdate())
        val sent = server.takeRequest()
        assertEquals("PATCH", sent.method)
        assertEquals("/api/sales/s1", sent.path)
        assertEquals("edit-key", sent.getHeader("Idempotency-Key"))
        val json = JsonParser.parseString(sent.body.readUtf8()).asJsonObject
        listOf("initialPayment", "paymentMethod", "paymentReference").forEach { assertFalse(json.has(it)) }
        assertEquals(11.0, json.getAsJsonArray("details")[0].asJsonObject["unitPrice"].asDouble, 0.0)
        assertTrue(json.getAsJsonArray("details")[0].asJsonObject["manualPrice"].asBoolean)
        assertEquals(0, json.getAsJsonArray("bonuses").size())
    }

    @Test fun eligibleContactsIncludePrincipalAndAllAuthorizedActiveContacts() {
        val contacts = listOf(
            WhatsAppContactDto("a", "A", "71111111", true, true),
            WhatsAppContactDto("b", "B", "72222222", true, true),
            WhatsAppContactDto("inactive", "C", "73333333", false, true),
            WhatsAppContactDto("no-consent", "D", "74444444", true, false),
            WhatsAppContactDto("no-phone", "E", "", true, true))
        assertEquals(listOf("principal", "a", "b"), eligibleWhatsAppRecipients(WhatsAppClientDto("70000000", true), contacts))
        assertEquals(listOf("a", "b"), eligibleWhatsAppRecipients(WhatsAppClientDto(null, false), contacts))
    }

    @Test fun whatsappUsesBatchAndReportsPartialFailure() = runBlocking {
        response("""{"id":"s1","clientId":"c1","status":"CONFIRMED"}""")
        response("""{"phone":"70000000","whatsappConsent":true}""")
        response("""[{"id":"a","name":"A","phone":"71111111","isActive":true,"whatsappConsent":true}]""")
        response("""{"results":[{"recipient":"principal","status":"SENT"},{"recipient":"a","status":"FAILED","error":"Sin saldo"}]}""")
        val result = repository.sendWhatsapp("s1", false).getOrThrow()
        assertEquals(1, result.acceptedCount)
        assertTrue(result.message().contains("Sin saldo"))
        repeat(3) { server.takeRequest() }
        val sent = server.takeRequest()
        assertEquals("/api/sales/s1/whatsapp/recipients", sent.path)
        val body = JsonParser.parseString(sent.body.readUtf8()).asJsonObject
        assertEquals(2, body.getAsJsonArray("recipients").size())
        assertFalse(body["resend"].asBoolean)
    }

    @Test fun pendingSaleCannotSendWhatsApp() = runBlocking {
        response("""{"id":"s1","clientId":"c1","status":"PENDING"}""")
        assertTrue(repository.sendWhatsapp("s1", false).isFailure)
        assertEquals(1, server.requestCount)
    }

    @Test fun contactsFailureDoesNotSilentlySendOnlyToPrincipal() = runBlocking {
        response("""{"id":"s1","clientId":"c1","status":"CONFIRMED"}""")
        response("""{"phone":"70000000","whatsappConsent":true}""")
        response("{}", 403)
        assertTrue(repository.sendWhatsapp("s1", false).isFailure)
        assertEquals(3, server.requestCount)
    }

    @Test fun missingBatchResultsAreNotReportedAsSent() = runBlocking {
        response("""{"id":"s1","clientId":"c1","status":"CONFIRMED"}""")
        response("""{"phone":"70000000","whatsappConsent":true}""")
        response("[]")
        response("{}")
        val result = repository.sendWhatsapp("s1", false).getOrThrow()
        assertEquals(0, result.acceptedCount)
        assertEquals("UNKNOWN", result.results.single().status)
    }
}
