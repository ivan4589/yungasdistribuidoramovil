package com.example.yungasdistribuidora.presentation.products

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.yungasdistribuidora.domain.model.AvailabilityStatus
import com.example.yungasdistribuidora.domain.model.Product
import com.example.yungasdistribuidora.presentation.components.YungasBottomBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel,
    isAdmin: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val selectedSubCategoryId by viewModel.selectedSubCategoryId.collectAsState()
    val availabilityFilter by viewModel.availabilityFilter.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val lastSyncStr = remember(lastSyncTime) {
        lastSyncTime?.let { dateFormat.format(Date(it)) } ?: "Nunca"
    }

    var showFilters by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val filteredSubCategories = remember(subCategories, selectedCategoryId) {
        if (selectedCategoryId == null) subCategories
        else subCategories.filter { it.categoryId == selectedCategoryId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de productos (${products.size})") },
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
                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Ordenar")
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Nombre (A - Z)") },
                                onClick = { viewModel.setSortOption(ProductSortOption.NAME_AZ); sortMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Nombre (Z - A)") },
                                onClick = { viewModel.setSortOption(ProductSortOption.NAME_ZA); sortMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Menor precio") },
                                onClick = { viewModel.setSortOption(ProductSortOption.PRICE_LOW_HIGH); sortMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Mayor precio") },
                                onClick = { viewModel.setSortOption(ProductSortOption.PRICE_HIGH_LOW); sortMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Mayor disponibilidad") },
                                onClick = { viewModel.setSortOption(ProductSortOption.STOCK_HIGH_LOW); sortMenuExpanded = false }
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.refreshCatalog(isInitial = false) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar")
                    }
                }
            )
        },
        bottomBar = {
            YungasBottomBar(
                currentRoute = "products",
                isAdmin = isAdmin,
                onNavigate = onNavigate
            )
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
            // Sync & Filters toggle row
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

            // Search Bar with QR icon
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por código o nombre...", color = Color(0xFFA8B8C5)) },
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
                        Text("Disponibilidad", style = MaterialTheme.typography.labelMedium, color = Color(0xFF35D5B1))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = availabilityFilter == AvailabilityFilter.ALL,
                                onClick = { viewModel.setAvailabilityFilter(AvailabilityFilter.ALL) },
                                label = { Text("Todos") }
                            )
                            FilterChip(
                                selected = availabilityFilter == AvailabilityFilter.AVAILABLE,
                                onClick = { viewModel.setAvailabilityFilter(AvailabilityFilter.AVAILABLE) },
                                label = { Text("Disponibles") }
                            )
                            FilterChip(
                                selected = availabilityFilter == AvailabilityFilter.OUT_OF_STOCK,
                                onClick = { viewModel.setAvailabilityFilter(AvailabilityFilter.OUT_OF_STOCK) },
                                label = { Text("Agotados") }
                            )
                        }

                        // Category Dropdown
                        Text("Categoría", style = MaterialTheme.typography.labelMedium, color = Color(0xFF35D5B1))
                        var catExpanded by remember { mutableStateOf(false) }
                        val selectedCatName = categories.find { it.id == selectedCategoryId }?.name ?: "Todas las categorías"
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { catExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(selectedCatName)
                            }
                            DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Todas las categorías") },
                                    onClick = { viewModel.setSelectedCategory(null); catExpanded = false }
                                )
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = { viewModel.setSelectedCategory(cat.id); catExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Error banner if any
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
                        TextButton(onClick = { viewModel.refreshCatalog(false) }) {
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
            } else if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFFA8B8C5))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No se encontraron productos", style = MaterialTheme.typography.bodyLarge, color = Color(0xFFA8B8C5))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductItemCard(product = product, onClick = { onNavigateToDetail(product.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(product: Product, onClick: () -> Unit) {
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
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.small,
                color = Color(0xFF103B56)
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF35D5B1))
                    }
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.code,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF35D5B1)
                    )
                    AvailabilityBadge(status = product.availabilityStatus)
                }

                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bs. ${"%.2f".format(product.priceNormal)} (${product.unit})",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF35D5B1)
                    )
                    Text(
                        text = "Stock: ${product.availableStock.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA8B8C5)
                    )
                }
            }
        }
    }
}

@Composable
fun AvailabilityBadge(status: AvailabilityStatus) {
    val (text, color) = when (status) {
        AvailabilityStatus.AVAILABLE -> "Disponible" to Color(0xFF087C69)
        AvailabilityStatus.LOW_STOCK -> "Poco stock" to Color(0xFFF5B942)
        AvailabilityStatus.OUT_OF_STOCK -> "Agotado" to Color(0xFFEF5350)
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
