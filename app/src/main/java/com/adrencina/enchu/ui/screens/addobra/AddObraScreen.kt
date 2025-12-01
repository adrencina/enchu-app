package com.adrencina.enchu.ui.screens.addobra

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.core.resources.AppStrings
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.ui.components.AppClientSelector
import com.adrencina.enchu.ui.components.AppTextField
import com.adrencina.enchu.ui.components.ClientForm
import com.adrencina.enchu.ui.components.EstadoObraChips
import com.adrencina.enchu.ui.components.FormSection
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.ui.theme.EnchuTheme
import com.adrencina.enchu.viewmodel.AddObraSideEffect
import com.adrencina.enchu.viewmodel.AddObraUiState
import com.adrencina.enchu.viewmodel.AddObraViewModel

@Composable
fun AddObraScreen(
    viewModel: AddObraViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateBackWithResult: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is AddObraSideEffect.NavigateBack -> onNavigateBack()
                is AddObraSideEffect.NavigateBackWithResult -> onNavigateBackWithResult(effect.clientName)
            }
        }
    }

    BackHandler {
        viewModel.onBackPress()
    }

    AddObraScreenContent(
        uiState = uiState,
        onNombreChange = viewModel::onNombreChange,
        onClienteSelected = viewModel::onClienteSelected,
        onDescripcionChange = viewModel::onDescripcionChange,
        onTelefonoChange = viewModel::onTelefonoChange,
        onDireccionChange = viewModel::onDireccionChange,
        onEstadoChange = viewModel::onEstadoChange,
        onSaveClick = viewModel::onSaveClick,
        onBackPress = viewModel::onBackPress,
        onDismissDialog = viewModel::onDismissDialog,
        onConfirmDiscard = viewModel::onConfirmDiscard,
        // Add Client Dialog actions
        onAddClientClick = viewModel::onAddClientClick,
        onDismissAddClientDialog = viewModel::onDismissAddClientDialog,
        onNewClientNameChange = viewModel::onNewClientNameChange,
        onNewClientDniChange = viewModel::onNewClientDniChange,
        onNewClientPhoneChange = viewModel::onNewClientPhoneChange,
        onNewClientEmailChange = viewModel::onNewClientEmailChange,
        onNewClientAddressChange = viewModel::onNewClientAddressChange,
        onToggleClientFormExpand = viewModel::onToggleClientFormExpand,
        onAutoDniCheckedChange = viewModel::onAutoDniCheckedChange,
        onSaveNewClient = viewModel::onSaveNewClient
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddObraScreenContent(
    uiState: AddObraUiState,
    onNombreChange: (String) -> Unit,
    onClienteSelected: (Cliente) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onDireccionChange: (String) -> Unit,
    onEstadoChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackPress: () -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmDiscard: () -> Unit,
    // Add Client Dialog actions
    onAddClientClick: () -> Unit,
    onDismissAddClientDialog: () -> Unit,
    onNewClientNameChange: (String) -> Unit,
    onNewClientDniChange: (String) -> Unit,
    onNewClientPhoneChange: (String) -> Unit,
    onNewClientEmailChange: (String) -> Unit,
    onNewClientAddressChange: (String) -> Unit,
    onToggleClientFormExpand: () -> Unit,
    onAutoDniCheckedChange: (Boolean) -> Unit,
    onSaveNewClient: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Crear Nueva Obra", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(AppIcons.Close, contentDescription = AppStrings.close)
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSaveClick,
                        enabled = uiState.isSaveEnabled,
                        modifier = Modifier.testTag("save_button")
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(Dimens.ProgressIndicatorSize / 2))
                        } else {
                            Text("GUARDAR", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item {
                FormSection(title = "Nombre de la obra") {
                    AppTextField(
                        value = uiState.nombreObra,
                        onValueChange = onNombreChange,
                        placeholder = "Ej: Instalacion portero"
                    )
                }
            }

            item {
                FormSection(title = "Cliente") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            AppClientSelector(
                                clientes = uiState.clientes,
                                selectedCliente = uiState.clienteSeleccionado,
                                onClienteSelected = onClienteSelected,
                                placeholder = "Ej: Juan Pérez"
                            )
                        }
                        Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                        IconButton(onClick = onAddClientClick) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Añadir nuevo cliente",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                FormSection(title = "Descripción (opcional)") {
                    AppTextField(
                        value = uiState.descripcion,
                        onValueChange = onDescripcionChange,
                        placeholder = "Detalles adicionales...",
                        minLines = 3
                    )
                }
            }

            item {
                FormSection(title = "Contacto (opcional)") {
                    AppTextField(
                        value = uiState.telefono,
                        onValueChange = onTelefonoChange,
                        placeholder = "Teléfono alternativo"
                    )
                }
            }

            item {
                FormSection(title = "Dirección de obra") {
                    AppTextField(
                        value = uiState.direccion,
                        onValueChange = onDireccionChange,
                        placeholder = "Ej: Av. Siempreviva 742"
                    )
                }
            }

            item {
                FormSection(title = "Estado inicial") {
                    EstadoObraChips(
                        selectedState = uiState.estado,
                        onStateSelected = onEstadoChange
                    )
                }
            }
            
            item { Spacer(Modifier.height(Dimens.PaddingLarge)) }
        }
    }

    if (uiState.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text(text = "¿Descartar cambios?") },
            text = { Text("Tienes cambios sin guardar. ¿Estás seguro de que quieres salir?") },
            confirmButton = {
                TextButton(onClick = onConfirmDiscard) {
                    Text("Descartar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialog) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (uiState.showAddClientDialog) {
        AlertDialog(
            onDismissRequest = onDismissAddClientDialog,
            title = { Text(text = "Nuevo Cliente") },
            text = {
                Column {
                    ClientForm(
                        name = uiState.newClientNameInput,
                        onNameChange = onNewClientNameChange,
                        dni = uiState.newClientDniInput,
                        onDniChange = onNewClientDniChange,
                        isAutoDni = uiState.isAutoDniChecked,
                        onAutoDniChange = onAutoDniCheckedChange,
                        phone = uiState.newClientPhoneInput,
                        onPhoneChange = onNewClientPhoneChange,
                        email = uiState.newClientEmailInput,
                        onEmailChange = onNewClientEmailChange,
                        address = uiState.newClientAddressInput,
                        onAddressChange = onNewClientAddressChange,
                        isExpanded = uiState.isClientFormExpanded,
                        onToggleExpand = onToggleClientFormExpand,
                        showExpandButton = true
                    )

                    if (uiState.saveClientError != null) {
                        Text(
                            text = uiState.saveClientError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = Dimens.PaddingExtraSmall)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onSaveNewClient,
                    enabled = uiState.newClientNameInput.isNotBlank() &&
                            (uiState.newClientDniInput.isNotBlank() || uiState.isAutoDniChecked) &&
                            !uiState.isSavingClient
                ) {
                    if (uiState.isSavingClient) {
                        CircularProgressIndicator(Modifier.size(Dimens.ProgressIndicatorSize / 2))
                    } else {
                        Text("Guardar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissAddClientDialog) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private class AddObraUiStateProvider : CollectionPreviewParameterProvider<AddObraUiState>(
    listOf(
        AddObraUiState(
            nombreObra = "Instalación Eléctrica Completa",
            clientes = listOf(Cliente(id = "1", nombre = "Constructora del Sol S.A.")),
            clienteSeleccionado = Cliente(id = "1", nombre = "Constructora del Sol S.A.")
        )
    )
)

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun AddObraScreenContentPreview(
    @PreviewParameter(AddObraUiStateProvider::class) uiState: AddObraUiState
) {
    EnchuTheme {
        AddObraScreenContent(
            uiState = uiState,
            onNombreChange = {},
            onClienteSelected = {},
            onDescripcionChange = {},
            onTelefonoChange = {},
            onDireccionChange = {},
            onEstadoChange = {},
            onSaveClick = {},
            onBackPress = {},
            onDismissDialog = {},
            onConfirmDiscard = {},
            onAddClientClick = {},
            onDismissAddClientDialog = {},
            onNewClientNameChange = {},
            onNewClientDniChange = {},
            onNewClientPhoneChange = {},
            onNewClientEmailChange = {},
            onNewClientAddressChange = {},
            onToggleClientFormExpand = {},
            onAutoDniCheckedChange = {},
            onSaveNewClient = {}
        )
    }
}
