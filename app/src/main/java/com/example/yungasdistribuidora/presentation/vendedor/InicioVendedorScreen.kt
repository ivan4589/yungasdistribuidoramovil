package com.example.yungasdistribuidora.presentation.vendedor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InicioVendedorScreen(
    userName: String,
    userEmail: String,
    onNavigateToClients: () -> Unit,
    onLogout: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Inicio del vendedor", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Nombre: $userName", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Correo: $userEmail", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Rol: VENDEDOR", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToClients,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Portafolio de Clientes")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
