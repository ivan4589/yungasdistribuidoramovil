package com.example.yungasdistribuidora.presentation.productdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.yungasdistribuidora.presentation.products.AvailabilityBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val product by viewModel.product.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading && product == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (product == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(errorMessage ?: "Producto no encontrado")
            }
        } else {
            val p = product!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Header
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    if (!p.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = p.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                // Main Info Card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = p.code, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            AvailabilityBadge(status = p.availabilityStatus)
                        }
                        Text(text = p.name, style = MaterialTheme.typography.headlineSmall)
                        if (!p.description.isNullOrBlank()) {
                            Text(text = p.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Divider()
                        DetailRow(label = "Categoría", value = p.categoryName ?: p.categoryId)
                        if (!p.subCategoryName.isNullOrBlank() || !p.subCategoryId.isNullOrBlank()) {
                            DetailRow(label = "Subcategoría", value = p.subCategoryName ?: p.subCategoryId!!)
                        }
                        DetailRow(label = "Unidad", value = p.unit)
                        if (!p.weight.isNullOrBlank()) {
                            DetailRow(label = "Peso", value = p.weight)
                        }
                    }
                }

                // Pricing Card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Precios de Venta", style = MaterialTheme.typography.titleMedium)
                        Divider()
                        DetailRow(label = "Precio Normal", value = "Bs. ${"%.2f".format(p.priceNormal)}")
                        DetailRow(label = "Precio Camino", value = "Bs. ${"%.2f".format(p.priceCamino)}")
                        DetailRow(label = "Precio Especial", value = "Bs. ${"%.2f".format(p.priceEspecial)}")
                        if (p.priceMayorista != null) {
                            DetailRow(label = "Precio Mayorista", value = "Bs. ${"%.2f".format(p.priceMayorista)} (Min. ${p.minQuantityWholesale ?: 0})")
                        }
                    }
                }

                // Stock Card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Disponibilidad en Almacén Central", style = MaterialTheme.typography.titleMedium)
                        Divider()
                        DetailRow(label = "Stock Disponible", value = "${p.availableStock.toInt()} ${p.unit}")
                        DetailRow(label = "Stock Físico Central", value = "${p.centralStock?.toInt() ?: p.stock.toInt()} ${p.unit}")
                        DetailRow(label = "Stock Reservado", value = "${p.centralReservedStock?.toInt() ?: 0} ${p.unit}")
                        DetailRow(label = "Stock Mínimo", value = "${p.minStock.toInt()} ${p.unit}")
                    }
                }

                if (!p.additionalInfo.isNullOrBlank()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Información Adicional", style = MaterialTheme.typography.titleMedium)
                            Divider()
                            Text(text = p.additionalInfo, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
