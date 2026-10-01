package com.example.yungasdistribuidora.presentation.lock

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.example.yungasdistribuidora.util.BiometricAuthManager
import com.example.yungasdistribuidora.util.BiometricStartResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppUnlockScreen(
    activity: FragmentActivity,
    biometricAuthManager: BiometricAuthManager,
    appLockManager: AppLockManager,
    userName: String?,
    onLogout: () -> Unit
) {
    BackHandler(enabled = true) {
        // Intercept back button
    }

    val lockState by appLockManager.lockState.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastAttemptGeneration by remember { mutableStateOf(-1L) }
    val coroutineScope = rememberCoroutineScope()

    val triggerBiometric = remember(activity, biometricAuthManager, appLockManager) {
        {
            if (!appLockManager.isBiometricPromptActive && lockState is AppLockState.Locked) {
                appLockManager.setAuthenticating(true)
                errorMessage = null

                coroutineScope.launch {
                    // Wait until activity is RESUMED
                    while (activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
                        delay(50)
                        if (activity.isFinishing || activity.isDestroyed) {
                            appLockManager.setLocked()
                            appLockManager.isBiometricPromptActive = false
                            return@launch
                        }
                    }

                    val result = biometricAuthManager.authenticate(
                        activity = activity,
                        title = "Yungas Distribuidora",
                        subtitle = "Verifica tu identidad para continuar",
                        onSuccess = {
                            appLockManager.setUnlocked()
                        },
                        onError = { err ->
                            appLockManager.setError(err)
                            errorMessage = err
                        },
                        onFailed = {
                            appLockManager.setLocked()
                            errorMessage = "No se pudo verificar tu identidad. Intenta nuevamente."
                        }
                    )

                    if (result is BiometricStartResult.NotStarted) {
                        appLockManager.setLocked()
                        appLockManager.isBiometricPromptActive = false
                        errorMessage = result.message
                    }
                }
            }
        }
    }

    LaunchedEffect(appLockManager.lockGeneration, lockState) {
        if (lockState is AppLockState.Locked && appLockManager.lockGeneration != lastAttemptGeneration) {
            lastAttemptGeneration = appLockManager.lockGeneration
            triggerBiometric()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Yungas Distribuidora",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Aplicación bloqueada\nVerifica tu identidad para continuar.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!userName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Usuario: $userName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            val isAuthenticating = lockState is AppLockState.Authenticating

            Button(
                onClick = { triggerBiometric() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !appLockManager.isBiometricPromptActive
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verificando identidad...")
                } else {
                    Text("Desbloquear")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cerrar sesión")
            }

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
