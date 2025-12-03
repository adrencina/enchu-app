package com.adrencina.enchu.ui.screens.obra_detail.presupuesto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adrencina.enchu.data.model.PresupuestoItem
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PresupuestoScreen(
    presupuestoItems: List<PresupuestoItem>,
    onDeleteItem: (PresupuestoItem) -> Unit
) {
    val totalMateriales = presupuestoItems.filter { it.tipo == "MATERIAL" }.sumOf { it.total }
    val totalManoObra = presupuestoItems.filter { it.tipo == "MANO_DE_OBRA" }.sumOf { it.total }
    val granTotal = totalMateriales + totalManoObra

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR")) // Ajustar locale según preferencia

    Column(modifier = Modifier.fillMaxSize()) {
        // Resumen Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Resumen del Presupuesto", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Materiales:")
                    Text(currencyFormat.format(totalMateriales))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Mano de Obra:")
                    Text(currencyFormat.format(totalManoObra))
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(currencyFormat.format(granTotal), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presupuestoItems) { item ->
                PresupuestoItemCard(item = item, currencyFormat = currencyFormat, onDelete = { onDeleteItem(item) })
            }
            item { 
                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
            }
        }
    }
}

@Composable
fun PresupuestoItemCard(
    item: PresupuestoItem,
    currencyFormat: NumberFormat,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.tipo == "MATERIAL") Icons.Default.ShoppingCart else Icons.Default.Engineering,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.descripcion, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    text = "${item.cantidad} x ${currencyFormat.format(item.precioUnitario)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = currencyFormat.format(item.total),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
