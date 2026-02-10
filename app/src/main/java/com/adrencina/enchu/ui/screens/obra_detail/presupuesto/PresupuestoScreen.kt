package com.adrencina.enchu.ui.screens.obra_detail.presupuesto

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adrencina.enchu.domain.model.PresupuestoItem
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PresupuestoScreen(
    presupuestoItems: List<PresupuestoItem>,
    onDeleteItem: (PresupuestoItem) -> Unit,
    onUpdateLogistics: (PresupuestoItem, Boolean, Boolean, Double?) -> Unit
) {
    val totalMateriales = presupuestoItems.filter { it.tipo == "MATERIAL" }.sumOf { it.subtotal }
    val totalReal = presupuestoItems.sumOf { it.totalReal }
    val desvioTotal = presupuestoItems.sumOf { it.desvio }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR")).apply { maximumFractionDigits = 0 }

    var itemToUpdateCosto by remember { mutableStateOf<PresupuestoItem?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // --- Header Minimalista ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BALANCE DE MATERIALES",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            
            // Dato Protagonista: El Desvío/Ahorro
            Text(
                text = (if (desvioTotal > 0) "+" else "") + currencyFormat.format(desvioTotal),
                style = MaterialTheme.typography.displaySmall, // Grande pero fino
                fontWeight = FontWeight.Bold,
                color = if (desvioTotal >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Datos Secundarios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Presupuestado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(currencyFormat.format(totalMateriales), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(32.dp))
                VerticalDivider(Modifier.height(24.dp))
                Spacer(Modifier.width(32.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Costo Real", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(currencyFormat.format(totalReal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // --- Lista Plana ---
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(presupuestoItems) { item ->
                PresupuestoItemRow(
                    item = item, 
                    currencyFormat = currencyFormat, 
                    onDelete = { onDeleteItem(item) },
                    onToggleComprado = { comprado ->
                        if (comprado && item.tipo == "MATERIAL") {
                            itemToUpdateCosto = item
                        } else {
                            onUpdateLogistics(item, comprado, item.isInstalado, if (!comprado) null else item.costoReal)
                        }
                    },
                    onToggleInstalado = { instalado ->
                        onUpdateLogistics(item, item.isComprado, instalado, item.costoReal)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            }
        }
    }

    if (itemToUpdateCosto != null) {
        CostoRealDialog(
            item = itemToUpdateCosto!!,
            onDismiss = { itemToUpdateCosto = null },
            onConfirm = { costo ->
                onUpdateLogistics(itemToUpdateCosto!!, true, itemToUpdateCosto!!.isInstalado, costo)
                itemToUpdateCosto = null
            }
        )
    }
}

@Composable
fun PresupuestoItemRow(
    item: PresupuestoItem,
    currencyFormat: NumberFormat,
    onDelete: () -> Unit,
    onToggleComprado: (Boolean) -> Unit,
    onToggleInstalado: (Boolean) -> Unit
) {
    // Definimos colores sutiles
    val isDone = item.isInstalado
    val textColor = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono sutil a la izquierda
        Icon(
            imageVector = if (item.tipo == "MATERIAL") Icons.Default.ShoppingCart else Icons.Default.Engineering,
            contentDescription = null,
            tint = if (item.isComprado) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f).let { MaterialTheme.colorScheme.outline }, // Gris si no, Verde si comprado
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.descripcion, 
                    style = MaterialTheme.typography.bodyMedium, 
                    fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                
                // Precios a la derecha
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormat.format(item.subtotal),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    if (item.costoReal != null) {
                        Text(
                            text = currencyFormat.format(item.totalReal),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (item.desvio >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(4.dp))
            
            // Fila de Info y Acciones (Logística)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Cantidad y Precio unitario
                Text(
                    text = "${item.cantidad} x ${currencyFormat.format(item.precioUnitario)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.weight(1f))
                
                // Chips de Estado (Logística Minimalista)
                if (item.tipo == "MATERIAL") {
                    StatusChip(
                        label = "Comprado",
                        isActive = item.isComprado,
                        onClick = { onToggleComprado(!item.isComprado) }
                    )
                    Spacer(Modifier.width(8.dp))
                }
                
                StatusChip(
                    label = if (item.tipo == "MATERIAL") "Instalado" else "Hecho",
                    isActive = item.isInstalado,
                    onClick = { onToggleInstalado(!item.isInstalado) }
                )
            }
        }
    }
}

@Composable
fun StatusChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isActive,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        leadingIcon = if (isActive) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isActive,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            selectedBorderColor = Color.Transparent,
            borderWidth = 1.dp
        ),
        modifier = Modifier.height(28.dp) // Chip muy compacto
    )
}

@Composable
fun CostoRealDialog(
    item: PresupuestoItem,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var costoText by remember { mutableStateOf(item.precioUnitario.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Costo Real de Compra") },
        text = {
            Column {
                Text("Ingresa el precio unitario real que pagaste:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = costoText,
                    onValueChange = { costoText = it },
                    label = { Text("Precio Unitario") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val value = costoText.toDoubleOrNull() ?: item.precioUnitario
                onConfirm(value)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
