package com.example.yungasdistribuidora

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.yungasdistribuidora.presentation.lock.AppLockState
import com.example.yungasdistribuidora.presentation.lock.AppUnlockScreen
import com.example.yungasdistribuidora.presentation.navigation.AppNavGraph
import com.example.yungasdistribuidora.ui.theme.YungasDistribuidoraTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YungasDistribuidoraTheme {
                val app = YungasApplication.instance
                val appLockManager = app.appLockManager
                val biometricAuthManager = app.biometricAuthManager
                val lockState by appLockManager.lockState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        AppNavGraph()

                        if (lockState is AppLockState.Locked || lockState is AppLockState.Authenticating || lockState is AppLockState.Error) {
                            val userName = remember { app.offlineSessionManager.getSession()?.name }
                            AppUnlockScreen(
                                activity = this@MainActivity,
                                biometricAuthManager = biometricAuthManager,
                                appLockManager = appLockManager,
                                userName = userName,
                                onLogout = {
                                    app.authRepository.clearLocalSession()
                                    appLockManager.clear()
                                    recreate()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
