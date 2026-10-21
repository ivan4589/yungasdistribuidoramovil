package com.example.yungasdistribuidora.presentation.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresaleDetailScreen(
    viewModel: PresaleDetailViewModel,
    isAdmin: Boolean,
    onNavigateBack: () -> Unit
) {
    val sale by viewModel.sale.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Preventa") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (isLoading && sale == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (sale == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No se encontró la preventa", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            val s = sale!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Preventa N.º ${s.saleNumber}", style = MaterialTheme.typography.titleLarge)
                        HorizontalDivider()
                        Text("Cliente: ${s.clientName ?: s.clientId}", style = MaterialTheme.typography.bodyLarge)
                        Text("Estado: ${s.status}", style = MaterialTheme.typography.bodyMedium)
                        Text("Estado de Pago: ${s.paymentStatus}", style = MaterialTheme.typography.bodyMedium)
                        Text("Tipo de Venta: ${s.saleType}", style = MaterialTheme.typography.bodyMedium)
                        if (!s.dueDate.isNullOrBlank()) {
                            Text("Fecha de Vencimiento: ${s.dueDate}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("Subtotal: Bs. ${"%.2f".format(s.subtotal)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Descuento: Bs. ${"%.2f".format(s.discount)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Total: Bs. ${"%.2f".format(s.total)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        if (!s.observations.isNullOrBlank()) {
                            Text("Observaciones: ${s.observations}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Details / Items Card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Productos", style = MaterialTheme.typography.titleMedium)
                        s.details?.forEach { detail ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${detail.productName ?: detail.productId} (x${detail.quantity})", modifier = Modifier.weight(1f))
                                Text("Bs. ${"%.2f".format(detail.subtotal)}")
                            }
                        }
                    }
                }

                // Bonuses Card if any
                if (!s.bonuses.isNullOrEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Bonificaciones", style = MaterialTheme.typography.titleMedium)
                            s.bonuses.forEach { bonus ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(bonus.productName ?: bonus.productId, modifier = Modifier.weight(1f))
                                    Text("x${bonus.quantity}")
                                }
                            }
                        }
                    }
                }

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (s.status == "PENDING") {
                        Button(
                            onClick = { viewModel.confirmSale() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirmar")
                        }
                    }
                    if (isAdmin && s.status != "CANCELLED") {
                        OutlinedButton(
                            onClick = { viewModel.cancelSale() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancelar")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.sendWhatsapp(resend = false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp")
                    }
                    Button(
                        onClick = { viewModel.downloadAndOpenPdf(context) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver PDF")
                    }
                }
            }
        }
    }
}
