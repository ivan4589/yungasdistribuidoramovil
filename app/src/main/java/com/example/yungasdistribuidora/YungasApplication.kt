package com.example.yungasdistribuidora

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.yungasdistribuidora.data.local.database.AppDatabase
import com.example.yungasdistribuidora.data.local.session.OfflineSessionManager
import com.example.yungasdistribuidora.data.local.session.SecurePersistentCookieJar
import com.example.yungasdistribuidora.data.remote.api.AuthApi
import com.example.yungasdistribuidora.data.remote.api.CategoryApi
import com.example.yungasdistribuidora.data.remote.api.ClientApi
import com.example.yungasdistribuidora.data.remote.api.LocationApi
import com.example.yungasdistribuidora.data.remote.api.ProductApi
import com.example.yungasdistribuidora.data.remote.api.SubCategoryApi
import com.example.yungasdistribuidora.data.remote.interceptor.AuthInterceptor
import com.example.yungasdistribuidora.data.repository.AuthRepository
import com.example.yungasdistribuidora.data.repository.AuthRepositoryImpl
import com.example.yungasdistribuidora.domain.repository.ClientRepository
import com.example.yungasdistribuidora.data.repository.ClientRepositoryImpl
import com.example.yungasdistribuidora.domain.repository.ProductRepository
import com.example.yungasdistribuidora.data.repository.ProductRepositoryImpl
import com.example.yungasdistribuidora.presentation.lock.AppLifecycleObserver
import com.example.yungasdistribuidora.presentation.lock.AppLockManager
import com.example.yungasdistribuidora.util.BiometricAuthManager
import com.example.yungasdistribuidora.util.BiometricAuthManagerImpl
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

    lateinit var productApi: ProductApi
        private set

    lateinit var categoryApi: CategoryApi
        private set

    lateinit var subCategoryApi: SubCategoryApi
        private set

    lateinit var productRepository: ProductRepository
        private set

    lateinit var appLockManager: AppLockManager
        private set

    lateinit var biometricAuthManager: BiometricAuthManager
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

        productApi = retrofit.create(ProductApi::class.java)
        categoryApi = retrofit.create(CategoryApi::class.java)
        subCategoryApi = retrofit.create(SubCategoryApi::class.java)
        productRepository = ProductRepositoryImpl(
            productApi,
            categoryApi,
            subCategoryApi,
            database
        )

        appLockManager = AppLockManager(authRepository, offlineSessionManager)
        biometricAuthManager = BiometricAuthManagerImpl(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver(appLockManager))
    }

    companion object {
        lateinit var instance: YungasApplication
            private set
    }
}
