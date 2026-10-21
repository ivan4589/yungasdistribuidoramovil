package com.example.yungasdistribuidora.presentation.clients.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.yungasdistribuidora.domain.model.Client
import com.example.yungasdistribuidora.domain.model.ClientType
import com.example.yungasdistribuidora.presentation.components.YungasBottomBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    viewModel: ClientsViewModel,
    isAdmin: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val clients by viewModel.clients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedLocationId by viewModel.selectedLocationId.collectAsState()
    val includeInactive by viewModel.includeInactive.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val lastSyncStr = remember(lastSyncTime) {
        lastSyncTime?.let { dateFormat.format(Date(it)) } ?: "Nunca"
    }

    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clientes (${clients.size})") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF103B56),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData(isInitial = false) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        bottomBar = {
            YungasBottomBar(
                currentRoute = "clients",
                isAdmin = isAdmin,
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = Color(0xFF35D5B1),
                contentColor = Color(0xFF071E30)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Cliente")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF071E30))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sync & Status Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sincronización: $lastSyncStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA8B8C5)
                )
                TextButton(onClick = { showFilters = !showFilters }) {
                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF35D5B1))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showFilters) "Ocultar filtros" else "Filtros", color = Color(0xFF35D5B1))
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre, alias, teléfono...", color = Color(0xFFA8B8C5)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFA8B8C5)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = Color(0xFFA8B8C5))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF35D5B1),
                    unfocusedBorderColor = Color(0xFFA8B8C5).copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Collapsible Filters
            if (showFilters) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13324C))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tipo de Cliente", style = MaterialTheme.typography.labelMedium, color = Color(0xFF35D5B1))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedType == null,
                                onClick = { viewModel.setSelectedType(null) },
                                label = { Text("Todos") }
                            )
                            ClientType.entries.forEach { type ->
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick = { viewModel.setSelectedType(type) },
                                    label = { Text(type.name) }
                                )
                            }
                        }

                        Text("Localidad", style = MaterialTheme.typography.labelMedium, color = Color(0xFF35D5B1))
                        var locationExpanded by remember { mutableStateOf(false) }
                        val selectedLocName = locations.find { it.id == selectedLocationId }?.name ?: "Todas las localidades"
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { locationExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedLocName)
                            }
                            DropdownMenu(
                                expanded = locationExpanded,
                                onDismissRequest = { locationExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todas las localidades") },
                                    onClick = {
                                        viewModel.setSelectedLocationId(null)
                                        locationExpanded = false
                                    }
                                )
                                locations.forEach { loc ->
                                    DropdownMenuItem(
                                        text = { Text(loc.name) },
                                        onClick = {
                                            viewModel.setSelectedLocationId(loc.id)
                                            locationExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (isAdmin) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Incluir inactivos", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                Switch(
                                    checked = includeInactive,
                                    onCheckedChange = viewModel::setIncludeInactive
                                )
                            }
                        }
                    }
                }
            }

            // Error banner
            errorMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF5350).copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = msg, color = Color.White, modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.refreshData(false) }) {
                            Text("Reintentar", color = Color(0xFF35D5B1))
                        }
                    }
                }
            }

            // Main Content
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF35D5B1))
                }
            } else if (clients.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFFA8B8C5))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No se encontraron clientes", style = MaterialTheme.typography.bodyLarge, color = Color(0xFFA8B8C5))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(clients, key = { it.id }) { client ->
                        ClientItemCard(client = client, onClick = { onNavigateToDetail(client.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun ClientItemCard(client: Client, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13324C)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular initials avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF35D5B1).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                val initial = if (client.fullName.isNotBlank()) client.fullName.take(1).uppercase() else "C"
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF35D5B1)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = client.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Surface(
                        color = when (client.type) {
                            ClientType.NORMAL -> Color(0xFF103B56)
                            ClientType.ESPECIAL -> Color(0xFF087C69)
                            ClientType.CAMINO -> Color(0xFFF5B942)
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = client.type.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                if (!client.alias.isNullOrBlank()) {
                    Text(
                        text = "Alias: ${client.alias}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA8B8C5)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = client.locationName ?: client.locationId,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA8B8C5)
                    )
                    if (!client.phone.isNullOrBlank()) {
                        Text(
                            text = client.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF35D5B1)
                        )
                    }
                }

                if (!client.isActive) {
                    Text(
                        text = "INACTIVO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFEF5350)
                    )
                }
            }
        }
    }
}
