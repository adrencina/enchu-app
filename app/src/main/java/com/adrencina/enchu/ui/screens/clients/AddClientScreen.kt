package com.adrencina.enchu.ui.screens.clients

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.core.utils.getContactDetails
import com.adrencina.enchu.core.utils.PickPhoneContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.adrencina.enchu.ui.components.ClientForm
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.viewmodel.AddClientSideEffect
import com.adrencina.enchu.viewmodel.AddClientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClientScreen(
    viewModel: AddClientViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val contactLauncher = rememberLauncherForActivityResult(
        contract = PickPhoneContact()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val contactData = getContactDetails(context, uri)
                launch(Dispatchers.Main) {
                    if (contactData.name.isNotBlank()) viewModel.onNameChange(contactData.name)
                    if (contactData.phone.isNotBlank()) viewModel.onPhoneChange(contactData.phone)
                    // Email not available with PickPhoneContact to avoid permissions
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is AddClientSideEffect.ClientSaved -> onNavigateBack()
                is AddClientSideEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Cliente") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::saveClient,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.Check, contentDescription = "Guardar")
                }
            }
        }
    ) { paddingValues ->
        com.adrencina.enchu.ui.components.FormSection(
            title = "Datos Personales",
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = Dimens.PaddingMedium)
        ) {
             ClientForm(
                name = uiState.name,
                onNameChange = viewModel::onNameChange,
                dni = uiState.dni,
                onDniChange = viewModel::onDniChange,
                isAutoDni = uiState.isAutoDni,
                onAutoDniChange = viewModel::onAutoDniChange,
                phone = uiState.phone,
                onPhoneChange = viewModel::onPhoneChange,
                email = uiState.email,
                onEmailChange = viewModel::onEmailChange,
                address = uiState.address,
                onAddressChange = viewModel::onAddressChange,
                isExpanded = uiState.isExpanded,
                onToggleExpand = viewModel::onToggleExpand,
                showExpandButton = false, // Always expanded in full screen, or user logic?
                                        // User said: "desde la pestaña de clientes, pida todo los datos desplegados"
                onPickContact = { contactLauncher.launch(Unit) }
            )
        }
    }
}
