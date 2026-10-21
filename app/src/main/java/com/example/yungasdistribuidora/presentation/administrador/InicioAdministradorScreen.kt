package com.example.yungasdistribuidora.presentation.administrador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.yungasdistribuidora.R
import com.example.yungasdistribuidora.presentation.components.ConnectionSyncStatusComponent
import com.example.yungasdistribuidora.presentation.components.SyncState
import com.example.yungasdistribuidora.presentation.components.YungasBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioAdministradorScreen(
    userName: String,
    userEmail: String,
    isOffline: Boolean,
    lastValidationTime: String?,
    onNavigateToClients: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToNewPresale: () -> Unit,
    onNavigateToMyPresales: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo_distribuidora),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified
                        )
                        Text("Yungas Distribuidora", style = MaterialTheme.typography.titleMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF103B56),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión", tint = Color(0xFFEF5350))
                    }
                }
            )
        },
        bottomBar = {
            YungasBottomBar(
                currentRoute = "admin_home",
                isAdmin = true,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF071E30))
        ) {
            ConnectionSyncStatusComponent(
                syncState = if (isOffline) SyncState.OFFLINE else SyncState.ONLINE_SYNCED,
                lastValidationTime = lastValidationTime
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13324C))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF35D5B1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (userName.isNotBlank()) userName.take(1).uppercase() else "A",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF071E30)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = userName.ifBlank { "Administrador" }, style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(text = userEmail.ifBlank { "admin@yungasdistribuidora.cc" }, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA8B8C5))
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color(0xFF087C69),
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = "ADMIN",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Gestión Principal",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFA8B8C5)
                )

                // 4 Feature Cards Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        HomeActionCard(
                            title = "Gestión de Clientes",
                            subtitle = "Administrar clientes y rutas",
                            icon = Icons.Default.Person,
                            accentColor = Color(0xFF35D5B1),
                            onClick = onNavigateToClients
                        )
                    }
                    item {
                        HomeActionCard(
                            title = "Catálogo Productos",
                            subtitle = "Inventario y precios",
                            icon = Icons.Default.Info,
                            accentColor = Color(0xFF35D5B1),
                            onClick = onNavigateToProducts
                        )
                    }
                    item {
                        HomeActionCard(
                            title = "Nueva Preventa",
                            subtitle = "Registrar nueva venta",
                            icon = Icons.Default.ShoppingCart,
                            accentColor = Color(0xFF087C69),
                            onClick = onNavigateToNewPresale
                        )
                    }
                    item {
                        HomeActionCard(
                            title = "Mis Preventas",
                            subtitle = "Historial y estados",
                            icon = Icons.Default.Done,
                            accentColor = Color(0xFFF5B942),
                            onClick = onNavigateToMyPresales
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13324C)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFFA8B8C5))
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA8B8C5),
                    maxLines = 1
                )
            }
        }
    }
}
