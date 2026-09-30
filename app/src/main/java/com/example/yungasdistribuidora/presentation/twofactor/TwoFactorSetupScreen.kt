package com.example.yungasdistribuidora.presentation.twofactor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun TwoFactorSetupScreen(
    viewModel: TwoFactorSetupViewModel,
    onConfirmed: (List<String>, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

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
            is TwoFactorSetupUiState.Confirmed -> onConfirmed(state.recoveryCodes, state.role)
            else -> {}
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Configuración de Autenticación 2FA", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is TwoFactorSetupUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is TwoFactorSetupUiState.SetupReady -> {
                    Text(text = "Escanee el enlace o ingrese el secreto en su aplicación autenticadora (Google Authenticator, Authy, etc.):")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = state.secret, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        clipboardManager.setText(AnnotatedString(state.secret))
                    }) {
                        Text("Copiar secreto")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.otpauthUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // ignore if no app handles intent
                        }
                    }) {
                        Text("Abrir en autenticador")
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) code = it },
                        label = { Text("Código de 6 dígitos") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = rememberDevice,
                            onCheckedChange = { rememberDevice = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recordar este dispositivo")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.confirmSetup(code, rememberDevice) },
                        enabled = code.length == 6,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Confirmar y Continuar")
                    }
                }
                is TwoFactorSetupUiState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchSetupData() }) {
                        Text("Reintentar")
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onBackToLogin) {
                Text("Regresar al login")
            }
        }
    }
}
