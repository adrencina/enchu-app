package com.adrencina.enchu.ui.screens.clients

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.ui.components.SearchBar
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.viewmodel.ClientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    viewModel: ClientsViewModel = hiltViewModel(),
    onAddClientClick: () -> Unit,
    onClientClick: (String) -> Unit,
    onCreateBudgetClick: (String) -> Unit, // Nuevo callback
    onClientSelected: ((Cliente) -> Unit)? = null, // Para modo selección (si se usa desde otro lado)
    onAddManualClientClick: (() -> Unit)? = null
) {
    val filteredClientes by viewModel.filteredClientes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    
    // Si onClientSelected no es nulo, estamos en modo selección "puro" (ej. desde Wizard), 
    // pero aquí estamos rediseñando la pantalla principal de Clientes. 
    // Asumiremos que el modo "normal" es el principal.
    
    val isSelectionMode = onClientSelected != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CenterAlignedTopAppBar(
            title = { 
                Text(
                    text = if (isSelectionMode) "Seleccionar Cliente" else "Clientes",
                    fontWeight = FontWeight.SemiBold
                ) 
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Barra de Búsqueda
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    placeholder = "Buscar por nombre, teléfono...",
                    modifier = Modifier.weight(1f)
                )

                if (isSelectionMode && onAddManualClientClick != null) {
                    FilledTonalIconButton(
                        onClick = onAddManualClientClick,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Manual")
                    }
                }
            }

            if (filteredClientes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No se encontraron clientes" else "No hay clientes registrados",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp), // Espaciado limpio final
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredClientes, key = { it.id }) { cliente ->
                        ClientItem(
                            cliente = cliente,
                            onClick = {
                                if (isSelectionMode) {
                                    onClientSelected?.invoke(cliente)
                                } else {
                                    onClientClick(cliente.id)
                                }
                            },
                            onCallClick = {
                                if (cliente.telefono.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${cliente.telefono}")
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            onWhatsAppClick = {
                                if (cliente.telefono.isNotBlank()) {
                                    try {
                                        val url = "https://api.whatsapp.com/send?phone=${cliente.telefono}"
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse(url)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                            },
                            onCreateBudgetClick = {
                                onCreateBudgetClick(cliente.id)
                            },
                            showMenu = !isSelectionMode // Ocultar menú en modo selección
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Dimens.PaddingMedium),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClientItem(
    cliente: Cliente,
    onClick: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onCreateBudgetClick: () -> Unit,
    showMenu: Boolean = true
) {
    var menuExpanded by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.background),
        headlineContent = {
            Text(
                text = cliente.nombre,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        supportingContent = {
            if (cliente.telefono.isNotBlank()) {
                Text(
                    text = cliente.telefono,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Surface(
                shape = CircleShape,
                color = getAvatarColor(cliente.nombre),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = cliente.nombre.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        trailingContent = {
            if (showMenu) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Crear Presupuesto") },
                            leadingIcon = { 
                                Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary) 
                            },
                            onClick = {
                                menuExpanded = false
                                onCreateBudgetClick()
                            }
                        )
                        if (cliente.telefono.isNotBlank()) {
                            DropdownMenuItem(
                                text = { Text("WhatsApp") },
                                leadingIcon = { 
                                    Icon(Icons.Default.Message, contentDescription = null, tint = Color(0xFF25D366)) 
                                },
                                onClick = {
                                    menuExpanded = false
                                    onWhatsAppClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Llamar") },
                                leadingIcon = { 
                                    Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) 
                                },
                                onClick = {
                                    menuExpanded = false
                                    onCallClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

private fun getAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFE53935),
        Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1)
    )
    val index = kotlin.math.abs(name.hashCode()) % colors.size
    return colors[index]
}

