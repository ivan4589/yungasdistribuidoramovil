package com.example.yungasdistribuidora.presentation.presale

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.yungasdistribuidora.domain.model.Client
import com.example.yungasdistribuidora.domain.model.ClientType
import com.example.yungasdistribuidora.domain.model.PresaleItem
import com.example.yungasdistribuidora.domain.model.Product
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
                title = { Text("Nueva Preventa", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF103B56))
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                color = Color(0xFF103B56),
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
                        Text("Total Bs. ${"%.2f".format(totalAmount)}", style = MaterialTheme.typography.titleMedium, color = Color(0xFF35D5B1))
                        Text("Items: ${draft.items.size}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA8B8C5))
                    }
                    Button(
                        onClick = { viewModel.showConfirmDialog() },
                        enabled = draft.client != null && draft.items.isNotEmpty() && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF35D5B1), contentColor = Color(0xFF071E30))
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF071E30))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Registrar Preventa", style = MaterialTheme.typography.labelLarge)
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
                .background(Color(0xFF071E30))
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Datos de Venta
            Text("1. Datos de Venta", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF103B56)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Modalidad de Venta", style = MaterialTheme.typography.labelMedium, color = Color(0xFFA8B8C5))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SaleModality.entries.forEach { modality ->
                            FilterChip(
                                selected = draft.modality == modality,
                                onClick = { viewModel.setModality(modality) },
                                label = { Text(modality.displayName) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF087C69),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF071E30),
                                    labelColor = Color(0xFFA8B8C5)
                                )
                            )
                        }
                    }

                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { viewModel.discountInput.value = it },
                            label = { Text("Descuento Global (Bs.)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF35D5B1),
                                unfocusedBorderColor = Color(0xFFA8B8C5),
                                focusedLabelColor = Color(0xFF35D5B1),
                                unfocusedLabelColor = Color(0xFFA8B8C5),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    if (draft.modality == SaleModality.CONTADO) {
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
                    } else if (draft.modality == SaleModality.CREDITO) {
                        OutlinedTextField(
                            value = dueDateInput,
                            onValueChange = { viewModel.dueDateInput.value = it },
                            label = { Text("Fecha de Vencimiento (YYYY-MM-DD) *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Ej: 2026-10-05") }
                        )
                    }
                }
            }

            // 2. Cliente (Search bar instead of button)
            Text("2. Cliente", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF103B56)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = clientSearchQuery,
                            onValueChange = viewModel::setClientSearchQuery,
                            label = { Text("Buscar o seleccionar cliente...") },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFA8B8C5)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF35D5B1),
                                unfocusedBorderColor = Color(0xFFA8B8C5),
                                focusedLabelColor = Color(0xFF35D5B1),
                                unfocusedLabelColor = Color(0xFFA8B8C5),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        if (canCreateClient) {
                            Button(
                                onClick = { viewModel.openClientForm() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF35D5B1), contentColor = Color(0xFF071E30))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Nuevo")
                            }
                        }
                    }

                    if (draft.client != null) {
                        Surface(
                            color = Color(0xFF087C69),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Seleccionado: ${draft.client?.fullName}", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                    if (!draft.client?.alias.isNullOrBlank()) {
                                        Text("Alias: ${draft.client?.alias}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA8B8C5))
                                    }
                                }
                                TextButton(onClick = { viewModel.selectClient(null) }) {
                                    Text("Cambiar", color = Color(0xFF35D5B1))
                                }
                            }
                        }
                    } else if (clientSearchQuery.isNotBlank() || clients.isNotEmpty()) {
                        // Inline suggestions list
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            clients.forEach { client ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectClient(client)
                                            viewModel.setClientSearchQuery("")
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF071E30))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(client.fullName, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                        if (!client.alias.isNullOrBlank()) {
                                            Text("Alias: ${client.alias}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA8B8C5))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Productos (Search bar and inline product cards)
            Text("3. Productos", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF103B56)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = productSearchQuery,
                        onValueChange = viewModel::setProductSearchQuery,
                        label = { Text("Buscar producto para agregar...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFA8B8C5)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF35D5B1),
                            unfocusedBorderColor = Color(0xFFA8B8C5),
                            focusedLabelColor = Color(0xFF35D5B1),
                            unfocusedLabelColor = Color(0xFFA8B8C5),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    if (products.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(products, key = { it.id }) { product ->
                                InlineProductCard(
                                    product = product,
                                    isAdmin = isAdmin,
                                    onAdd = { qty, price ->
                                        viewModel.selectProductForAdd(product)
                                        viewModel.updateAddQuantity(qty.toString())
                                        viewModel.addSelectedProductToPresale()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Products in Presale List
            Text("Productos en Preventa (${draft.items.size})", style = MaterialTheme.typography.titleMedium, color = Color.White)
            if (draft.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    Text("No hay productos agregados", color = Color(0xFFA8B8C5))
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    draft.items.forEach { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF103B56)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.product.name, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                    Text("Bs. ${"%.2f".format(item.unitPrice)} x ${item.quantity} = Bs. ${"%.2f".format(item.subtotal)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF35D5B1))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { editingItem = item }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF35D5B1))
                                    }
                                    IconButton(onClick = { viewModel.removeItem(item.product.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF5350))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Resumen
            Text("Resumen", style = MaterialTheme.typography.titleMedium, color = Color.White)
            OutlinedTextField(
                value = draft.observations,
                onValueChange = viewModel::setObservations,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Observaciones de la Preventa") },
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF35D5B1),
                    unfocusedBorderColor = Color(0xFFA8B8C5),
                    focusedLabelColor = Color(0xFF35D5B1),
                    unfocusedLabelColor = Color(0xFFA8B8C5),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
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
        var qtyText by remember { mutableStateOf(item.quantity.toString()) }
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newQty = qtyText.toIntOrNull() ?: item.quantity
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

@Composable
fun InlineProductCard(
    product: Product,
    isAdmin: Boolean,
    onAdd: (Int, Double) -> Unit
) {
    var qtyStr by remember { mutableStateOf("1") }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF071E30)),
        modifier = Modifier
            .width(180.dp)
            .height(210.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Color(0xFF103B56)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFFA8B8C5))
                    }
                }
                Text(product.name, style = MaterialTheme.typography.bodySmall, color = Color.White, maxLines = 2)
                Text("Código: ${product.code}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA8B8C5))
                Text("Stock: ${product.availableStock.toInt()}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF35D5B1))
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Precio unitario", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA8B8C5))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Button(
                        onClick = {
                            val q = qtyStr.toIntOrNull() ?: 1
                            onAdd(q, product.priceNormal)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF35D5B1), contentColor = Color(0xFF071E30)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Añadir", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
