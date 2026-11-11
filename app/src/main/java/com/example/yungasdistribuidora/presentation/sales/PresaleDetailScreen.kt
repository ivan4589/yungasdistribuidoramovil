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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.yungasdistribuidora.data.remote.dto.DeliveryItemDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresaleDetailScreen(
    viewModel: PresaleDetailViewModel,
    isAdmin: Boolean,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit
) {
    val sale by viewModel.sale.collectAsState()
    val delivery by viewModel.delivery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val context = LocalContext.current
    var confirmWhatsApp by remember { mutableStateOf(false) }
    var showDeliveryDialog by remember { mutableStateOf(false) }
    var showReturnDialog by remember { mutableStateOf(false) }
    var modifyingItem by remember { mutableStateOf<DeliveryItemDto?>(null) }
    var newQtyInput by remember { mutableStateOf("") }
    var eventObs by remember { mutableStateOf("") }
    var selectedProductIdForEvent by remember { mutableStateOf("") }
    var eventQty by remember { mutableStateOf("") }

    val savePdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { viewModel.downloadAndOpenPdf(context, it) }
    }

    if (confirmWhatsApp) {
        AlertDialog(
            onDismissRequest = { confirmWhatsApp = false },
            title = { Text("Enviar recibo por WhatsApp") },
            text = { Text("Se enviará al número principal y a todos los contactos activos que autorizaron recibir documentos.") },
            confirmButton = { TextButton(onClick = { confirmWhatsApp = false; viewModel.sendWhatsapp(false) }) { Text("Enviar a todos") } },
            dismissButton = { TextButton(onClick = { confirmWhatsApp = false }) { Text("Cancelar") } }
        )
    }

    if (showDeliveryDialog) {
        AlertDialog(
            onDismissRequest = { showDeliveryDialog = false },
            title = { Text("Registrar Entrega Parcial") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val items = delivery?.items ?: emptyList()
                    OutlinedTextField(
                        value = eventQty,
                        onValueChange = { eventQty = it },
                        label = { Text("Cantidad a entregar") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = eventObs,
                        onValueChange = { eventObs = it },
                        label = { Text("Observaciones") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val qty = eventQty.toIntOrNull() ?: 0
                    viewModel.postDeliveryEvent("DELIVERY", qty, selectedProductIdForEvent.ifBlank { delivery?.items?.firstOrNull()?.productId }, eventObs)
                    showDeliveryDialog = false
                    eventQty = ""
                    eventObs = ""
                }) { Text("Registrar") }
            },
            dismissButton = { TextButton(onClick = { showDeliveryDialog = false }) { Text("Cancelar") } }
        )
    }

    if (showReturnDialog) {
        AlertDialog(
            onDismissRequest = { showReturnDialog = false },
            title = { Text("Registrar Devolución") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = eventQty,
                        onValueChange = { eventQty = it },
                        label = { Text("Cantidad devuelta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = eventObs,
                        onValueChange = { eventObs = it },
                        label = { Text("Motivo / Observaciones") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val qty = eventQty.toIntOrNull() ?: 0
                    viewModel.postDeliveryEvent("RETURN", qty, delivery?.items?.firstOrNull()?.productId, eventObs)
                    showReturnDialog = false
                    eventQty = ""
                    eventObs = ""
                }) { Text("Registrar Devolución") }
            },
            dismissButton = { TextButton(onClick = { showReturnDialog = false }) { Text("Cancelar") } }
        )
    }

    modifyingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { modifyingItem = null; newQtyInput = "" },
            title = { Text("Modificar cantidad entregada: ${item.productName ?: item.productId}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Solicitado: ${item.quantityOrdered}, Actual entregado: ${item.quantityDelivered}")
                    OutlinedTextField(
                        value = newQtyInput,
                        onValueChange = { newQtyInput = it },
                        label = { Text("Nueva cantidad entregada") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val q = newQtyInput.toIntOrNull() ?: item.quantityDelivered
                    viewModel.updateDeliveryQuantity(item.productId ?: "", q, item.itemRevision)
                    modifyingItem = null
                    newQtyInput = ""
                }) { Text("Actualizar") }
            },
            dismissButton = { TextButton(onClick = { modifyingItem = null; newQtyInput = "" }) { Text("Cancelar") } }
        )
    }

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
                title = { Text("Detalle de Preventa y Entregas") },
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
            val d = delivery
            val deliveryItems = d?.items ?: s.details?.map {
                DeliveryItemDto(
                    productId = it.productId,
                    productName = it.productName,
                    ordered = it.quantity,
                    solicitado = null,
                    delivered = 0,
                    entregado = null,
                    pending = it.quantity,
                    pendiente = null,
                    revision = 0,
                    status = "PENDING",
                    attribution = null
                )
            } ?: emptyList()

            val allFullyDelivered = deliveryItems.isNotEmpty() && deliveryItems.all { it.quantityPending <= 0 }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with presale number and status badge
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Preventa N.º ${s.saleNumber}", style = MaterialTheme.typography.titleLarge)
                            Surface(
                                color = when (s.status) {
                                    "CONFIRMED" -> MaterialTheme.colorScheme.primaryContainer
                                    "CANCELLED" -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.secondaryContainer
                                },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = s.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        HorizontalDivider()
                        Text("Cliente: ${s.clientName ?: s.clientId}", style = MaterialTheme.typography.bodyLarge)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tipo: ${s.saleType}", style = MaterialTheme.typography.bodyMedium)
                            Text("Pago: ${s.paymentStatus}", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (!s.dueDate.isNullOrBlank()) {
                            Text("Vencimiento: ${s.dueDate}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Total Amount Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Preventa", style = MaterialTheme.typography.bodyMedium)
                            if (s.discount > 0) {
                                Text("Subtotal: Bs. ${"%.2f".format(s.subtotal)} | Desc: Bs. ${"%.2f".format(s.discount)}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text("Bs. ${"%.2f".format(s.total)}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Delivery Summary Cards (No entregado, Parcial, Completo)
                val summary = d?.summary
                val noEntregadoCount = summary?.noEntregadoCount ?: deliveryItems.count { it.quantityDelivered == 0 }
                val parcialCount = summary?.parcialCountVal ?: deliveryItems.count { it.quantityDelivered > 0 && it.quantityPending > 0 }
                val completoCount = summary?.completoCountVal ?: deliveryItems.count { it.quantityDelivered > 0 && it.quantityPending <= 0 }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No Entregado", style = MaterialTheme.typography.labelSmall)
                            Text("$noEntregadoCount", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Parcial", style = MaterialTheme.typography.labelSmall)
                            Text("$parcialCount", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Completo", style = MaterialTheme.typography.labelSmall)
                            Text("$completoCount", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (d == null && s.status != "CANCELLED" && s.status != "CONFIRMED") {
                    Button(
                        onClick = { viewModel.startDelivery() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Entrega / Control de Ruta")
                    }
                }

                // Products List
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Productos y Entregas Parciales", style = MaterialTheme.typography.titleMedium)
                            if (d != null) {
                                TextButton(onClick = { showDeliveryDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Registrar Entrega")
                                }
                            }
                        }
                        HorizontalDivider()

                        deliveryItems.forEach { item ->
                            val ordered = item.quantityOrdered
                            val delivered = item.quantityDelivered
                            val pending = item.quantityPending
                            val progress = if (ordered > 0) (delivered.toFloat() / ordered.toFloat()).coerceIn(0f, 1f) else 0f
                            val itemStatus = when {
                                delivered == 0 -> "No entregado"
                                pending > 0 -> "Parcial"
                                else -> "Completo"
                            }
                            val statusColor = when (itemStatus) {
                                "Completo" -> MaterialTheme.colorScheme.primary
                                "Parcial" -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            }

                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.productName ?: item.productId ?: "", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                    Surface(
                                        color = statusColor.copy(alpha = 0.2f),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = itemStatus,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = statusColor
                                        )
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Solicitado: $ordered | Entregado: $delivered | Pendiente: $pending", style = MaterialTheme.typography.bodySmall)
                                    if (!item.attribution.isNullOrBlank()) {
                                        Text("Por: ${item.attribution}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                LinearProgressIndicator(
                                    progress = progress,
                                    modifier = Modifier.fillMaxWidth().height(6.dp)
                                )
                                if (d != null && s.status != "CONFIRMED" && s.status != "CANCELLED") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { modifyingItem = item }) {
                                            Text("Modificar cantidad")
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }

                // Delivery History Timeline ("Historial de entregas")
                if (!d?.events.isNullOrEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Historial de Entregas y Eventos", style = MaterialTheme.typography.titleMedium)
                            HorizontalDivider()
                            d!!.events!!.forEach { event ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${event.type ?: "EVENTO"} - ${event.timestamp ?: ""}", style = MaterialTheme.typography.labelMedium)
                                        if (!event.description.isNullOrBlank()) {
                                            Text(event.description!!, style = MaterialTheme.typography.bodySmall)
                                        }
                                        if (!event.details.isNullOrBlank()) {
                                            Text(event.details!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                        }
                                        if (!event.attribution.isNullOrBlank()) {
                                            Text("Responsable: ${event.attribution}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (d != null && s.status != "CONFIRMED" && s.status != "CANCELLED") {
                        OutlinedButton(
                            onClick = { showReturnDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Devolución")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { confirmWhatsApp = true },
                        enabled = !isLoading && s.status == "CONFIRMED",
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp")
                    }
                    Button(
                        onClick = { savePdf.launch("nota-venta-${s.saleNumber.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.pdf") },
                        enabled = !isLoading && s.status == "CONFIRMED",
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver PDF")
                    }
                }

                // Confirm sale button (enabled only when all items are fully delivered)
                if (s.status == "PENDING" || s.status == "IN_PROGRESS" || s.status == "PARTIAL") {
                    Button(
                        onClick = { viewModel.confirmSale() },
                        enabled = !isLoading && allFullyDelivered,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (allFullyDelivered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (allFullyDelivered) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (allFullyDelivered) "Confirmar Venta" else "Entregue todos los ítems para confirmar venta")
                    }
                }

                if (isAdmin && s.status != "CANCELLED") {
                    OutlinedButton(
                        onClick = { viewModel.cancelSale() },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancelar Preventa")
                    }
                }
            }
        }
    }
}
