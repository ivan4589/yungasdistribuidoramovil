package com.example.yungasdistribuidora.data.repository

import com.example.yungasdistribuidora.data.local.session.OfflineSessionManager
import com.example.yungasdistribuidora.data.local.session.SecurePersistentCookieJar
import com.example.yungasdistribuidora.data.remote.api.AuthApi
import com.example.yungasdistribuidora.data.remote.dto.LoginRequest
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

class AuthRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var authApi: AuthApi
    private lateinit var authRepository: AuthRepository
    private val cookieJar: SecurePersistentCookieJar = mockk(relaxed = true)
    private val offlineSessionManager: OfflineSessionManager = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authApi = retrofit.create(AuthApi::class.java)
        authRepository = AuthRepositoryImpl(authApi, cookieJar, offlineSessionManager)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testLoginRequiresTwoFactor() = runBlocking {
        val responseBody = """{"requiresTwoFactor":true,"challengeToken":"test-token"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val result = authRepository.login(LoginRequest("test@ejemplo.com", "password"))
        assertTrue(result.isSuccess)
        val loginResponse = result.getOrNull()
        assertTrue(loginResponse?.requiresTwoFactor == true)
        assertEquals("test-token", loginResponse?.challengeToken)
    }

    @Test
    fun testLoginUnauthorized() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val result = authRepository.login(LoginRequest("test@ejemplo.com", "wrong"))
        assertTrue(result.isFailure)
    }
}
