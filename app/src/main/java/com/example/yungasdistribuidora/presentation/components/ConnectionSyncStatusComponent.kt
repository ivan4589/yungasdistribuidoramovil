package com.example.yungasdistribuidora.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class SyncState {
    ONLINE_SYNCED,
    PENDING_SYNC,
    OFFLINE,
    SYNCING,
    SYNC_ERROR
}

@Composable
fun ConnectionSyncStatusComponent(
    syncState: SyncState,
    lastValidationTime: String? = null,
    errorMessage: String? = null,
    onRetrySync: (() -> Unit)? = null
) {
    val backgroundColor: Color
    val textColor: Color
    val text: String
    val icon: ImageVector

    when (syncState) {
        SyncState.ONLINE_SYNCED -> {
            backgroundColor = Color(0xFF087C69)
            textColor = Color.White
            text = "En línea • Sincronizado"
            icon = Icons.Default.CheckCircle
        }
        SyncState.PENDING_SYNC -> {
            backgroundColor = Color(0xFFF5B942)
            textColor = Color(0xFF071E30)
            text = "Cambios pendientes de sincronización"
            icon = Icons.Default.Info
        }
        SyncState.OFFLINE -> {
            backgroundColor = Color(0xFFEF5350)
            textColor = Color.White
            text = "Modo sin conexión" + if (lastValidationTime != null) " • Última vez: $lastValidationTime" else ""
            icon = Icons.Default.Warning
        }
        SyncState.SYNCING -> {
            backgroundColor = Color(0xFF103B56)
            textColor = Color(0xFF35D5B1)
            text = "Sincronizando datos..."
            icon = Icons.Default.Refresh
        }
        SyncState.SYNC_ERROR -> {
            backgroundColor = Color(0xFFEF5350)
            textColor = Color.White
            text = errorMessage ?: "Error de sincronización"
            icon = Icons.Default.Warning
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (syncState == SyncState.SYNCING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = textColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = text,
                    color = textColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (onRetrySync != null && (syncState == SyncState.SYNC_ERROR || syncState == SyncState.OFFLINE || syncState == SyncState.PENDING_SYNC)) {
                TextButton(
                    onClick = onRetrySync,
                    colors = ButtonDefaults.textButtonColors(contentColor = textColor)
                ) {
                    Text("Sincronizar", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
