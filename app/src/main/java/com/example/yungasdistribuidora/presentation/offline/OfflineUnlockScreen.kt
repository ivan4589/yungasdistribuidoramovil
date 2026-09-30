package com.example.yungasdistribuidora.presentation.offline

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.yungasdistribuidora.data.local.session.OfflineSession
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OfflineUnlockScreen(
    session: OfflineSession,
    onUnlock: () -> Unit,
    onRetryConnection: () -> Unit,
    onLogout: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val lastSyncStr = dateFormat.format(Date(session.lastOnlineValidationAt))

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Modo Sin Conexión", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Usuario: ${session.name}")
            Text(text = "Correo: ${session.email}")
            Text(text = "Rol: ${session.role}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Última sincronización online: $lastSyncStr", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onUnlock,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Continuar sin conexión (Biometría)")
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onRetryConnection,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Reintentar conexión")
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onLogout) {
                Text("Cerrar sesión y eliminar datos locales", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
