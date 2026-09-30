package com.example.yungasdistribuidora.presentation.clients.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
fun ClientDetailScreen(
    viewModel: ClientDetailViewModel,
    isAdmin: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val client by viewModel.client.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    val context = LocalContext.current
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showReactivateDialog by remember { mutableStateOf(false) }
    var reasonText by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage, actionMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Cliente") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (isAdmin && client != null) {
                        IconButton(onClick = { onNavigateToEdit(client!!.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (isLoading && client == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (client == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Cliente no encontrado")
            }
        } else {
            val c = client!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = c.fullName, style = MaterialTheme.typography.headlineSmall)
                        if (!c.alias.isNullOrBlank()) {
                            Text(text = "Alias: ${c.alias}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Divider()
                        DetailRow(label = "Tipo", value = c.type.name)
                        DetailRow(label = "Localidad", value = c.locationName ?: c.locationId)
                        DetailRow(label = "Teléfono", value = c.phone ?: "Sin teléfono")
                        DetailRow(label = "Consentimiento WhatsApp", value = if (c.whatsappConsent) "Sí" else "No")
                        DetailRow(label = "Información Adicional", value = c.additionalInfo.takeIf { !it.isNullOrBlank() } ?: "Ninguna")
                        DetailRow(label = "Estado", value = if (c.isActive) "Activo" else "Inactivo")
                        if (!c.isActive && !c.deletedAt.isNullOrBlank()) {
                            DetailRow(label = "Fecha de baja", value = c.deletedAt)
                        }
                    }
                }

                // Phone quick actions
                if (!c.phone.isNullOrBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${c.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Llamar")
                        }
                        Button(
                            onClick = {
                                val cleanPhone = c.phone.replace(Regex("[^\\d+]"), "")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp")
                        }
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Teléfono", c.phone)
                                clipboard.setPrimaryClip(clip)
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Copiar")
                        }
                    }
                }

                // Admin Deactivate / Reactivate actions
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (c.isActive) {
                        Button(
                            onClick = {
                                reasonText = ""
                                showDeactivateDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Desactivar cliente")
                        }
                    } else {
                        Button(
                            onClick = {
                                reasonText = ""
                                showReactivateDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reactivar cliente")
                        }
                    }
                }
            }
        }
    }

    // Deactivate Dialog
    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = { Text("Desactivar cliente") },
            text = {
                Column {
                    Text("El cliente dejará de estar disponible para nuevas ventas, pero su historial se conservará.\n\nMotivo (mínimo 10 caracteres):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ingrese el motivo...") },
                        isError = reasonText.length in 1..9
                    )
                    if (reasonText.length in 1..9) {
                        Text("El motivo debe tener al menos 10 caracteres", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (reasonText.length >= 10) {
                            showDeactivateDialog = false
                            viewModel.deactivateClient(reasonText) { }
                        }
                    },
                    enabled = reasonText.length >= 10
                ) {
                    Text("Desactivar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Reactivate Dialog
    if (showReactivateDialog) {
        AlertDialog(
            onDismissRequest = { showReactivateDialog = false },
            title = { Text("Reactivar cliente") },
            text = {
                Column {
                    Text("El cliente volverá a estar disponible para nuevas operaciones.\n\nMotivo (mínimo 10 caracteres):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ingrese el motivo...") },
                        isError = reasonText.length in 1..9
                    )
                    if (reasonText.length in 1..9) {
                        Text("El motivo debe tener al menos 10 caracteres", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (reasonText.length >= 10) {
                            showReactivateDialog = false
                            viewModel.reactivateClient(reasonText) { }
                        }
                    },
                    enabled = reasonText.length >= 10
                ) {
                    Text("Reactivar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReactivateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
