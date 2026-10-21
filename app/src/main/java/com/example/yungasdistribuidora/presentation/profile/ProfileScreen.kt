package com.example.yungasdistribuidora.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.yungasdistribuidora.presentation.components.YungasBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    userRole: String,
    isOffline: Boolean,
    lastValidationTime: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val isAdmin = userRole.uppercase() == "ADMIN"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Usuario") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF103B56),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            YungasBottomBar(
                currentRoute = "profile",
                isAdmin = isAdmin,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF071E30))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Avatar initial
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF35D5B1)),
                contentAlignment = Alignment.Center
            ) {
                val initial = if (userName.isNotBlank()) userName.take(1).uppercase() else "U"
                Text(
                    text = initial,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF071E30)
                )
            }

            Text(
                text = userName.ifBlank { "Usuario Yungas" },
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Text(
                text = userEmail.ifBlank { "usuario@yungasdistribuidora.cc" },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFA8B8C5)
            )

            Surface(
                color = Color(0xFF087C69),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = userRole.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13324C))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Información de Sesión", style = MaterialTheme.typography.titleMedium, color = Color(0xFF35D5B1))
                    HorizontalDivider(color = Color(0xFFA8B8C5).copy(alpha = 0.2f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estado de conexión", color = Color(0xFFA8B8C5))
                        Text(if (isOffline) "Sin conexión (Offline)" else "En línea", color = Color.White)
                    }
                    if (lastValidationTime != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Última validación", color = Color(0xFFA8B8C5))
                            Text(lastValidationTime, color = Color.White)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Versión de la App", color = Color(0xFFA8B8C5))
                        Text("1.0.0 (Modern UI)", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión", color = Color.White)
            }
        }
    }
}
