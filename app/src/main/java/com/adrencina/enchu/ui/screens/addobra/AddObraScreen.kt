package com.adrencina.enchu.ui.screens.addobra

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
            // MODIFIED START: Usando CenterAlignedTopAppBar para que coincida con el diseño
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
            // MODIFIED END
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // MODIFIED START: Layout reconstruido con LazyColumn y el nuevo componente FormSection
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
                    AppClientSelector(
                        clientes = uiState.clientes,
                        selectedCliente = uiState.clienteSeleccionado,
                        onClienteSelected = onClienteSelected,
                        placeholder = "Ej: Juan Pérez"
                    )
                }
            }

            item {
                FormSection(title = "Descripción (opcional)") {
                    AppTextField(
                        value = uiState.descripcion,
                        onValueChange = onDescripcionChange,
                        placeholder = "Ej: cableado e instalación de portero video planta baja y primer piso.",
                        singleLine = false,
                        minLines = 3
                    )
                }
            }

            item {
                FormSection(title = "Teléfono (opcional)") {
                    AppTextField(
                        value = uiState.telefono,
                        onValueChange = onTelefonoChange,
                        placeholder = "Ej: 221 3616161",
                        keyboardType = KeyboardType.Phone
                    )
                }
            }

            item {
                FormSection(title = "Dirección (opcional)") {
                    AppTextField(
                        value = uiState.direccion,
                        onValueChange = onDireccionChange,
                        placeholder = "Ej: Cabred 1900"
                    )
                }
            }

            item {
                FormSection(title = "Estado de la obra (opcional)") {
                    EstadoObraChips(
                        selectedState = uiState.estado,
                        onStateSelected = onEstadoChange
                    )
                }
            }

            item { Spacer(Modifier.height(Dimens.PaddingLarge)) }
        }
        // MODIFIED END
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
        // MODIFIED: Icono más apropiado para una advertencia
        icon = { Icon(AppIcons.Close, contentDescription = null) }
    )
}

// ADDED START: Previews más completas
private class AddObraUiStateProvider : CollectionPreviewParameterProvider<AddObraUiState>(
    listOf(
        AddObraUiState(
            nombreObra = "Instalación Eléctrica Completa",
            clientes = listOf(Cliente(id = "1", nombre = "Constructora del Sol S.A.")),
            clienteSeleccionado = Cliente(id = "1", nombre = "Constructora del Sol S.A.")
        ),
        AddObraUiState(
            isSaving = true
        )
    )
)

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Small Device", widthDp = 320, showBackground = true)
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
            onConfirmDiscard = {}
        )
    }
}
// ADDED END