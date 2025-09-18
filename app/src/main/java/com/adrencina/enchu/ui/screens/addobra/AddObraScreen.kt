package com.adrencina.enchu.ui.screens.addobra

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.core.resources.AppStrings
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.ui.components.AppClientSelector
import com.adrencina.enchu.ui.components.AppTextField
import com.adrencina.enchu.ui.components.EstadoObraChips
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
        onConfirmDiscard = viewModel::onConfirmDiscard
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
    onConfirmDiscard: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppStrings.createObraTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSaveClick,
                        enabled = uiState.isSaveEnabled
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(Dimens.ProgressIndicatorSize / 2))
                        } else {
                            Text(AppStrings.save)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
        ) {
            item { Spacer(Modifier.height(Dimens.PaddingSmall)) }
            item {
                AppTextField(
                    value = uiState.nombreObra,
                    onValueChange = onNombreChange,
                    label = AppStrings.obraNameLabel,
                    placeholder = AppStrings.obraNamePlaceholder
                )
            }
            item {
                AppClientSelector(
                    clientes = uiState.clientes,
                    selectedCliente = uiState.clienteSeleccionado,
                    onClienteSelected = onClienteSelected,
                    label = AppStrings.clientLabel
                )
            }
            item {
                AppTextField(
                    value = uiState.descripcion,
                    onValueChange = onDescripcionChange,
                    label = AppStrings.descriptionLabel,
                    placeholder = AppStrings.descriptionPlaceholder,
                    singleLine = false
                )
            }
            item {
                AppTextField(
                    value = uiState.telefono,
                    onValueChange = onTelefonoChange,
                    label = AppStrings.phoneLabel,
                    placeholder = AppStrings.phonePlaceholder,
                    keyboardType = KeyboardType.Phone
                )
            }
            item {
                AppTextField(
                    value = uiState.direccion,
                    onValueChange = onDireccionChange,
                    label = AppStrings.addressLabel,
                    placeholder = AppStrings.addressPlaceholder
                )
            }
            item {
                Column {
                    Text(
                        text = AppStrings.obraStateLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Dimens.PaddingSmall))
                    EstadoObraChips(
                        selectedState = uiState.estado,
                        onStateSelected = onEstadoChange
                    )
                }
            }
            item { Spacer(Modifier.height(Dimens.PaddingSmall)) }
        }
    }

    if (uiState.showDiscardDialog) {
        DiscardChangesDialog(
            onDismiss = onDismissDialog,
            onConfirm = onConfirmDiscard
        )
    }
}

@Composable
private fun DiscardChangesDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.discardObraTitle) },
        text = { Text(AppStrings.discardObraMessage) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text(AppStrings.discard) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.cancel) }
        },
        icon = { Icon(AppIcons.Close, contentDescription = null) }
    )
}

@Preview(showBackground = true)
@Composable
private fun AddObraScreenContentPreview() {
    EnchuTheme {
        AddObraScreenContent(
            uiState = AddObraUiState(
                nombreObra = "Instalación Eléctrica",
                clientes = listOf(Cliente(id = "1", nombre = "Inversiones Alpha")),
                clienteSeleccionado = Cliente(id = "1", nombre = "Inversiones Alpha")
            ),
            onNombreChange = {},
            onClienteSelected = {},
            onDescripcionChange = {},
            onTelefonoChange = {},
            onDireccionChange = {},
            onEstadoChange = {},
            onSaveClick = {},
            onBackPress = {},
            onDismissDialog = {},
            onConfirmDiscard = {}
        )
    }
}