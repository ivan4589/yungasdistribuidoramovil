package com.example.yungasdistribuidora

import android.app.Application
import com.example.yungasdistribuidora.data.local.database.AppDatabase
import com.example.yungasdistribuidora.data.local.session.OfflineSessionManager
import com.example.yungasdistribuidora.data.local.session.SecurePersistentCookieJar
import com.example.yungasdistribuidora.data.remote.api.AuthApi
import com.example.yungasdistribuidora.data.remote.api.ClientApi
import com.example.yungasdistribuidora.data.remote.api.LocationApi
import com.example.yungasdistribuidora.data.remote.interceptor.AuthInterceptor
import com.example.yungasdistribuidora.data.repository.AuthRepository
import com.example.yungasdistribuidora.data.repository.AuthRepositoryImpl
import com.example.yungasdistribuidora.domain.repository.ClientRepository
import com.example.yungasdistribuidora.data.repository.ClientRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class YungasApplication : Application() {

    lateinit var cookieJar: SecurePersistentCookieJar
        private set

    lateinit var offlineSessionManager: OfflineSessionManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var authApi: AuthApi
        private set

    lateinit var database: AppDatabase
        private set

    lateinit var clientApi: ClientApi
        private set

    lateinit var locationApi: LocationApi
        private set

    lateinit var clientRepository: ClientRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        cookieJar = SecurePersistentCookieJar(this)
        offlineSessionManager = OfflineSessionManager(this)

        val authInterceptor = AuthInterceptor { authRepository }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.yungasdistribuidora.cc/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authApi = retrofit.create(AuthApi::class.java)
        authRepository = AuthRepositoryImpl(authApi, cookieJar, offlineSessionManager)

        database = AppDatabase.getInstance(this)
        clientApi = retrofit.create(ClientApi::class.java)
        locationApi = retrofit.create(LocationApi::class.java)
        clientRepository = ClientRepositoryImpl(
            clientApi,
            locationApi,
            database.clientDao(),
            database.locationDao(),
            this
        )
    }

    companion object {
        lateinit var instance: YungasApplication
            private set
    }
}
