package com.adrencina.enchu.ui.screens.obra_detail.presupuesto

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.data.model.MaterialEntity
import com.adrencina.enchu.data.model.PresupuestoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialSearchDialog(
    onDismiss: () -> Unit,
    onConfirm: (PresupuestoItem) -> Unit,
    viewModel: MaterialSearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    
    var selectedMaterial by remember { mutableStateOf<MaterialEntity?>(null) }

    // If a material is selected, show the editing dialog (Step 2)
    if (selectedMaterial != null) {
        EditMaterialDetailsDialog(
            material = selectedMaterial!!,
            onDismiss = { selectedMaterial = null },
            onConfirm = { item ->
                onConfirm(item)
                onDismiss() // Close everything
            }
        )
    } else {
        // Step 1: Search
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false) // Full Screen
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search Bar
                    SmallTopAppBar(
                        title = { Text("Buscar Material") },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar")
                            }
                        },
                        colors = TopAppBarDefaults.smallTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Ej: Cable 2.5, Térmica...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )
                    
                    if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Button(onClick = {
                                // Create manual item with current query as name
                                selectedMaterial = MaterialEntity(name = searchQuery, category = "Otros", unit = "u", keywords = "")
                            }) {
                                Text("Crear '${searchQuery}' manualmente")
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(searchResults) { material ->
                            ListItem(
                                headlineContent = { Text(material.name) },
                                supportingContent = { Text("${material.category} • ${material.unit}") },
                                modifier = Modifier.clickable { selectedMaterial = material }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditMaterialDetailsDialog(
    material: MaterialEntity,
    onDismiss: () -> Unit,
    onConfirm: (PresupuestoItem) -> Unit
) {
    var cantidad by remember { mutableStateOf("1") }
    var precioUnitario by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf(material.name) }
    var tipo by remember { mutableStateOf("MATERIAL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del Ítem") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Selector de Tipo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = tipo == "MATERIAL", onClick = { tipo = "MATERIAL" })
                    Text("Material", modifier = Modifier.padding(end = 16.dp))
                    RadioButton(selected = tipo == "MANO_DE_OBRA", onClick = { tipo = "MANO_DE_OBRA" })
                    Text("Mano de Obra")
                }

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cant.") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = precioUnitario,
                        onValueChange = { precioUnitario = it },
                        label = { Text("Precio ($)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cant = cantidad.toDoubleOrNull() ?: 1.0
                    val precio = precioUnitario.toDoubleOrNull() ?: 0.0
                    onConfirm(
                        PresupuestoItem(
                            descripcion = descripcion,
                            cantidad = cant,
                            precioUnitario = precio,
                            tipo = tipo
                        )
                    )
                },
                enabled = descripcion.isNotBlank() && precioUnitario.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
