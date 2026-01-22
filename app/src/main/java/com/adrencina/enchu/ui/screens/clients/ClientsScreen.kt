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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClientsScreen(
    viewModel: ClientsViewModel = hiltViewModel(),
    onAddClientClick: () -> Unit,
    onClientClick: (String) -> Unit
) {
    val filteredClientes by viewModel.filteredClientes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClientClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Cliente")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.PaddingMedium)
        ) {
            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
            
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                placeholder = "Buscar cliente..."
            )

            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall),
                contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
            ) {
                items(
                    items = filteredClientes,
                    key = { it.id } // Stable key for efficiency
                ) { cliente ->
                    
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
                            // Close menu if open, or navigate
                            if (state.currentValue == SwipeValue.Open) {
                                // Reset logic can be handled by scope.launch { state.animateTo(SwipeValue.Closed) }
                                // ideally, but for now we just keep the default behavior
                            }
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

