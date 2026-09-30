package com.example.yungasdistribuidora.data.repository

import com.example.yungasdistribuidora.data.local.session.OfflineSessionManager
import com.example.yungasdistribuidora.data.local.session.SecurePersistentCookieJar
import com.example.yungasdistribuidora.data.remote.api.AuthApi
import com.example.yungasdistribuidora.data.remote.dto.*
import com.example.yungasdistribuidora.domain.model.User
import com.example.yungasdistribuidora.domain.model.UserStatus
import com.example.yungasdistribuidora.util.ErrorMapper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface AuthRepository {
    var accessToken: String?
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun setupTwoFactor(request: TwoFactorSetupRequest): Result<TwoFactorSetupResponse>
    suspend fun confirmTwoFactor(request: TwoFactorConfirmRequest): Result<AuthResponse>
    suspend fun verifyTwoFactor(request: TwoFactorVerifyRequest): Result<AuthResponse>
    suspend fun recoverTwoFactor(request: TwoFactorRecoveryRequest): Result<AuthResponse>
    suspend fun refreshSession(): Result<AuthResponse>
    suspend fun getCurrentUser(): Result<User>
    suspend fun logout(): Result<Unit>
    fun clearLocalSession()
}

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val cookieJar: SecurePersistentCookieJar,
    private val offlineSessionManager: OfflineSessionManager
) : AuthRepository {

    override var accessToken: String? = null
    private val refreshMutex = Mutex()

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = authApi.login(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun setupTwoFactor(request: TwoFactorSetupRequest): Result<TwoFactorSetupResponse> {
        return try {
            val response = authApi.setupTwoFactor(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun confirmTwoFactor(request: TwoFactorConfirmRequest): Result<AuthResponse> {
        return try {
            val response = authApi.confirmTwoFactor(request)
            if (response.isSuccessful && response.body() != null) {
                val authRes = response.body()!!
                accessToken = authRes.access_token
                val domainUser = authRes.user.toDomain()
                if (domainUser.status == UserStatus.ACTIVE && request.remember) {
                    offlineSessionManager.saveSession(domainUser, true)
                }
                Result.success(authRes)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun verifyTwoFactor(request: TwoFactorVerifyRequest): Result<AuthResponse> {
        return try {
            val response = authApi.verifyTwoFactor(request)
            if (response.isSuccessful && response.body() != null) {
                val authRes = response.body()!!
                accessToken = authRes.access_token
                val domainUser = authRes.user.toDomain()
                if (domainUser.status == UserStatus.ACTIVE && request.remember) {
                    offlineSessionManager.saveSession(domainUser, true)
                }
                Result.success(authRes)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun recoverTwoFactor(request: TwoFactorRecoveryRequest): Result<AuthResponse> {
        return try {
            val response = authApi.recoverTwoFactor(request)
            if (response.isSuccessful && response.body() != null) {
                val authRes = response.body()!!
                accessToken = authRes.access_token
                val domainUser = authRes.user.toDomain()
                if (domainUser.status == UserStatus.ACTIVE && request.remember) {
                    offlineSessionManager.saveSession(domainUser, true)
                }
                Result.success(authRes)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun refreshSession(): Result<AuthResponse> = refreshMutex.withLock {
        return try {
            val response = authApi.refreshSession()
            if (response.isSuccessful && response.body() != null) {
                val authRes = response.body()!!
                accessToken = authRes.access_token
                val domainUser = authRes.user.toDomain()
                val existingOffline = offlineSessionManager.getSession()
                if (domainUser.status == UserStatus.ACTIVE && existingOffline?.offlineEnabled == true) {
                    offlineSessionManager.saveSession(domainUser, true)
                }
                Result.success(authRes)
            } else {
                if (response.code() == 401) {
                    clearLocalSession()
                }
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = authApi.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                val domainUser = response.body()!!.toDomain()
                val existingOffline = offlineSessionManager.getSession()
                if (domainUser.status == UserStatus.ACTIVE && existingOffline?.offlineEnabled == true) {
                    offlineSessionManager.saveSession(domainUser, true)
                }
                Result.success(domainUser)
            } else {
                if (response.code() == 401) {
                    clearLocalSession()
                }
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            authApi.logout()
            clearLocalSession()
            Result.success(Unit)
        } catch (e: Exception) {
            clearLocalSession()
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override fun clearLocalSession() {
        accessToken = null
        cookieJar.clear()
        offlineSessionManager.clear()
    }
}
