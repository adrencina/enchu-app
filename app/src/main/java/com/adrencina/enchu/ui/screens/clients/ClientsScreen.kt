package com.adrencina.enchu.ui.screens.clients

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.ui.components.SearchBar
import com.adrencina.enchu.ui.components.SwipeValue
import com.adrencina.enchu.ui.components.SwipeableContactItem
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.viewmodel.ClientsViewModel
import com.adrencina.enchu.data.model.Cliente

import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlin.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClientsScreen(
    viewModel: ClientsViewModel = hiltViewModel(),
    onAddClientClick: () -> Unit,
    onClientClick: (String) -> Unit,
    onClientSelected: ((Cliente) -> Unit)? = null,
    onAddManualClientClick: (() -> Unit)? = null
) {
    val filteredClientes by viewModel.filteredClientes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current

    val isSelectionMode = onClientSelected != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.PaddingMedium)
        ) {
            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    placeholder = "Buscar cliente...",
                    modifier = Modifier.weight(1f)
                )

                if (isSelectionMode && onAddManualClientClick != null) {
                    FilledTonalIconButton(
                        onClick = onAddManualClientClick,
                        modifier = Modifier.size(48.dp), // Tamaño cómodo
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Manual / Contacto")
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall),
                contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
            ) {
                items(
                    items = filteredClientes,
                    key = { it.id } // Stable key for efficiency
                ) { cliente ->
                    
                    if (isSelectionMode) {
                        MinimalClientItem(
                            cliente = cliente,
                            onClick = { onClientSelected?.invoke(cliente) }
                        )
                    } else {
                        // AnchoredDraggableState for each item
                        val anchors = with(density) {
                            DraggableAnchors {
                                SwipeValue.Closed at 0f
                                SwipeValue.Open at -145.dp.toPx() // Increased reveal width for better spacing
                            }
                        }

                        val state = remember(cliente.id) {
                            AnchoredDraggableState(
                                initialValue = SwipeValue.Closed,
                                anchors = anchors,
                                positionalThreshold = { distance: Float -> distance * 0.3f }, // Easier to open
                                velocityThreshold = { with(density) { 100.dp.toPx() } },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                            )
                        }

                        SwipeableContactItem(
                            state = state,
                            cliente = cliente,
                            onClick = { 
                                onClientClick(cliente.id) 
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
                                        // Handle case where WA is not installed
                                    }
                                }
                            },
                            onScheduleClick = {
                                 val intent = Intent(Intent.ACTION_INSERT).apply {
                                    data = CalendarContract.Events.CONTENT_URI
                                    putExtra(CalendarContract.Events.TITLE, "Reunión con ${cliente.nombre}")
                                    if (cliente.telefono.isNotBlank()) {
                                        putExtra(CalendarContract.Events.DESCRIPTION, "Teléfono: ${cliente.telefono}")
                                    }
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinimalClientItem(
    cliente: Cliente,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cliente.nombre,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), // Suavizado
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

