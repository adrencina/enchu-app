package com.adrencina.enchu.ui.screens.clients

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.ui.components.SearchBar
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.viewmodel.ClientsViewModel

@Composable
fun ClientsScreen(
    viewModel: ClientsViewModel = hiltViewModel(),
    onAddClientClick: () -> Unit,
    onClientClick: (String) -> Unit
) {
    val filteredClientes by viewModel.filteredClientes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
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
                items(filteredClientes) { cliente ->
                    ClientCard(cliente = cliente, onClick = { onClientClick(cliente.id) })
                }
            }
        }
    }
}

@Composable
fun ClientCard(
    cliente: Cliente,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.PaddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cliente.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (cliente.telefono.isNotBlank() || cliente.email.isNotBlank()) {
                Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                         if (cliente.telefono.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.width(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cliente.telefono,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (cliente.email.isNotBlank()) {
                             if (cliente.telefono.isNotBlank()) Spacer(modifier = Modifier.height(4.dp))
                             Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.width(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cliente.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Quick Actions
                    Row {
                        if (cliente.telefono.isNotBlank()) {
                             IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${cliente.telefono}")
                                }
                                context.startActivity(intent)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Llamar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            // WhatsApp logic could be added here too
                        }
                    }
                }
            }
        }
    }
}
