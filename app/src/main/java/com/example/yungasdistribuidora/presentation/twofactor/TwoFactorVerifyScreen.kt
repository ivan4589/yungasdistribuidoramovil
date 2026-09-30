package com.example.yungasdistribuidora.presentation.twofactor

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun TwoFactorVerifyScreen(
    viewModel: TwoFactorVerifyViewModel,
    onVerified: (String) -> Unit,
    onNavigateToRecovery: () -> Unit,
    onBackToLogin: () -> Unit
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
    var code by remember { mutableStateOf("") }
    var rememberDevice by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is TwoFactorVerifyUiState.Success -> {
                val route = when (state.role.uppercase()) {
                    "ADMIN" -> "admin_home"
                    "VENDEDOR" -> "vendor_home"
                    else -> "role_not_available"
                }
                onVerified(route)
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
            Text(text = "Verificación de Dos Factores (2FA)", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Ingrese el código de 6 dígitos de su aplicación autenticadora.")
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) code = it },
                label = { Text("Código de 6 dígitos") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

            val isLoading = uiState is TwoFactorVerifyUiState.Loading

            Button(
                onClick = { viewModel.verify(code, rememberDevice) },
                enabled = code.length == 6 && !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Verificar")
                }
            }

            if (uiState is TwoFactorVerifyUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = (uiState as TwoFactorVerifyUiState.Error).message, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onNavigateToRecovery) {
                Text("Usar código de recuperación")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onBackToLogin) {
                Text("Regresar al login")
            }
        }
    }
}
