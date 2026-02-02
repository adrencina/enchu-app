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
import com.adrencina.enchu.domain.model.Movimiento
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
        // --- Header Minimalista ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SALDO DISPONIBLE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = currencyFormat.format(saldo),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (saldo >= 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.error
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Row de Ingresos/Gastos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowUpward, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(currencyFormat.format(totalIngresos), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }
                
                Spacer(Modifier.width(24.dp))
                VerticalDivider(Modifier.height(16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.width(24.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowDownward, null, tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(currencyFormat.format(totalEgresos), style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

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
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (tipo == "INGRESO") "Registrar Cobro" else "Registrar Gasto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Selector de Tipo
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = tipo == "INGRESO",
                        onClick = { tipo = "INGRESO"; categoria = "PAGO_CLIENTE" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("Cobro") }
                    SegmentedButton(
                        selected = tipo == "EGRESO",
                        onClick = { tipo = "EGRESO"; categoria = "MATERIALES" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("Gasto") }
                }

                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it },
                    label = { Text("Monto ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (ej: Adelanto, Compra cables...)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Categoría:", style = MaterialTheme.typography.labelSmall)
                val categorias = if (tipo == "INGRESO") listOf("PAGO_CLIENTE", "ADELANTO", "OTROS") 
                                 else listOf("MATERIALES", "HERRAMIENTAS", "VIATICOS", "MANO_OBRA_EXTRA", "OTROS")
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categorias.take(3).forEach { cat ->
                        FilterChip(
                            selected = categoria == cat,
                            onClick = { categoria = cat },
                            label = { Text(cat, fontSize = 9.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = monto.toDoubleOrNull() ?: 0.0
                    if (m > 0 && descripcion.isNotBlank()) {
                        onConfirm(Movimiento(descripcion = descripcion, monto = m, tipo = tipo, categoria = categoria))
                    }
                },
                enabled = monto.isNotBlank() && descripcion.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
