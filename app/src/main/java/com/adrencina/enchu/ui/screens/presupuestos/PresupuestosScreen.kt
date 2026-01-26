package com.adrencina.enchu.ui.screens.presupuestos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.data.model.PresupuestoWithItems
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresupuestosScreen(
    viewModel: PresupuestosViewModel = hiltViewModel(),
    onNewBudgetClick: () -> Unit,
    onEditBudgetClick: (String) -> Unit,
    initialTab: Int? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(initialTab ?: 0) }
    
    LaunchedEffect(initialTab) {
        if (initialTab != null) {
            selectedTabIndex = initialTab
        }
    }

    val tabs = listOf("Borradores", "Enviados")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Mis Presupuestos") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val items = if (selectedTabIndex == 0) uiState.drafts else uiState.sent

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (items.isEmpty()) {
                EmptyState(
                    modifier = Modifier.align(Alignment.Center),
                    message = if (selectedTabIndex == 0) "No tienes borradores pendientes" else "No has enviado presupuestos aún"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = items,
                        key = { it.presupuesto.id }
                    ) { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deletePresupuesto(item.presupuesto)
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            modifier = Modifier.animateItemPlacement(),
                            backgroundContent = {
                                val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                                    MaterialTheme.colorScheme.errorContainer
                                else Color.Transparent

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            },
                            content = {
                                CompactBudgetCard(
                                    presupuesto = item,
                                    isSent = selectedTabIndex == 1,
                                    onClick = { onEditBudgetClick(item.presupuesto.id) },
                                    onCreateObra = { /* TODO: Implementar creación de Obra */ }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier, message: String) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CompactBudgetCard(
    presupuesto: PresupuestoWithItems,
    isSent: Boolean,
    onClick: () -> Unit,
    onCreateObra: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yy", Locale.getDefault()) }
    val currencyFormatter = remember {
        val cf = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        cf.maximumFractionDigits = 0
        cf
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp), // Más cuadrado/compacto
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Elevación sutil
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), // Padding interno reducido
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna Izquierda: Info Principal
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (presupuesto.presupuesto.numero > 0) {
                        Text(
                            text = "#${String.format("%04d", presupuesto.presupuesto.numero)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = presupuesto.presupuesto.titulo.ifBlank { "Sin título" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = presupuesto.presupuesto.clienteNombre ?: "Cliente desconocido",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            // Columna Derecha: Totales y Fecha
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormatter.format(presupuesto.presupuesto.total),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = dateFormatter.format(Date(presupuesto.presupuesto.creadoEn)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Botón Crear Obra (Solo en Enviados)
            if (isSent) {
                Spacer(Modifier.width(8.dp))
                VerticalDivider(Modifier.height(32.dp))
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onCreateObra) {
                    Icon(
                        Icons.Default.Build, 
                        contentDescription = "Crear Obra",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
