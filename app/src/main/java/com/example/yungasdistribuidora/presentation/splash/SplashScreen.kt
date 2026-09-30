package com.example.yungasdistribuidora.presentation.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.yungasdistribuidora.data.local.session.OfflineSession
import com.example.yungasdistribuidora.presentation.offline.OfflineUnlockScreen
import com.example.yungasdistribuidora.util.BiometricAuthManagerImpl

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateOnline: (String) -> Unit,
    onNavigateOffline: (OfflineSession) -> Unit,
    onNavigateToLogin: (String) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var pendingSession by remember { mutableStateOf<OfflineSession?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SplashUiState.NavigateToOnline -> onNavigateOnline(state.route)
            is SplashUiState.NavigateToOffline -> onNavigateOffline(state.session)
            is SplashUiState.RequireBiometric -> {
                pendingSession = state.session
                val biometricManager = BiometricAuthManagerImpl(context)
                val activity = context as? FragmentActivity
                if (biometricManager.canAuthenticate() && activity != null) {
                    biometricManager.authenticate(
                        activity = activity,
                        title = "Desbloquear Sesión Offline",
                        subtitle = "Usa tu huella, rostro o PIN del dispositivo",
                        onSuccess = {
                            viewModel.onBiometricSuccess(state.session)
                        },
                        onError = { err ->
                            errorMessage = err
                        },
                        onFailed = {
                            errorMessage = "Autenticación biométrica fallida o cancelada."
                        }
                    )
                } else {
                    errorMessage = "Este dispositivo no tiene configurado un bloqueo seguro."
                }
            }
            is SplashUiState.NavigateToLoginNoConnection -> onNavigateToLogin("Necesitas conexión a internet para iniciar sesión por primera vez en este dispositivo.")
            is SplashUiState.NavigateToLoginExpired -> onNavigateToLogin("Tu sesión sin conexión ha expirado (más de 48 horas). Conéctate a internet para continuar.")
            is SplashUiState.Error -> errorMessage = state.message
            else -> {}
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (pendingSession != null && uiState is SplashUiState.RequireBiometric) {
            OfflineUnlockScreen(
                session = pendingSession!!,
                onUnlock = {
                    val biometricManager = BiometricAuthManagerImpl(context)
                    val activity = context as? FragmentActivity
                    if (biometricManager.canAuthenticate() && activity != null) {
                        biometricManager.authenticate(
                            activity = activity,
                            title = "Desbloquear Sesión Offline",
                            subtitle = "Usa tu huella, rostro o PIN del dispositivo",
                            onSuccess = { viewModel.onBiometricSuccess(pendingSession!!) },
                            onError = { errorMessage = it },
                            onFailed = { errorMessage = "Autenticación fallida." }
                        )
                    }
                },
                onRetryConnection = {
                    viewModel.checkSession()
                },
                onLogout = {
                    onNavigateToLogin("Sesión eliminada localmente.")
                }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Yungas Distribuidora", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.checkSession() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}
