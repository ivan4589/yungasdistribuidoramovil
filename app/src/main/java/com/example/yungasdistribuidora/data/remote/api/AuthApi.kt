package com.example.yungasdistribuidora.data.remote.api

import com.example.yungasdistribuidora.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/2fa/setup")
    suspend fun setupTwoFactor(@Body request: TwoFactorSetupRequest): Response<TwoFactorSetupResponse>

    @POST("auth/2fa/confirm")
    suspend fun confirmTwoFactor(@Body request: TwoFactorConfirmRequest): Response<AuthResponse>

    @POST("auth/2fa/verify")
    suspend fun verifyTwoFactor(@Body request: TwoFactorVerifyRequest): Response<AuthResponse>

    @POST("auth/2fa/recovery")
    suspend fun recoverTwoFactor(@Body request: TwoFactorRecoveryRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshSession(@Body body: Map<String, String> = emptyMap()): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @POST("auth/logout")
    suspend fun logout(@Body body: Map<String, String> = emptyMap()): Response<Unit>
}
