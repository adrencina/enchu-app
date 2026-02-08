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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.adrencina.enchu.ui.components.EnchuButton
import com.adrencina.enchu.ui.components.EnchuDialog
import com.adrencina.enchu.ui.components.AppTextField
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
        // --- Header Premium Card ---
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BALANCE DE MATERIALES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                
                // Dato Protagonista: El Desvío/Ahorro
                Text(
                    text = (if (desvioTotal > 0) "+" else "") + currencyFormat.format(desvioTotal),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (desvioTotal >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PresupuestoSummaryItem(
                        label = "Presupuestado",
                        value = currencyFormat.format(totalMateriales)
                    )
                    PresupuestoSummaryItem(
                        label = "Costo Real",
                        value = currencyFormat.format(totalReal)
                    )
                }
            }
        }

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
private fun PresupuestoSummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(), 
            style = MaterialTheme.typography.labelSmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun CostoRealDialog(
    item: PresupuestoItem,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var costoText by remember { mutableStateOf(item.precioUnitario.toString()) }

    EnchuDialog(
        onDismiss = onDismiss,
        title = "Costo Real de Compra",
        confirmButton = {
            EnchuButton(
                onClick = {
                    val value = costoText.toDoubleOrNull() ?: item.precioUnitario
                    onConfirm(value)
                },
                text = "Guardar"
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(56.dp)) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    ) {
        Column {
            Text(
                text = "¿Cuánto pagaste por unidad de ${item.descripcion}?", 
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            AppTextField(
                value = costoText,
                onValueChange = { costoText = it },
                placeholder = "Precio Unitario ($)",
                keyboardType = KeyboardType.Decimal
            )
        }
    }
}
