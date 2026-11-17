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
import com.example.yungasdistribuidora.data.remote.dto.DeliveryProductDto
import com.example.yungasdistribuidora.domain.model.Product

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
    var showActiveDeliveryEditDialog by remember { mutableStateOf(false) }
    var modifyingItem by remember { mutableStateOf<DeliveryProductDto?>(null) }
    var newQtyInput by remember { mutableStateOf("") }
    var eventQty by remember { mutableStateOf("") }
    var selectedProductId by remember { mutableStateOf("") }

    val savePdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { viewModel.downloadAndOpenPdf(context, it) }
    }

    if (confirmWhatsApp) {
        AlertDialog(
            onDismissRequest = { confirmWhatsApp = false },
            title = { Text("Enviar recibo por WhatsApp") },
            text = { Text("Se enviará al número principal y contactos autorizados.") },
            confirmButton = { TextButton(onClick = { confirmWhatsApp = false; viewModel.sendWhatsapp(false) }) { Text("Enviar") } },
            dismissButton = { TextButton(onClick = { confirmWhatsApp = false }) { Text("Cancelar") } }
        )
    }

    if (showDeliveryDialog) {
        AlertDialog(
            onDismissRequest = { showDeliveryDialog = false },
            title = { Text("Registrar Entrega") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val products = delivery?.products ?: emptyList()
                    var productExpanded by remember { mutableStateOf(false) }
                    val selectedProd = products.find { it.productId == selectedProductId } ?: products.firstOrNull()

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { productExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(selectedProd?.name ?: "Seleccione producto")
                        }
                        DropdownMenu(expanded = productExpanded, onDismissRequest = { productExpanded = false }) {
                            products.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text("${p.name} (Pendiente: ${p.pending ?: 0})") },
                                    onClick = { selectedProductId = p.productId ?: ""; productExpanded = false }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = eventQty,
                        onValueChange = { eventQty = it },
                        label = { Text("Cantidad a entregar") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val qty = eventQty.toIntOrNull() ?: 0
                    val prodId = selectedProductId.ifBlank { delivery?.products?.firstOrNull()?.productId ?: "" }
                    val rev = delivery?.revision ?: 0
                    viewModel.recordDeliveryEvent(prodId, "DELIVER", qty, rev)
                    showDeliveryDialog = false
                    eventQty = ""
                }) { Text("Registrar Entrega") }
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
                    val products = delivery?.products ?: emptyList()
                    var productExpanded by remember { mutableStateOf(false) }
                    val selectedProd = products.find { it.productId == selectedProductId } ?: products.firstOrNull()

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { productExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(selectedProd?.name ?: "Seleccione producto")
                        }
                        DropdownMenu(expanded = productExpanded, onDismissRequest = { productExpanded = false }) {
                            products.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text("${p.name} (Entregado: ${p.delivered ?: 0})") },
                                    onClick = { selectedProductId = p.productId ?: ""; productExpanded = false }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = eventQty,
                        onValueChange = { eventQty = it },
                        label = { Text("Cantidad devuelta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val qty = eventQty.toIntOrNull() ?: 0
                    val prodId = selectedProductId.ifBlank { delivery?.products?.firstOrNull()?.productId ?: "" }
                    val rev = delivery?.revision ?: 0
                    viewModel.recordDeliveryEvent(prodId, "RETURN", qty, rev)
                    showReturnDialog = false
                    eventQty = ""
                }) { Text("Registrar Devolución") }
            },
            dismissButton = { TextButton(onClick = { showReturnDialog = false }) { Text("Cancelar") } }
        )
    }

    if (showActiveDeliveryEditDialog) {
        val catalogProducts by viewModel.catalogProducts.collectAsState()
        val currentProducts = delivery?.products ?: emptyList()

        val editableQuantities = remember(currentProducts) {
            mutableStateMapOf<String, String>().apply {
                currentProducts.forEach { p ->
                    put(p.productId ?: "", (p.ordered ?: 0).toString())
                }
            }
        }
        var showAddProductDialog by remember { mutableStateOf(false) }
        var selectedCatalogProd by remember { mutableStateOf<Product?>(null) }
        var newProdQtyInput by remember { mutableStateOf("1") }
        var productDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showActiveDeliveryEditDialog = false },
            title = { Text("Editar pedido con reparto activo") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Ajuste cantidades solicitadas o agregue nuevos productos. No puede reducir por debajo de lo ya entregado.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    HorizontalDivider()

                    currentProducts.forEach { p ->
                        val pId = p.productId ?: ""
                        val delivered = p.delivered ?: 0
                        val currentQtyStr = editableQuantities[pId] ?: (p.ordered ?: 0).toString()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.name ?: pId, style = MaterialTheme.typography.bodyMedium)
                                Text("Entregado: $delivered | Pendiente: ${p.pending ?: 0}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                            OutlinedTextField(
                                value = currentQtyStr,
                                onValueChange = { editableQuantities[pId] = it },
                                label = { Text("Cant.") },
                                modifier = Modifier.width(90.dp)
                            )
                        }
                    }

                    HorizontalDivider()
                    Button(onClick = { showAddProductDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar nuevo producto")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val updates = mutableMapOf<String, Int>()
                    var isValid = true
                    for ((pId, qStr) in editableQuantities) {
                        val q = qStr.toIntOrNull()
                        if (q == null || q < 0) {
                            isValid = false
                            break
                        }
                        val prod = currentProducts.find { it.productId == pId }
                        val delivered = prod?.delivered ?: 0
                        if (q < delivered) {
                            isValid = false
                            break
                        }
                        updates[pId] = q
                    }
                    if (isValid) {
                        val rev = delivery?.revision ?: 0
                        viewModel.updateDeliveryQuantities(updates, rev) {
                            showActiveDeliveryEditDialog = false
                        }
                    }
                }) { Text("Guardar cambios") }
            },
            dismissButton = {
                TextButton(onClick = { showActiveDeliveryEditDialog = false }) { Text("Cancelar") }
            }
        )

        if (showAddProductDialog) {
            AlertDialog(
                onDismissRequest = { showAddProductDialog = false },
                title = { Text("Agregar producto al pedido") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { productDropdownExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(selectedCatalogProd?.name ?: "Seleccione producto del catálogo")
                            }
                            DropdownMenu(expanded = productDropdownExpanded, onDismissRequest = { productDropdownExpanded = false }) {
                                catalogProducts.forEach { cp ->
                                    DropdownMenuItem(
                                        text = { Text("${cp.name} (Stock: ${cp.availableStock.toInt()})") },
                                        onClick = { selectedCatalogProd = cp; productDropdownExpanded = false }
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = newProdQtyInput,
                            onValueChange = { newProdQtyInput = it },
                            label = { Text("Cantidad solicitada") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val selProd = selectedCatalogProd
                        val qty = newProdQtyInput.toIntOrNull() ?: 1
                        if (selProd != null && qty > 0) {
                            val pId = selProd.id
                            editableQuantities[pId] = qty.toString()
                            showAddProductDialog = false
                            selectedCatalogProd = null
                            newProdQtyInput = "1"
                        }
                    }) { Text("Agregar") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddProductDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }

    modifyingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { modifyingItem = null; newQtyInput = "" },
            title = { Text("Modificar cantidad solicitada: ${item.name ?: item.productId}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Solicitado actual: ${item.ordered}, Entregado: ${item.delivered}")
                    OutlinedTextField(
                        value = newQtyInput,
                        onValueChange = { newQtyInput = it },
                        label = { Text("Nueva cantidad solicitada") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val q = newQtyInput.toIntOrNull() ?: item.ordered ?: 0
                    val rev = delivery?.revision ?: 0
                    viewModel.updateDeliveryQuantity(item.productId ?: "", q, rev)
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
            val products = d?.products ?: s.details?.map {
                DeliveryProductDto(
                    saleDetailId = it.id,
                    productId = it.productId,
                    name = it.productName,
                    ordered = it.quantity,
                    delivered = 0,
                    pending = it.quantity
                )
            } ?: emptyList()

            val isComplete = d?.complete == true || (products.isNotEmpty() && products.all { (it.pending ?: 0) <= 0 })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
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
                        Text("Total Preventa", style = MaterialTheme.typography.bodyMedium)
                        Text("Bs. ${"%.2f".format(s.total)}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }

                val deliveryNotStarted = d == null || d?.deliveryMode != true

                if (deliveryNotStarted && s.status != "CANCELLED" && s.status != "CONFIRMED") {
                    Button(
                        onClick = { viewModel.startDelivery() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Entrega / Control de Ruta")
                    }
                }

                // Products & Deliveries
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Productos y Entregas", style = MaterialTheme.typography.titleMedium)
                            if (!deliveryNotStarted && s.status != "CONFIRMED") {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(onClick = { showDeliveryDialog = true }) {
                                        Text("Entrega")
                                    }
                                    TextButton(onClick = { showReturnDialog = true }) {
                                        Text("Devolución")
                                    }
                                }
                            }
                        }
                        HorizontalDivider()

                        products.forEach { prod ->
                            val ord = prod.ordered ?: 0
                            val del = prod.delivered ?: 0
                            val pen = prod.pending ?: maxOf(0, ord - del)
                            val progress = if (ord > 0) (del.toFloat() / ord.toFloat()).coerceIn(0f, 1f) else 0f
                            val status = when {
                                del == 0 -> "No entregado"
                                pen > 0 -> "Parcial"
                                else -> "Completo"
                            }
                            val statusColor = when (status) {
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
                                    Text(prod.name ?: prod.productId ?: "", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                    Surface(
                                        color = statusColor.copy(alpha = 0.2f),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = status,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = statusColor
                                        )
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Solicitado: $ord | Entregado: $del | Pendiente: $pen", style = MaterialTheme.typography.bodySmall)
                                }
                                LinearProgressIndicator(
                                    progress = progress,
                                    modifier = Modifier.fillMaxWidth().height(6.dp)
                                )
                                if (d != null && s.status != "CONFIRMED" && s.status != "CANCELLED") {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = { modifyingItem = prod }) {
                                            Text("Modificar pedido")
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }

                // Delivery History Events
                if (!d?.events.isNullOrEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Historial de Entregas", style = MaterialTheme.typography.titleMedium)
                            HorizontalDivider()
                            d!!.events!!.forEach { ev ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${ev.kind ?: "EVENTO"} - Cantidad: ${ev.quantity ?: 0}", style = MaterialTheme.typography.labelMedium)
                                        Text("Usuario ID: ${ev.userId ?: "N/D"} | ${ev.createdAt ?: ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            }
                        }
                    }
                }

                // Edit order button (if PENDING)
                if (s.status == "PENDING") {
                    val deliveryActive = d?.deliveryMode == true
                    Button(
                        onClick = {
                            if (!deliveryActive) {
                                onEdit()
                            } else {
                                showActiveDeliveryEditDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar pedido")
                    }
                }

                // Action buttons (WhatsApp, Ver PDF)
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

                // Confirm sale button
                if (s.status != "CONFIRMED" && s.status != "CANCELLED") {
                    Button(
                        onClick = { viewModel.confirmSale() },
                        enabled = !isLoading && isComplete,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isComplete) "Confirmar Venta" else "Pedido incompleto (Faltan entregas)")
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
