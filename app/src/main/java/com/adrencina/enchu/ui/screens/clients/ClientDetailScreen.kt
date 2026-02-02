package com.adrencina.enchu.ui.screens.clients

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.domain.model.Obra
import com.adrencina.enchu.ui.components.FormSection
import com.adrencina.enchu.ui.components.ObraCard
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.viewmodel.ClientDetailViewModel
import androidx.compose.material.icons.filled.Edit

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    viewModel: ClientDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onObraClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    if (uiState.showEditDialog && uiState.cliente != null) {
        EditClientDialog(
            cliente = uiState.cliente!!,
            onDismiss = viewModel::onDismissEditDialog,
            onConfirm = viewModel::onConfirmEdit
        )
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteDialog,
            title = { Text("Eliminar Cliente") },
            text = { Text("¿Estás seguro de que quieres eliminar a este cliente? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onConfirmDelete(onSuccess = onNavigateBack) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteDialog) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Cliente") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                     IconButton(onClick = viewModel::onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Cliente")
                    }
                     IconButton(onClick = viewModel::onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar Cliente", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else if (uiState.cliente != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = Dimens.PaddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
            ) {
                // Header Info
                item {
                    ClientHeader(
                        cliente = uiState.cliente!!,
                        onCall = {
                            if (uiState.cliente!!.telefono.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${uiState.cliente!!.telefono}")
                                }
                                context.startActivity(intent)
                            }
                        },
                        onEmail = {
                            if (uiState.cliente!!.email.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${uiState.cliente!!.email}")
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }

                // Obras Section
                item {
                    Text(
                        text = "Obras Relacionadas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = Dimens.PaddingSmall)
                    )
                }

                if (uiState.obras.isEmpty()) {
                    item {
                        Text(
                            text = "Este cliente no tiene obras registradas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = Dimens.PaddingMedium)
                        )
                    }
                } else {
                    items(uiState.obras) { obra ->
                        ObraCard(obra = obra, onClick = { onObraClick(obra.id) })
                    }
                }
                
                item { Spacer(Modifier.height(Dimens.PaddingLarge)) }
            }
        }
    }
}

@Composable
fun ClientHeader(
    cliente: Cliente,
    onCall: () -> Unit,
    onEmail: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
        ) {
            Text(
                text = cliente.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            if (cliente.dni.isNotBlank()) {
                DetailRow(label = "ID/DNI", value = cliente.dni)
            }
            
            if (cliente.direccion.isNotBlank()) {
                 Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.LocationOn, 
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = cliente.direccion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = Dimens.PaddingSmall),
                horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
            ) {
                if (cliente.telefono.isNotBlank()) {
                    ActionButton(
                        icon = Icons.Default.Phone,
                        text = "Llamar",
                        onClick = onCall,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (cliente.email.isNotBlank()) {
                    ActionButton(
                        icon = Icons.Default.Email,
                        text = "Email",
                        onClick = onEmail,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text = text)
    }
}
