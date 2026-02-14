package com.adrencina.enchu.ui.screens.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.core.utils.getContactDetails
import com.adrencina.enchu.core.utils.PickPhoneContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.adrencina.enchu.ui.components.ClientForm
import com.adrencina.enchu.ui.components.EnchuButton
import com.adrencina.enchu.ui.components.SuccessDialog
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
    var showSuccess by remember { mutableStateOf(false) }
    
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
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is AddClientSideEffect.ClientSaved -> {
                    showSuccess = true
                }
                is AddClientSideEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    if (showSuccess) {
        SuccessDialog(
            onDismiss = {
                showSuccess = false
                onNavigateBack()
            },
            message = "Cliente guardado"
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "NUEVO CLIENTE",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
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
                    showExpandButton = false,
                    onPickContact = { contactLauncher.launch(Unit) }
                )
            }

            item {
                Spacer(Modifier.height(32.dp))
                EnchuButton(
                    onClick = viewModel::saveClient,
                    text = "Guardar Cliente",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && uiState.name.isNotBlank()
                )
                if (uiState.isLoading) {
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
