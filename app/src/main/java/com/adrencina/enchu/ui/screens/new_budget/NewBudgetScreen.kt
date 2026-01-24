package com.adrencina.enchu.ui.screens.new_budget

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items // Añado este por si acaso
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.core.utils.PickPhoneContact
import com.adrencina.enchu.core.utils.getContactDetails
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.data.model.PresupuestoItemEntity
import com.adrencina.enchu.ui.screens.clients.ClientsScreen
import com.adrencina.enchu.ui.screens.obra_detail.presupuesto.MaterialSearchDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBudgetScreen(
    viewModel: NewBudgetViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMaterialSearch by remember { mutableStateOf(false) }
    var showManualClientDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (uiState.currentStep) {
                                1 -> "Seleccionar Cliente"
                                2 -> "Datos Generales"
                                else -> "Carga de Materiales"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Paso ${uiState.currentStep} de 3",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep > 1) {
                            viewModel.previousStep()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = if (uiState.currentStep == 1) Icons.Default.Close else Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.currentStep == 3) {
                BudgetTotalsFooter(
                    subtotal = uiState.subtotal,
                    total = uiState.total,
                    onSaveDraft = {
                        viewModel.saveDraft()
                        onNavigateBack()
                    },
                    onApprove = {
                        viewModel.saveDraft()
                        onNavigateBack()
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AnimatedContent(
                targetState = uiState.currentStep,
                label = "WizardStepTransition"
            ) { step ->
                when (step) {
                    1 -> {
                        ClientsScreen(
                            onAddClientClick = { /* No usado en modo selección si onAddManualClientClick está presente */ },
                            onClientClick = { /* No se usa en modo selección */ },
                            onClientSelected = { cliente ->
                                viewModel.onClientSelected(cliente)
                            },
                            onAddManualClientClick = {
                                showManualClientDialog = true
                            }
                        )
                    }
                    2 -> {
                        BudgetInfoStep(
                            title = uiState.budgetTitle,
                            onTitleChange = viewModel::onTitleChanged,
                            clientName = "${uiState.selectedClient?.nombre ?: ""}",
                            onNext = { viewModel.nextStep() }
                        )
                    }
                    3 -> {
                        BudgetItemsStep(
                            items = uiState.items,
                            onAddItemClick = { showMaterialSearch = true },
                            onRemoveItem = viewModel::removeItem,
                            onUpdateItem = viewModel::updateItem
                        )
                    }
                }
            }
        }

        if (showMaterialSearch) {
            MaterialSearchDialog(
                onDismiss = { showMaterialSearch = false },
                onConfirm = { item ->
                    viewModel.addItemFromMaterial(item)
                }
            )
        }

        if (showManualClientDialog) {
            ManualClientDialog(
                onDismiss = { showManualClientDialog = false },
                onConfirm = { cliente ->
                    viewModel.onClientSelected(cliente)
                    showManualClientDialog = false
                }
            )
        }
    }
}

@Composable
fun ManualClientDialog(
    onDismiss: () -> Unit,
    onConfirm: (Cliente) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val contactLauncher = rememberLauncherForActivityResult(
        contract = PickPhoneContact()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val contactData = getContactDetails(context, uri)
                launch(Dispatchers.Main) {
                    if (contactData.name.isNotBlank()) name = contactData.name
                    if (contactData.phone.isNotBlank()) phone = contactData.phone
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cliente Manual / Contacto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { contactLauncher.launch(Unit) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContactPhone, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Importar de Contactos")
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (Requerido)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tempClient = Cliente(
                        id = UUID.randomUUID().toString(),
                        nombre = name.trim(),
                        direccion = address.trim(),
                        telefono = phone.trim(),
                        organizationId = "" // Temporal
                    )
                    onConfirm(tempClient)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Usar este Cliente")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun BudgetInfoStep(
    title: String,
    onTitleChange: (String) -> Unit,
    clientName: String,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = clientName,
            onValueChange = {},
            label = { Text("Cliente") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Título del Presupuesto") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank()
        ) {
            Text("Siguiente: Cargar Materiales")
        }
    }
}

@Composable
fun BudgetItemsStep(
    items: List<PresupuestoItemEntity>,
    onAddItemClick: () -> Unit,
    onRemoveItem: (Int) -> Unit,
    onUpdateItem: (Int, Double, Double) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(items) { index, item ->
                BudgetItemCard(
                    item = item,
                    onRemove = { onRemoveItem(index) }
                )
            }
            
            item {
                OutlinedButton(
                    onClick = onAddItemClick,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Añadir Material o Mano de Obra")
                }
            }
        }
    }
}

@Composable
fun BudgetItemCard(
    item: PresupuestoItemEntity,
    onRemove: () -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "AR")) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.descripcion, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    text = "${item.cantidad} x ${currencyFormatter.format(item.precioUnitario)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = currencyFormatter.format(item.cantidad * item.precioUnitario),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun BudgetTotalsFooter(
    subtotal: Double,
    total: Double,
    onSaveDraft: () -> Unit,
    onApprove: () -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "AR")) }

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal:", style = MaterialTheme.typography.bodyLarge)
                Text(currencyFormatter.format(subtotal), style = MaterialTheme.typography.bodyLarge)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TOTAL:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    currencyFormatter.format(total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onSaveDraft,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar Borrador")
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar y Aprobar")
                }
            }
        }
    }
}