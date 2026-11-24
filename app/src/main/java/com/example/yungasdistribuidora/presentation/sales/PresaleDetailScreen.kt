package com.example.yungasdistribuidora.presentation.sales

 import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.yungasdistribuidora.data.remote.dto.DeliveryProductDto
import com.example.yungasdistribuidora.domain.model.Product
import java.text.Normalizer

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
    val editLines by viewModel.editDraftLines.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val context = LocalContext.current

    var confirmWhatsApp by rememberSaveable { mutableStateOf(false) }
    var showDeliveryDialog by rememberSaveable { mutableStateOf(false) }
    var showReturnDialog by rememberSaveable { mutableStateOf(false) }
    var showActiveDeliveryEditDialog by rememberSaveable { mutableStateOf(false) }
    var returnProductTargetId by rememberSaveable { mutableStateOf<String?>(null) }
    val returnProductTarget = delivery?.products?.firstOrNull { it.productId == returnProductTargetId }
    var returnEventQty by rememberSaveable { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    
    var modifyingItemId by rememberSaveable { mutableStateOf<String?>(null) }
    val modifyingItem = delivery?.products?.firstOrNull { it.productId == modifyingItemId }
    var newQtyInput by rememberSaveable { mutableStateOf("") }
    var eventQty by rememberSaveable { mutableStateOf("") }
    var selectedProductId by rememberSaveable { mutableStateOf("") }

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

    returnProductTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { returnProductTargetId = null; returnEventQty = "" },
            title = { Text("Registrar Devolución: ${target.name ?: target.productId}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Entregado actual: ${target.delivered}. Indique cuántas unidades se devuelven:")
                    OutlinedTextField(
                        value = returnEventQty,
                        onValueChange = { returnEventQty = it },
                        label = { Text("Cantidad devuelta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val qty = returnEventQty.toIntOrNull() ?: 0
                    val rev = delivery?.revision ?: 0
                    if (qty > 0 && qty <= (target.delivered ?: 0)) {
                        viewModel.recordDeliveryEvent(target.productId ?: "", "RETURN", qty, rev)
                        returnProductTargetId = null
                        returnEventQty = ""
                    }
                }) { Text("Confirmar Devolución") }
            },
            dismissButton = {
                TextButton(onClick = { returnProductTargetId = null; returnEventQty = "" }) { Text("Cancelar") }
            }
        )
    }

    validationMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { validationMessage = null },
            title = { Text("Aviso de Validación") },
            text = { Text(msg) },
            confirmButton = { Button(onClick = { validationMessage = null }) { Text("Aceptar") } }
        )
    }

    if (showActiveDeliveryEditDialog) {
        val catalogProducts by viewModel.catalogProducts.collectAsState()
        val currentProducts = delivery?.products ?: emptyList()
        val s = sale
        val saleDetailsMap = s?.details?.associate { it.productId to it.unitPrice } ?: emptyMap()

        LaunchedEffect(showActiveDeliveryEditDialog) {
            if (editLines.isEmpty()) {
                viewModel.initEditDraft(currentProducts, saleDetailsMap)
            }
        }

        var showAddProductDialog by rememberSaveable { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showActiveDeliveryEditDialog = false },
            title = { Text("Editar pedido con reparto activo") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        if (isAdmin) "Ajuste cantidades solicitadas o precios (ADMIN)." else "Ajuste cantidades solicitadas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    HorizontalDivider()

                    editLines.forEach { line ->
                        val pId = line.productId
                        val delivered = line.delivered
                        val currentQtyStr = line.ordered.toString()
                        val currentPriceStr = line.unitPrice.toString()
                        val subtotalPreview = line.ordered * line.unitPrice

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(line.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        if (delivered > 0) {
                                            validationMessage = "Este producto tiene unidades entregadas. Primero debe registrar la devolución correspondiente."
                                        } else if (editLines.size <= 1) {
                                            validationMessage = "El pedido debe tener al menos un producto."
                                        } else {
                                            viewModel.removeDraftItem(pId)
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Quitar producto", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Text("Entregado: $delivered | Pendiente: ${maxOf(0, line.ordered - delivered)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = currentQtyStr,
                                        onValueChange = { newVal ->
                                            val q = newVal.toIntOrNull() ?: 0
                                            viewModel.updateDraftItem(pId, q, line.unitPrice)
                                        },
                                        label = { Text("Cant.") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isAdmin) {
                                        OutlinedTextField(
                                            value = currentPriceStr,
                                            onValueChange = { newVal ->
                                                val pr = newVal.toDoubleOrNull() ?: line.unitPrice
                                                viewModel.updateDraftItem(pId, line.ordered, pr)
                                            },
                                            label = { Text("Precio (Bs.)") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Text("Precio: Bs. $currentPriceStr", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }

                                if (delivered > 0) {
                                    OutlinedButton(
                                        onClick = {
                                            val targetDto = currentProducts.find { it.productId == pId } ?: DeliveryProductDto(null, pId, line.name, line.ordered, delivered, line.ordered - delivered, line.unitPrice)
                                            returnProductTargetId = targetDto.productId
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Registrar devolución")
                                    }
                                }

                                Text("Subtotal: Bs. ${"%.2f".format(subtotalPreview)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
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
                    var isValid = true
                    val updates = mutableMapOf<String, Int>()
                    val uPrices = mutableMapOf<String, Double>()
                    for (line in editLines) {
                        val delivered = currentProducts.find { it.productId == line.productId }?.delivered ?: 0
                        if (line.ordered < delivered) {
                            isValid = false
                            break
                        }
                        updates[line.productId] = line.ordered
                        uPrices[line.productId] = line.unitPrice
                    }
                    if (!isValid) {
                        validationMessage = "No se puede reducir la cantidad solicitada por debajo de las unidades ya entregadas."
                        return@Button
                    }
                    val rev = delivery?.revision ?: 0
                    val origPrices = currentProducts.associate { (it.productId ?: "") to (it.unitPrice ?: saleDetailsMap[it.productId] ?: 0.0) }
                    viewModel.saveEditDraft(currentProducts, origPrices, rev) {
                        showActiveDeliveryEditDialog = false
                    }
                }) { Text("Guardar cambios") }
            },
            dismissButton = {
                TextButton(onClick = { showActiveDeliveryEditDialog = false }) { Text("Cancelar") }
            }
        )

        if (showAddProductDialog) {
            var searchQuery by rememberSaveable { mutableStateOf("") }
            var selectedCatalogProdId by rememberSaveable { mutableStateOf<String?>(null) }
            val selectedCatalogProd = catalogProducts.firstOrNull { it.id == selectedCatalogProdId }
            var newProdQtyInput by rememberSaveable { mutableStateOf("1") }
            var newProdPriceInput by rememberSaveable { mutableStateOf("") }

            val filteredCatalog = remember(catalogProducts, searchQuery) {
                if (searchQuery.isBlank()) catalogProducts
                else {
                    val q = searchQuery.trim().lowercase().let {
                        val norm = Normalizer.normalize(it, Normalizer.Form.NFD)
                        Regex("\\p{InCombiningDiacriticalMarks}+").replace(norm, "")
                    }
                    catalogProducts.filter { cp ->
                        val codeNorm = cp.code.lowercase()
                        val nameNorm = cp.name.lowercase().let {
                            val norm = Normalizer.normalize(it, Normalizer.Form.NFD)
                            Regex("\\p{InCombiningDiacriticalMarks}+").replace(norm, "")
                        }
                        codeNorm.contains(q) || nameNorm.contains(q)
                    }
                }
            }

            AlertDialog(
                onDismissRequest = { showAddProductDialog = false },
                title = { Text("Buscar y agregar producto") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 450.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Buscar por código o nombre...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        val sel = selectedCatalogProd
                        if (sel != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Seleccionado: ${sel.name}", style = MaterialTheme.typography.titleSmall)
                                    Text("Código: ${sel.code} | Stock disp.: ${sel.availableStock.toInt()} ${sel.unit}", style = MaterialTheme.typography.bodySmall)

                                    OutlinedTextField(
                                        value = newProdQtyInput,
                                        onValueChange = { newProdQtyInput = it },
                                        label = { Text("Cantidad solicitada") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    val defaultPrice = viewModel.calculatePrice(sel, (newProdQtyInput.toDoubleOrNull() ?: 1.0))
                                    if (newProdPriceInput.isBlank()) {
                                        newProdPriceInput = defaultPrice.toString()
                                    }

                                    val qN = newProdQtyInput.toIntOrNull() ?: 1
                                    val pN = newProdPriceInput.toDoubleOrNull() ?: defaultPrice
                                    val subtotalNew = qN * pN

                                    if (isAdmin) {
                                        OutlinedTextField(
                                            value = newProdPriceInput,
                                            onValueChange = { newProdPriceInput = it },
                                            label = { Text("Precio unitario (Bs.) [ADMIN]") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        Text("Precio unitario: Bs. $newProdPriceInput", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Text("Subtotal: Bs. ${"%.2f".format(subtotalNew)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (filteredCatalog.isEmpty()) {
                                    item {
                                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                            Text("No se encontraron productos", color = MaterialTheme.colorScheme.outline)
                                        }
                                    }
                                } else {
                                    items(filteredCatalog, key = { it.id }) { cp ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedCatalogProdId = cp.id
                                                    val defP = viewModel.calculatePrice(cp, 1.0)
                                                    newProdPriceInput = defP.toString()
                                                },
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("[${cp.code}] ${cp.name}", style = MaterialTheme.typography.bodyMedium)
                                                Text("Precio: Bs. ${cp.priceNormal} | Stock: ${cp.availableStock.toInt()} ${cp.unit}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val selProd = selectedCatalogProd
                        val qty = newProdQtyInput.toIntOrNull() ?: 1
                        val price = newProdPriceInput.toDoubleOrNull()
                        if (selProd != null && qty > 0) {
                            val pr = if (isAdmin && price != null && price >= 0.0) price else viewModel.calculatePrice(selProd, qty.toDouble())
                            viewModel.addProductToDraft(selProd, qty, pr)
                            showAddProductDialog = false
                            selectedCatalogProdId = null
                            newProdQtyInput = "1"
                            newProdPriceInput = ""
                        }
                    }, enabled = selectedCatalogProd != null) { Text("Agregar al pedido") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        if (selectedCatalogProd != null) {
                            selectedCatalogProdId = null
                        } else {
                            showAddProductDialog = false
                        }
                    }) { Text(if (selectedCatalogProd != null) "Volver" else "Cancelar") }
                }
            )
        }
    }

    modifyingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { modifyingItemId = null; newQtyInput = "" },
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
                    modifyingItemId = null
                    newQtyInput = ""
                }) { Text("Actualizar") }
            },
            dismissButton = { TextButton(onClick = { modifyingItemId = null; newQtyInput = "" }) { Text("Cancelar") } }
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
            val saleDetailsMap = s.details?.associate { it.productId to it.unitPrice } ?: emptyMap()

            val products = d?.products?.map { p ->
                p.copy(unitPrice = p.unitPrice ?: saleDetailsMap[p.productId])
            } ?: s.details?.map {
                DeliveryProductDto(
                    saleDetailId = it.id,
                    productId = it.productId,
                    name = it.productName,
                    ordered = it.quantity,
                    delivered = 0,
                    pending = it.quantity,
                    unitPrice = it.unitPrice
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
                            Text("Pago: ${s.paymentStatus ?: "--"}", style = MaterialTheme.typography.bodyMedium)
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
                                    // General Devolución button removed from main screen
                                }
                            }
                        }
                        HorizontalDivider()

                        products.forEach { prod ->
                            val ord = prod.ordered ?: 0
                            val del = prod.delivered ?: 0
                            val pen = prod.pending ?: maxOf(0, ord - del)
                            val agreedPrice = prod.unitPrice ?: saleDetailsMap[prod.productId] ?: 0.0
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
                                    Text("Bs. ${"%.2f".format(agreedPrice)} c/u", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                }
                                LinearProgressIndicator(
                                    progress = progress,
                                    modifier = Modifier.fillMaxWidth().height(6.dp)
                                )
                                if (d != null && s.status != "CONFIRMED" && s.status != "CANCELLED") {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = { modifyingItemId = prod.productId }) {
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
