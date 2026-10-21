package com.example.yungasdistribuidora.presentation.sales

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.yungasdistribuidora.presentation.components.YungasBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPresalesScreen(
    viewModel: MyPresalesViewModel,
    isAdmin: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToNewPresale: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val sales by viewModel.filteredSales.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val paymentStatusFilter by viewModel.paymentStatusFilter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var statusExpanded by remember { mutableStateOf(false) }
    var payStatusExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Preventas y Ventas") },
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
                    IconButton(onClick = { viewModel.loadSales() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        bottomBar = {
            YungasBottomBar(
                currentRoute = "my_presales",
                isAdmin = isAdmin,
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewPresale,
                containerColor = Color(0xFF35D5B1),
                contentColor = Color(0xFF071E30)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Preventa")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF071E30))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por N.º, cliente u observaciones...", color = Color(0xFFA8B8C5)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFA8B8C5)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF35D5B1),
                    unfocusedBorderColor = Color(0xFFA8B8C5).copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Filter Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { statusExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Estado: ${statusFilter ?: "Todos"}")
                    }
                    DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        DropdownMenuItem(text = { Text("Todos") }, onClick = { viewModel.setStatusFilter(null); statusExpanded = false })
                        listOf("PENDING", "CONFIRMED", "CANCELLED", "DELIVERED").forEach { st ->
                            DropdownMenuItem(text = { Text(st) }, onClick = { viewModel.setStatusFilter(st); statusExpanded = false })
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { payStatusExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Pago: ${paymentStatusFilter ?: "Todos"}")
                    }
                    DropdownMenu(expanded = payStatusExpanded, onDismissRequest = { payStatusExpanded = false }) {
                        DropdownMenuItem(text = { Text("Todos") }, onClick = { viewModel.setPaymentStatusFilter(null); payStatusExpanded = false })
                        listOf("PENDING", "PARTIAL", "PAID").forEach { pst ->
                            DropdownMenuItem(text = { Text(pst) }, onClick = { viewModel.setPaymentStatusFilter(pst); payStatusExpanded = false })
                        }
                    }
                }
            }

            if (isLoading && sales.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF35D5B1))
                }
            } else if (sales.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFFA8B8C5))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No se encontraron preventas", color = Color(0xFFA8B8C5), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(sales, key = { it.id }) { sale ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToDetail(sale.id) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF13324C)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("N.º ${sale.saleNumber}", style = MaterialTheme.typography.titleMedium, color = Color.White)
                                    Text("Bs. ${"%.2f".format(sale.total)}", style = MaterialTheme.typography.titleMedium, color = Color(0xFF35D5B1))
                                }
                                Text("Cliente: ${sale.clientName ?: sale.clientId}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFA8B8C5))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        color = when (sale.status) {
                                            "CONFIRMED" -> Color(0xFF087C69)
                                            "PENDING" -> Color(0xFFF5B942)
                                            else -> Color(0xFFEF5350)
                                        },
                                        shape = MaterialTheme.shapes.extraSmall
                                    ) {
                                        Text(
                                            text = "Estado: ${sale.status}",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                    Text("Pago: ${sale.paymentStatus}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA8B8C5))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
