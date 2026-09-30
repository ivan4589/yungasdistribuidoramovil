package com.example.yungasdistribuidora.presentation.clients.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.yungasdistribuidora.domain.model.Client
import com.example.yungasdistribuidora.domain.model.ClientType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    viewModel: ClientsViewModel,
    isAdmin: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit
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
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Cliente")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Sync & Status Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Última sincronización: $lastSyncStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { showFilters = !showFilters }) {
                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showFilters) "Ocultar filtros" else "Filtros")
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre, alias, teléfono...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true
            )

            // Collapsible Filters
            if (showFilters) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Tipo de Cliente", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Localidad", style = MaterialTheme.typography.labelMedium)
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
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Incluir inactivos", style = MaterialTheme.typography.bodyMedium)
                                Switch(
                                    checked = includeInactive,
                                    onCheckedChange = viewModel::setIncludeInactive
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Error banner if any
            errorMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = msg, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.refreshData(false) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            // Main Content: Loading or List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (clients.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No se encontraron clientes", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = client.fullName,
                    style = MaterialTheme.typography.titleMedium
                )
                Surface(
                    color = when (client.type) {
                        ClientType.NORMAL -> MaterialTheme.colorScheme.primaryContainer
                        ClientType.ESPECIAL -> MaterialTheme.colorScheme.tertiaryContainer
                        ClientType.CAMINO -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = client.type.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            if (!client.alias.isNullOrBlank()) {
                Text(
                    text = "Alias: ${client.alias}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Localidad: ${client.locationName ?: client.locationId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!client.phone.isNullOrBlank()) {
                    Text(
                        text = client.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!client.isActive) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "INACTIVO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
