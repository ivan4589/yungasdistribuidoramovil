package com.example.yungasdistribuidora.presentation.twofactor

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun TwoFactorRecoveryScreen(
    viewModel: TwoFactorRecoveryViewModel,
    onRecovered: (String) -> Unit,
    onBackToVerify: () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    var recoveryCode by remember { mutableStateOf("") }
    var rememberDevice by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is TwoFactorRecoveryUiState.Success -> {
                val route = when (state.role.uppercase()) {
                    "ADMIN" -> "admin_home"
                    "VENDEDOR" -> "vendor_home"
                    else -> "role_not_available"
                }
                onRecovered(route)
            }
            else -> {}
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Uso de Código de Recuperación", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Ingrese uno de sus códigos de recuperación guardados.")
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = recoveryCode,
                onValueChange = { recoveryCode = it },
                label = { Text("Código de recuperación") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(checked = rememberDevice, onCheckedChange = { rememberDevice = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recordar este dispositivo")
            }
            Spacer(modifier = Modifier.height(24.dp))

            val isLoading = uiState is TwoFactorRecoveryUiState.Loading

            Button(
                onClick = { viewModel.recover(recoveryCode, rememberDevice) },
                enabled = recoveryCode.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Confirmar")
                }
            }

            if (uiState is TwoFactorRecoveryUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = (uiState as TwoFactorRecoveryUiState.Error).message, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onBackToVerify) {
                Text("Volver a verificación normal")
            }
        }
    }
}
