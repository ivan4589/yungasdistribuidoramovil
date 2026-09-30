package com.example.yungasdistribuidora.presentation.twofactor

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

@Composable
fun RecoveryCodesScreen(
    recoveryCodes: List<String>,
    userRole: String,
    onNavigateToHome: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var confirmedSaved by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Códigos de Recuperación", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Guarde estos códigos en un lugar seguro. No volverán a mostrarse.")
            Spacer(modifier = Modifier.height(16.dp))

            recoveryCodes.forEach { code ->
                Text(text = code, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                clipboardManager.setText(AnnotatedString(recoveryCodes.joinToString("\n")))
            }) {
                Text("Copiar códigos")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = confirmedSaved, onCheckedChange = { confirmedSaved = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirmo que he guardado mis códigos de recuperación")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val route = when (userRole.uppercase()) {
                        "ADMIN" -> "admin_home"
                        "VENDEDOR" -> "vendor_home"
                        else -> "role_not_available"
                    }
                    onNavigateToHome(route)
                },
                enabled = confirmedSaved,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Continuar")
            }
        }
    }
}
