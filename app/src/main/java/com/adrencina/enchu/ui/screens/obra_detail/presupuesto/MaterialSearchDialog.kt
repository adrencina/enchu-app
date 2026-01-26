package com.adrencina.enchu.ui.screens.obra_detail.presupuesto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { material ->
                            MaterialResultItem(
                                material = material,
                                onClick = { selectedMaterial = material }
                            )
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "Detalles del Ítem",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Tipo Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         RadioButton(
                            selected = tipo == "MATERIAL", 
                            onClick = { tipo = "MATERIAL" },
                            modifier = Modifier.size(32.dp).padding(4.dp)
                         )
                         Text("Material", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         RadioButton(
                            selected = tipo == "MANO_DE_OBRA", 
                            onClick = { tipo = "MANO_DE_OBRA" },
                            modifier = Modifier.size(32.dp).padding(4.dp)
                         )
                         Text("Mano de Obra", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Description
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
                
                // Qty & Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) cantidad = it },
                        label = { Text("Cant.", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.width(70.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = precioUnitario,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) precioUnitario = it },
                        label = { Text("Precio ($)", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.width(110.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }

                // Actions (Footer Style)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Cancelar", style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp))
                    }
                    
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
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        enabled = descripcion.isNotBlank() && precioUnitario.isNotBlank()
                    ) {
                        Text("Agregar", style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp))
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialResultItem(
    material: MaterialEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = material.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = material.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Unidad: ${material.unit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}