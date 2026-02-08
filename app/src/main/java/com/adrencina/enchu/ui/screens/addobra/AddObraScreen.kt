package com.adrencina.enchu.ui.screens.addobra

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.adrencina.enchu.core.utils.getContactDetails
import com.adrencina.enchu.core.utils.PickPhoneContact
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
import com.adrencina.enchu.ui.components.EnchuButton
import com.adrencina.enchu.ui.components.EnchuDialog
import com.adrencina.enchu.viewmodel.AddObraSideEffect
import com.adrencina.enchu.viewmodel.AddObraUiState
import com.adrencina.enchu.viewmodel.AddObraViewModel

import com.adrencina.enchu.ui.components.SuccessDialog

@Composable
fun AddObraScreen(
    viewModel: AddObraViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateBackWithResult: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSuccess by remember { mutableStateOf(false) }
    var pendingResult by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val contactLauncher = rememberLauncherForActivityResult(
        contract = PickPhoneContact()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val contactData = getContactDetails(context, uri)
                launch(Dispatchers.Main) {
                    if (contactData.name.isNotBlank()) viewModel.onNewClientNameChange(contactData.name)
                    if (contactData.phone.isNotBlank()) viewModel.onNewClientPhoneChange(contactData.phone)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is AddObraSideEffect.NavigateBack -> onNavigateBack()
                is AddObraSideEffect.NavigateBackWithResult -> {
                    pendingResult = effect.clientName
                    showSuccess = true
                }
            }
        }
    }

    if (showSuccess) {
        SuccessDialog(
            onDismiss = {
                showSuccess = false
                pendingResult?.let { onNavigateBackWithResult(it) }
            },
            message = "Obra creada"
        )
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
        onSaveNewClient = viewModel::onSaveNewClient,
        onPickContact = { contactLauncher.launch(Unit) }
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
    onSaveNewClient: () -> Unit,
    onPickContact: () -> Unit
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
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (isPressed) 0.90f else 1f,
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                        ),
                        label = "scale"
                    )

                    TextButton(
                        onClick = onSaveClick,
                        enabled = uiState.isSaveEnabled,
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .testTag("save_button")
                            .scale(scale)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(Dimens.ProgressIndicatorSize / 2))
                        } else {
                            Text(
                                "GUARDAR", 
                                fontWeight = FontWeight.Black,
                                color = if (uiState.isSaveEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
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
                        
                        val clientInteractionSource = remember { MutableInteractionSource() }
                        val clientIsPressed by clientInteractionSource.collectIsPressedAsState()
                        val clientScale by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (clientIsPressed) 0.9f else 1f,
                            animationSpec = androidx.compose.animation.core.spring(),
                            label = "scale"
                        )

                        IconButton(
                            onClick = onAddClientClick,
                            interactionSource = clientInteractionSource,
                            modifier = Modifier.scale(clientScale)
                        ) {
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
        EnchuDialog(
            onDismiss = onDismissDialog,
            title = "¿Descartar cambios?",
            confirmButton = {
                EnchuButton(
                    onClick = onConfirmDiscard,
                    text = "Descartar",
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            },
            dismissButton = {
                TextButton(onClick = onDismissDialog, modifier = Modifier.height(56.dp)) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            }
        ) {
            Text(
                text = "Tienes cambios sin guardar. ¿Estás seguro de que quieres salir?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (uiState.showAddClientDialog) {
        EnchuDialog(
            onDismiss = onDismissAddClientDialog,
            title = "Nuevo Cliente",
            confirmButton = {
                EnchuButton(
                    onClick = onSaveNewClient,
                    text = "Guardar",
                    enabled = uiState.newClientNameInput.isNotBlank() &&
                            (uiState.newClientDniInput.isNotBlank() || uiState.isAutoDniChecked) &&
                            !uiState.isSavingClient
                )
            },
            dismissButton = {
                TextButton(onClick = onDismissAddClientDialog, modifier = Modifier.height(56.dp)) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            }
        ) {
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
                    showExpandButton = true,
                    onPickContact = onPickContact
                )

                if (uiState.saveClientError != null) {
                    Text(
                        text = uiState.saveClientError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = Dimens.PaddingExtraSmall)
                    )
                }

                if (uiState.isSavingClient) {
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
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
            onSaveNewClient = {},
            onPickContact = {}
        )
    }
}
