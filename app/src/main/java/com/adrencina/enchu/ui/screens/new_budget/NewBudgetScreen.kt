package com.adrencina.enchu.ui.screens.new_budget

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items // Añado este por si acaso
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
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

    // Intercept system back button
    BackHandler(enabled = uiState.currentStep > 1) {
        viewModel.previousStep()
    }

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
                    onAddItemClick = { showMaterialSearch = true }, // New action in footer
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
                            // onAddItemClick removed (now in footer)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetItemsStep(
    items: List<PresupuestoItemEntity>,
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
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            onRemoveItem(index)
                            true
                        } else {
                            false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) 
                            MaterialTheme.colorScheme.errorContainer 
                        else Color.Transparent
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color, MaterialTheme.shapes.small)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    },
                    content = {
                        BudgetItemCard(
                            item = item,
                            onUpdate = { q, p -> onUpdateItem(index, q, p) }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun BudgetItemCard(
    item: PresupuestoItemEntity,
    onUpdate: (Double, Double) -> Unit
) {
    // Formateador personalizado para pesos argentinos sin decimales
    val currencyFormatter = remember { 
        val format = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        format.maximumFractionDigits = 0
        format
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Description
            Text(
                text = item.descripcion,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Edit Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom, 
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Price Input (Fixed width for "$0000000")
                MinimalInput(
                    value = item.precioUnitario,
                    label = "Precio",
                    modifier = Modifier.width(90.dp), 
                    onValueChange = { onUpdate(item.cantidad, it) },
                    prefix = "$"
                )

                // Quantity Input (Fixed width for "0000")
                MinimalInput(
                    value = item.cantidad,
                    label = "Cant.",
                    modifier = Modifier.width(50.dp),
                    onValueChange = { onUpdate(it, item.precioUnitario) }
                )
                
                Spacer(modifier = Modifier.weight(1f))

                // Total Preview
                Text(
                    text = currencyFormatter.format(item.cantidad * item.precioUnitario),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
            }
        }
    }
}

@Composable
fun MinimalInput(
    value: Double,
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (Double) -> Unit,
    prefix: String? = null
) {
    var text by remember(value) { mutableStateOf(if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.US, "%.1f", value)) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            maxLines = 1
        )
        
        BasicTextField(
            value = text,
            onValueChange = { 
                val filtered = it.filter { char -> char.isDigit() || char == '.' }
                text = filtered
                val num = filtered.toDoubleOrNull()
                if (num != null) {
                    onValueChange(num)
                }
            },
            singleLine = true, // CRITICAL: Prevents line breaks
            maxLines = 1,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp, 
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (prefix != null) {
                        Text(
                            prefix, 
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun BudgetTotalsFooter(
    subtotal: Double,
    total: Double,
    onAddItemClick: () -> Unit,
    onSaveDraft: () -> Unit,
    onApprove: () -> Unit
) {
    val currencyFormatter = remember { 
        val cf = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        cf.maximumFractionDigits = 0
        cf
    }

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 16.dp, // Strong shadow to separate from content
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Add Item Button (Top, Full Width, Compact)
            OutlinedButton(
                onClick = onAddItemClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("AGREGAR MATERIAL O MANO DE OBRA", style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp))
            }

            // 2. Totals Row (Compact, Single Line)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subtotal: ${currencyFormatter.format(subtotal)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TOTAL: ",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormatter.format(total),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // 3. Action Buttons (Side by Side, Compact)
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSaveDraft,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("Guardar Borrador", style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp), maxLines = 1)
                }
                
                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("Aprobar", style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp), maxLines = 1)
                }
            }
        }
    }
}