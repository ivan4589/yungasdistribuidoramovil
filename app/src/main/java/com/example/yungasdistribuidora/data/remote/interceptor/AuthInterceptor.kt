package com.example.yungasdistribuidora.data.remote.interceptor

import com.example.yungasdistribuidora.data.repository.AuthRepository
import okhttp3.Interceptor
import okhttp3.Response
import kotlinx.coroutines.runBlocking

class AuthInterceptor(
    private val authRepositoryProvider: () -> AuthRepository
) : Interceptor {

    private val excludedPaths = listOf(
        "auth/login",
        "auth/2fa/setup",
        "auth/2fa/confirm",
        "auth/2fa/verify",
        "auth/2fa/recovery",
        "auth/refresh",
        "auth/logout"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        val isExcluded = excludedPaths.any { path.contains(it) }
        val authRepo = authRepositoryProvider()

        val modifiedRequest = if (!isExcluded && authRepo.accessToken != null) {
            request.newBuilder()
                .header("Authorization", "Bearer ${authRepo.accessToken}")
                .build()
        } else {
            request
        }

        val response = chain.proceed(modifiedRequest)

        if (response.code == 401 && !isExcluded) {
            val refreshed = runBlocking {
                authRepo.refreshSession().isSuccess
            }
            if (refreshed && authRepo.accessToken != null) {
                response.close()
                val retryRequest = request.newBuilder()
                    .header("Authorization", "Bearer ${authRepo.accessToken}")
                    .build()
                return chain.proceed(retryRequest)
            }
        }

        return response
    }
}
