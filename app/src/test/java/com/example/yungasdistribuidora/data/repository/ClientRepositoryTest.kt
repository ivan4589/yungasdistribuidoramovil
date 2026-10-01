package com.example.yungasdistribuidora.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.yungasdistribuidora.data.local.client.ClientDao
import com.example.yungasdistribuidora.data.local.location.LocationDao
import com.example.yungasdistribuidora.data.remote.api.ClientApi
import com.example.yungasdistribuidora.data.remote.api.LocationApi
import com.example.yungasdistribuidora.data.remote.dto.client.CreateClientRequest
import com.example.yungasdistribuidora.domain.repository.ClientRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ClientRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var clientApi: ClientApi
    private lateinit var locationApi: LocationApi
    private lateinit var clientRepository: ClientRepository

    private val clientDao: ClientDao = mockk(relaxed = true)
    private val locationDao: LocationDao = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val sharedPreferences: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        clientApi = retrofit.create(ClientApi::class.java)
        locationApi = retrofit.create(LocationApi::class.java)

        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.apply() } just Runs
        every { clientDao.getClients(any(), any(), any()) } returns flowOf(emptyList())

        clientRepository = ClientRepositoryImpl(
            clientApi,
            locationApi,
            clientDao,
            locationDao,
            context
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testRefreshClientsSuccess() = runBlocking {
        val jsonResponse = """
            [
              {
                "id": "cuid-1",
                "fullName": "Tienda Don Mario",
                "alias": "Don Mario",
                "type": "NORMAL",
                "locationId": "loc-1",
                "locationName": "Chulumani",
                "phone": "+59171234567",
                "whatsappConsent": true,
                "additionalInfo": "Referencia",
                "isActive": true,
                "deletedAt": null,
                "createdAt": "2026-09-29T00:00:00.000Z",
                "updatedAt": "2026-09-29T00:00:00.000Z"
              }
            ]
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val result = clientRepository.refreshClients(includeInactive = false, locationId = null, type = null)
        assertTrue(result.isSuccess)
        coVerify { clientDao.insertClients(any()) }
    }

    @Test
    fun testCreateClientDuplicatePhone409() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(409))

        val request = CreateClientRequest(
            fullName = "Tienda Duplicada",
            alias = null,
            type = "NORMAL",
            locationId = "loc-1",
            phone = "+59171234567",
            whatsappConsent = false,
            additionalInfo = null
        )

        val result = clientRepository.createClient(request)
        assertTrue(result.isFailure)
        assertEquals("Ya existe un cliente activo con el mismo teléfono", result.exceptionOrNull()?.message)
    }

    @Test
    fun testUpdateClientSuccessByVendorOrAdmin() = runBlocking {
        val jsonResponse = """
            {
              "id": "cuid-1",
              "fullName": "Tienda Actualizada",
              "alias": "Don Mario",
              "type": "NORMAL",
              "locationId": "loc-1",
              "locationName": "Chulumani",
              "phone": "+59171234567",
              "whatsappConsent": true,
              "additionalInfo": "Nueva Ref",
              "isActive": true,
              "deletedAt": null,
              "createdAt": "2026-09-29T00:00:00.000Z",
              "updatedAt": "2026-09-29T00:00:00.000Z"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val updateMap = mapOf("fullName" to "Tienda Actualizada")
        val result = clientRepository.updateClient("cuid-1", updateMap)
        assertTrue(result.isSuccess)
        assertEquals("Tienda Actualizada", result.getOrNull()?.fullName)
        coVerify { clientDao.insertClient(any()) }
    }
}
