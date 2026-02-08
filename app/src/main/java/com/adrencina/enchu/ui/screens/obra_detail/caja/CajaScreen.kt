package com.adrencina.enchu.ui.screens.obra_detail.caja

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
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
import com.adrencina.enchu.domain.model.Movimiento
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CajaScreen(
    movimientos: List<Movimiento>,
    onDeleteMovimiento: (String) -> Unit
) {
    val totalIngresos = movimientos.filter { it.tipo == "INGRESO" }.sumOf { it.monto }
    val totalEgresos = movimientos.filter { it.tipo == "EGRESO" }.sumOf { it.monto }
    val saldo = totalIngresos - totalEgresos

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR")).apply { maximumFractionDigits = 0 }

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
                    text = "ESTADO DE CAJA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = currencyFormat.format(saldo),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (saldo >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CajaSummaryItem(
                        label = "Ingresos",
                        value = currencyFormat.format(totalIngresos),
                        color = Color(0xFF2E7D32),
                        icon = Icons.Default.ArrowUpward
                    )
                    
                    CajaSummaryItem(
                        label = "Gastos",
                        value = currencyFormat.format(totalEgresos),
                        color = Color(0xFFC62828),
                        icon = Icons.Default.ArrowDownward
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(movimientos) { mov ->
                MovimientoItemRow(
                    mov = mov, 
                    currencyFormat = currencyFormat, 
                    onDelete = { onDeleteMovimiento(mov.id) }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun CajaSummaryItem(label: String, value: String, color: Color, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
        }
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun MovimientoItemRow(
    mov: Movimiento,
    currencyFormat: NumberFormat,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }
    val isIngreso = mov.tipo == "INGRESO"
    val color = if (isIngreso) Color(0xFF2E7D32) else Color(0xFFC62828)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono simple (sin caja)
        Icon(
            imageVector = if (isIngreso) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mov.descripcion, 
                style = MaterialTheme.typography.bodyMedium, 
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = "${mov.categoria} • ${mov.fecha?.let { dateFormat.format(it) } ?: ""}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = (if (isIngreso) "+" else "-") + currencyFormat.format(mov.monto),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = "Eliminar", 
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), 
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMovimientoDialog(
    onDismiss: () -> Unit,
    onConfirm: (Movimiento) -> Unit
) {
    var descripcion by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("INGRESO") }
    var categoria by remember { mutableStateOf("OTRO") }

    EnchuDialog(
        onDismiss = onDismiss,
        title = if (tipo == "INGRESO") "Registrar Cobro" else "Registrar Gasto",
        confirmButton = {
            EnchuButton(
                onClick = {
                    val m = monto.toDoubleOrNull() ?: 0.0
                    if (m > 0 && descripcion.isNotBlank()) {
                        onConfirm(Movimiento(descripcion = descripcion, monto = m, tipo = tipo, categoria = categoria))
                    }
                },
                text = "Guardar",
                enabled = monto.isNotBlank() && descripcion.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(56.dp)) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Selector de Tipo
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = tipo == "INGRESO",
                    onClick = { tipo = "INGRESO"; categoria = "PAGO_CLIENTE" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Cobro", fontWeight = FontWeight.Bold) }
                SegmentedButton(
                    selected = tipo == "EGRESO",
                    onClick = { tipo = "EGRESO"; categoria = "MATERIALES" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("Gasto", fontWeight = FontWeight.Bold) }
            }

            AppTextField(
                value = monto,
                onValueChange = { monto = it },
                placeholder = "Monto ($)",
                keyboardType = KeyboardType.Decimal
            )

            AppTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                placeholder = "Descripción (ej: Adelanto, Compra cables...)"
            )
            
            Column {
                Text(
                    text = "CATEGORÍA", 
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                val categorias = if (tipo == "INGRESO") listOf("PAGO_CLIENTE", "ADELANTO", "OTROS") 
                                 else listOf("MATERIALES", "HERRAMIENTAS", "VIATICOS", "MANO_OBRA_EXTRA", "OTROS")
                
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categorias.take(3).forEach { cat ->
                        FilterChip(
                            selected = categoria == cat,
                            onClick = { categoria = cat },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }
    }
}
