package com.adrencina.enchu.ui.screens.obra_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.domain.model.EstadoObra

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditObraDialog(
    uiState: ObraDetailUiState.Success,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onEstadoChanged: (String) -> Unit,
    onTelefonoChanged: (String) -> Unit,
    onDireccionChanged: (String) -> Unit,
    onClienteChanged: (Cliente) -> Unit,
    onToggleExpand: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Obra") },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                // Selector de Cliente
                var clientExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = clientExpanded,
                    onExpandedChange = { clientExpanded = !clientExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = uiState.editedCliente?.nombre ?: "Sin cliente asignado",
                        onValueChange = {},
                        label = { Text("Cliente") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = clientExpanded,
                        onDismissRequest = { clientExpanded = false }
                    ) {
                        uiState.allClientes.forEach { cliente ->
                            DropdownMenuItem(
                                text = { Text(cliente.nombre) },
                                onClick = {
                                    onClienteChanged(cliente)
                                    clientExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

                TextField(
                    value = uiState.editedObraName,
                    onValueChange = onNameChanged,
                    label = { Text("Nombre de la Obra") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
                TextField(
                    value = uiState.editedObraDescription,
                    onValueChange = onDescriptionChanged,
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
                Text("Estado")
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall),
                    verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
                ) {
                    val estados = listOf(
                        EstadoObra.PRESUPUESTADO, 
                        EstadoObra.EN_PROGRESO, 
                        EstadoObra.EN_PAUSA, 
                        EstadoObra.FINALIZADO
                    )
                    estados.forEach { estado ->
                        FilterChip(
                            selected = uiState.editedObraEstado == estado,
                            onClick = { onEstadoChanged(estado.value) },
                            label = { Text(estado.value) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

                // Sección expandible
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Datos Adicionales", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (uiState.isEditDialogExpanded) AppIcons.ExpandLess else AppIcons.ExpandMore,
                            contentDescription = if (uiState.isEditDialogExpanded) "Contraer" else "Expandir"
                        )
                    }
                }

                AnimatedVisibility(visible = uiState.isEditDialogExpanded) {
                    Column {
                        Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
                        TextField(
                            value = uiState.editedTelefono,
                            onValueChange = onTelefonoChanged,
                            label = { Text("Teléfono") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
                        TextField(
                            value = uiState.editedDireccion,
                            onValueChange = onDireccionChanged,
                            label = { Text("Dirección") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    )
}
