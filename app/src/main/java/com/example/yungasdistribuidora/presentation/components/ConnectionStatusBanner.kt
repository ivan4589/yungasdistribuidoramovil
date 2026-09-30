package com.example.yungasdistribuidora.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionStatusBanner(
    isOffline: Boolean,
    lastValidationTime: String? = null
) {
    if (isOffline) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFD32F2F))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Modo sin conexión" + if (lastValidationTime != null) " • Última actualización: $lastValidationTime" else "",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
