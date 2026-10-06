package com.example.yungasdistribuidora.presentation.presale

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.yungasdistribuidora.domain.model.Client
import com.example.yungasdistribuidora.domain.model.ClientType
import com.example.yungasdistribuidora.domain.model.PresaleItem
import com.example.yungasdistribuidora.domain.model.SaleModality

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPresaleScreen(
    viewModel: NewPresaleViewModel,
    isAdmin: Boolean,
    canCreateClient: Boolean,
    onNavigateBack: () -> Unit
) {
    val draft by viewModel.draft.collectAsState()
    val clients by viewModel.filteredClients.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val clientSearchQuery by viewModel.clientSearchQuery.collectAsState()
    val productSearchQuery by viewModel.productSearchQuery.collectAsState()
    val selectedProductForAdd by viewModel.selectedProductForAdd.collectAsState()
    val addQuantityInput by viewModel.addQuantityInput.collectAsState()
    val addPriceInput by viewModel.addPriceInput.collectAsState()
    val discountInput by viewModel.discountInput.collectAsState()
    val paymentMethodInput by viewModel.paymentMethodInput.collectAsState()
    val paymentReferenceInput by viewModel.paymentReferenceInput.collectAsState()
    val dueDateInput by viewModel.dueDateInput.collectAsState()

    val isClientFormOpen by viewModel.isClientFormOpen.collectAsState()
    val isConfirmDialogVisible by viewModel.isConfirmDialogVisible.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val registeredSale by viewModel.registeredSale.collectAsState()

    val subtotal by viewModel.subtotal.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showClientDropdown by remember { mutableStateOf(false) }
    var showProductDropdown by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<PresaleItem?>(null) }
    var paymentMethodExpanded by remember { mutableStateOf(false) }

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
                title = { Text("Nueva Preventa") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total: Bs. ${"%.2f".format(totalAmount)}", style = MaterialTheme.typography.titleMedium)
                        Text("Items: ${draft.items.size}", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = { viewModel.showConfirmDialog() },
                        enabled = draft.client != null && draft.items.isNotEmpty() && !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Registrar Preventa")
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Modality Selector
            Text("Modalidad de Venta *", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SaleModality.entries.forEach { modality ->
                    FilterChip(
                        selected = draft.modality == modality,
                        onClick = { viewModel.setModality(modality) },
                        label = { Text(modality.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Conditional fields based on Modality
            if (draft.modality == SaleModality.CONTADO) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Detalles de Contado", style = MaterialTheme.typography.titleSmall)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { paymentMethodExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Método de pago: $paymentMethodInput")
                            }
                            DropdownMenu(expanded = paymentMethodExpanded, onDismissRequest = { paymentMethodExpanded = false }) {
                                listOf("CASH", "QR", "BANK_TRANSFER").forEach { method ->
                                    DropdownMenuItem(
                                        text = { Text(method) },
                                        onClick = { viewModel.paymentMethodInput.value = method; paymentMethodExpanded = false }
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = paymentReferenceInput,
                            onValueChange = { viewModel.paymentReferenceInput.value = it },
                            label = { Text("Referencia de pago (Opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            } else if (draft.modality == SaleModality.CREDITO) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Detalles de Crédito", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(
                            value = dueDateInput,
                            onValueChange = { viewModel.dueDateInput.value = it },
                            label = { Text("Fecha de Vencimiento (YYYY-MM-DD) *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Ej: 2026-10-05") }
                        )
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { paymentMethodExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Método pago inicial (Opcional): $paymentMethodInput")
                            }
                            DropdownMenu(expanded = paymentMethodExpanded, onDismissRequest = { paymentMethodExpanded = false }) {
                                listOf("", "CASH", "QR", "BANK_TRANSFER").forEach { method ->
                                    DropdownMenuItem(
                                        text = { Text(method.ifBlank { "Ninguno" }) },
                                        onClick = { viewModel.paymentMethodInput.value = method; paymentMethodExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Admin Discount Input
            if (isAdmin) {
                OutlinedTextField(
                    value = discountInput,
                    onValueChange = { viewModel.discountInput.value = it },
                    label = { Text("Descuento Global (Bs.)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            // Client Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showClientDropdown = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(draft.client?.fullName ?: "Seleccionar cliente...")
                }
                if (canCreateClient) {
                    Button(onClick = { viewModel.openClientForm() }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuevo")
                    }
                }
            }

            // Client Dropdown Dialog
            if (showClientDropdown) {
                AlertDialog(
                    onDismissRequest = { showClientDropdown = false },
                    title = { Text("Seleccionar Cliente") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = clientSearchQuery,
                                onValueChange = viewModel::setClientSearchQuery,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Buscar cliente...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                singleLine = true
                            )
                            if (clients.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("No se encontró el cliente")
                                    if (canCreateClient) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(onClick = {
                                            showClientDropdown = false
                                            viewModel.openClientForm()
                                        }) {
                                            Text("+ Crear nuevo cliente")
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.height(200.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(clients, key = { it.id }) { client ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.selectClient(client)
                                                    showClientDropdown = false
                                                }
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(client.fullName, style = MaterialTheme.typography.bodyLarge)
                                                if (!client.alias.isNullOrBlank()) {
                                                    Text("Alias: ${client.alias}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showClientDropdown = false }) {
                            Text("Cerrar")
                        }
                    }
                )
            }

            // Web-like Inline Product Addition Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Agregar Producto al Pedido", style = MaterialTheme.typography.titleSmall)

                    OutlinedButton(
                        onClick = { showProductDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedProductForAdd?.let { "${it.code} — ${it.name}" } ?: "Buscar producto...")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = addQuantityInput,
                            onValueChange = viewModel::updateAddQuantity,
                            label = { Text("Cantidad") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = addPriceInput,
                            onValueChange = { viewModel.addPriceInput.value = it },
                            label = { Text("Precio unitario") },
                            modifier = Modifier.weight(1f),
                            enabled = isAdmin,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )

                        Button(
                            onClick = { viewModel.addSelectedProductToPresale() },
                            enabled = selectedProductForAdd != null
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Agregar")
                        }
                    }
                }
            }

            // Product Picker Dialog
            if (showProductDropdown) {
                AlertDialog(
                    onDismissRequest = { showProductDropdown = false },
                    title = { Text("Seleccionar Producto") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = productSearchQuery,
                                onValueChange = viewModel::setProductSearchQuery,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Buscar producto...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                singleLine = true
                            )
                            LazyColumn(
                                modifier = Modifier.height(250.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(products, key = { it.id }) { product ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectProductForAdd(product)
                                                showProductDropdown = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(product.name, style = MaterialTheme.typography.bodyLarge)
                                                Text("Código: ${product.code} | Stock: ${product.availableStock.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text("Bs. ${"%.2f".format(product.priceNormal)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showProductDropdown = false }) {
                            Text("Cerrar")
                        }
                    }
                )
            }

            // Items List Header
            Text("Productos en Preventa (${draft.items.size})", style = MaterialTheme.typography.titleMedium)

            // Items List
            if (draft.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay productos agregados en la preventa", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    draft.items.forEach { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editingItem = item }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.product.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "Bs. ${"%.2f".format(item.unitPrice)} x ${item.quantity.toInt()} = Bs. ${"%.2f".format(item.subtotal)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { editingItem = item }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = { viewModel.removeItem(item.product.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Observations
            OutlinedTextField(
                value = draft.observations,
                onValueChange = viewModel::setObservations,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Observaciones de la Preventa") },
                minLines = 2
            )
        }
    }

    // Confirmation Dialog before registering
    if (isConfirmDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.hideConfirmDialog() },
            title = { Text("¿Registrar esta preventa?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Cliente: ${draft.client?.fullName ?: "N/D"}")
                    Text("Productos: ${draft.items.size} ítems")
                    Text("Total: Bs. ${"%.2f".format(totalAmount)}")
                    Text("Modalidad: ${draft.modality.displayName}")
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.registerSale() }) {
                    Text("Registrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideConfirmDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Success Result Dialog
    registeredSale?.let { sale ->
        AlertDialog(
            onDismissRequest = { viewModel.resetRegisteredSale(); onNavigateBack() },
            title = { Text("Preventa registrada correctamente") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("N.º: ${sale.saleNumber}")
                    Text("Total: Bs. ${"%.2f".format(sale.total)}")
                    Text("Estado: ${sale.status}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetRegisteredSale()
                        onNavigateBack()
                    }
                ) {
                    Text("Volver al inicio")
                }
            }
        )
    }

    // Edit Item Dialog
    editingItem?.let { item ->
        var qtyText by remember { mutableStateOf(item.quantity.toInt().toString()) }
        var priceText by remember { mutableStateOf(item.unitPrice.toString()) }

        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("Editar Producto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(item.product.name, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Cantidad") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Precio Unitario (Bs.)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isAdmin,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    if (!isAdmin) {
                        Text("Solo el administrador puede editar los precios.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newQty = qtyText.toDoubleOrNull() ?: item.quantity
                        val newPrice = priceText.toDoubleOrNull() ?: item.unitPrice
                        viewModel.updateItemDetails(item.product.id, newQty, newPrice)
                        editingItem = null
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Create New Client Dialog / Form
    if (isClientFormOpen) {
        var fullName by remember { mutableStateOf("") }
        var alias by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(ClientType.NORMAL) }
        var selectedLocationId by remember { mutableStateOf(locations.firstOrNull()?.id ?: "") }
        var phone by remember { mutableStateOf("") }
        var whatsappConsent by remember { mutableStateOf(false) }
        var additionalInfo by remember { mutableStateOf("") }
        var formError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { viewModel.closeClientForm() },
            title = { Text("Crear Nuevo Cliente") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nombre Completo *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = alias,
                        onValueChange = { alias = it },
                        label = { Text("Alias / Referencia") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("Tipo de Cliente *", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ClientType.entries.forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type.name) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Text("Localidad *", style = MaterialTheme.typography.labelMedium)
                    var locExpanded by remember { mutableStateOf(false) }
                    val locName = locations.find { it.id == selectedLocationId }?.name ?: "Seleccione localidad"
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { locExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(locName)
                        }
                        DropdownMenu(expanded = locExpanded, onDismissRequest = { locExpanded = false }) {
                            locations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc.name) },
                                    onClick = { selectedLocationId = loc.id; locExpanded = false }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Consentimiento WhatsApp", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = whatsappConsent, onCheckedChange = { whatsappConsent = it })
                    }

                    OutlinedTextField(
                        value = additionalInfo,
                        onValueChange = { additionalInfo = it },
                        label = { Text("Información Adicional") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    formError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fullName.isBlank()) {
                            formError = "El nombre completo es obligatorio"
                            return@Button
                        }
                        if (selectedLocationId.isBlank()) {
                            formError = "Debe seleccionar una localidad"
                            return@Button
                        }
                        viewModel.createNewClient(
                            fullName = fullName.trim(),
                            alias = alias.takeIf { it.isNotBlank() },
                            type = selectedType,
                            locationId = selectedLocationId,
                            phone = phone.takeIf { it.isNotBlank() },
                            whatsappConsent = whatsappConsent,
                            additionalInfo = additionalInfo.takeIf { it.isNotBlank() }
                        ) {
                            // success
                        }
                    }
                ) {
                    Text("Guardar y Seleccionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeClientForm() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
