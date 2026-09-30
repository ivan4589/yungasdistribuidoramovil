package com.example.yungasdistribuidora.presentation.clients.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.yungasdistribuidora.domain.model.ClientType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormScreen(
    viewModel: ClientFormViewModel,
    isEditing: Boolean,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val fullName by viewModel.fullName.collectAsState()
    val alias by viewModel.alias.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedLocationId by viewModel.selectedLocationId.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val whatsappConsent by viewModel.whatsappConsent.collectAsState()
    val additionalInfo by viewModel.additionalInfo.collectAsState()

    val locations by viewModel.locations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onSaveSuccess()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Cliente" else "Nuevo Cliente") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { viewModel.fullName.value = it },
                    label = { Text("Nombre Completo *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("${fullName.length}/160") }
                )

                OutlinedTextField(
                    value = alias,
                    onValueChange = { viewModel.alias.value = it },
                    label = { Text("Alias / Referencia") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("${alias.length}/100") }
                )

                // Type Selection
                Text("Tipo de Cliente *", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClientType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { viewModel.selectedType.value = type },
                            label = { Text(type.name) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Location Selection
                Text("Localidad *", style = MaterialTheme.typography.labelMedium)
                var locationExpanded by remember { mutableStateOf(false) }
                val selectedLocName = locations.find { it.id == selectedLocationId }?.name ?: "Seleccione localidad"

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { locationExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedLocName)
                    }
                    DropdownMenu(
                        expanded = locationExpanded,
                        onDismissRequest = { locationExpanded = false }
                    ) {
                        locations.forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc.name) },
                                onClick = {
                                    viewModel.selectedLocationId.value = loc.id
                                    locationExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { viewModel.phone.value = it },
                    label = { Text("Teléfono / Celular") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("${phone.length}/30") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Consentimiento para WhatsApp", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = whatsappConsent,
                        onCheckedChange = { viewModel.whatsappConsent.value = it }
                    )
                }

                OutlinedTextField(
                    value = additionalInfo,
                    onValueChange = { viewModel.additionalInfo.value = it },
                    label = { Text("Información Adicional (Dirección, Referencia)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    supportingText = { Text("${additionalInfo.length}/500") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.saveClient() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(if (isEditing) "Guardar Cambios" else "Crear Cliente")
                    }
                }
            }
        }
    }
}
