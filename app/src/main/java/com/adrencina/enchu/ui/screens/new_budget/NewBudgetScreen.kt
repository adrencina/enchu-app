package com.adrencina.enchu.ui.screens.new_budget

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// Imports para PDF Zoom
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import kotlinx.coroutines.withContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import com.adrencina.enchu.ui.components.EnchuButton
import com.adrencina.enchu.ui.components.EnchuDialog
import com.adrencina.enchu.ui.components.AppTextField

import com.adrencina.enchu.ui.components.SuccessDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBudgetScreen(
    viewModel: NewBudgetViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onBudgetSaved: (Boolean) -> Unit // true = enviado
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMaterialSearch by remember { mutableStateOf(false) }
    var showManualClientDialog by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var pendingBudgetSavedResult by remember { mutableStateOf<Boolean?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is NewBudgetUiEvent.BudgetSaved -> {
                    pendingBudgetSavedResult = event.isSent
                    showSuccess = true
                }
            }
        }
    }

    if (showSuccess) {
        SuccessDialog(
            onDismiss = {
                showSuccess = false
                pendingBudgetSavedResult?.let { onBudgetSaved(it) }
            },
            message = if (pendingBudgetSavedResult == true) "Presupuesto enviado" else "Borrador guardado"
        )
    }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val sharePdf = {
        scope.launch {
            viewModel.generatePdf(context)?.let { file ->
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir Presupuesto"))
            }
        }
    }

    BackHandler(enabled = uiState.currentStep > 1) {
        viewModel.previousStep()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (uiState.currentStep) {
                                1 -> "CLIENTE"
                                2 -> "DATOS GENERALES"
                                3 -> "MATERIALES"
                                else -> "REVISIÓN"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "PASO ${uiState.currentStep} DE 4",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
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
                },
                actions = {
                    if (uiState.currentStep == 4) {
                        IconButton(onClick = { sharePdf() }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir PDF", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (uiState.currentStep == 3) {
                BudgetTotalsFooter(
                    subtotal = uiState.subtotal,
                    total = uiState.total,
                    discountInput = uiState.discountInput,
                    isEditingSentBudget = uiState.isEditingSentBudget,
                    onDiscountChange = viewModel::onDiscountChanged,
                    onAddItemClick = { showMaterialSearch = true },
                    onSaveDraft = {
                        viewModel.saveDraft()
                    },
                    onApprove = {
                        viewModel.nextStep()
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
                            onAddClientClick = { },
                            onClientClick = { },
                            onCreateBudgetClick = { }, // Not used in wizard mode
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
                            validity = uiState.validity,
                            onValidityChange = viewModel::onValidityChanged,
                            notes = uiState.notes,
                            onNotesChange = viewModel::onNotesChanged,
                            clientName = "${uiState.selectedClient?.nombre ?: ""}",
                            onNext = { viewModel.nextStep() }
                        )
                    }
                    3 -> {
                        BudgetItemsStep(
                            items = uiState.items,
                            onRemoveItem = viewModel::removeItem,
                            onUpdateItem = viewModel::updateItem
                        )
                    }
                    4 -> {
                        BudgetPreviewStep(
                            uiState = uiState,
                            viewModel = viewModel,
                            onSaveDraft = {
                                viewModel.saveDraft()
                            },
                            onApprove = {
                                viewModel.finalizeBudget()
                            }
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

    EnchuDialog(
        onDismiss = onDismiss,
        title = "Cliente Manual",
        confirmButton = {
            EnchuButton(
                onClick = {
                    val tempClient = Cliente(
                        id = UUID.randomUUID().toString(),
                        nombre = name.trim(),
                        direccion = address.trim(),
                        telefono = phone.trim(),
                        organizationId = ""
                    )
                    onConfirm(tempClient)
                },
                text = "Usar Cliente",
                enabled = name.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(56.dp)) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Import Contact Snappy Button
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

            Surface(
                onClick = { contactLauncher.launch(Unit) },
                interactionSource = interactionSource,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth().scale(scale)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ContactPhone, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.width(12.dp))
                    Text("Importar de Contactos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            AppTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Nombre (Requerido)"
            )
            AppTextField(
                value = address,
                onValueChange = { address = it },
                placeholder = "Dirección"
            )
            AppTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = "Teléfono",
                keyboardType = KeyboardType.Phone
            )
        }
    }
}

@Composable
fun BudgetInfoStep(
    title: String,
    onTitleChange: (String) -> Unit,
    validity: String,
    onValidityChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    clientName: String,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AppTextField(
            value = clientName,
            onValueChange = {},
            placeholder = "Cliente",
            enabled = false
        )

        AppTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = "Título del Presupuesto (ej: Reforma Cocina)"
        )

        AppTextField(
            value = validity,
            onValueChange = onValidityChange,
            placeholder = "Validez de oferta (días)",
            keyboardType = KeyboardType.Number
        )

        AppTextField(
            value = notes,
            onValueChange = onNotesChange,
            placeholder = "Notas / Condiciones",
            singleLine = false,
            minLines = 4,
            modifier = Modifier.height(150.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        EnchuButton(
            onClick = onNext,
            text = "Siguiente: Cargar Materiales",
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() && validity.isNotBlank()
        )
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
    val currencyFormatter = remember { 
        val format = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        format.maximumFractionDigits = 0
        format
    }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = item.descripcion,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MinimalInput(
                    value = item.precioUnitario,
                    label = "PRECIO",
                    modifier = Modifier.weight(1.2f), 
                    onValueChange = { onUpdate(item.cantidad, it) },
                    prefix = "$"
                )

                MinimalInput(
                    value = item.cantidad,
                    label = "CANT.",
                    modifier = Modifier.weight(0.8f),
                    onValueChange = { onUpdate(it, item.precioUnitario) }
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "SUBTOTAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currencyFormatter.format(item.cantidad * item.precioUnitario),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
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
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(4.dp))
        
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
            singleLine = true,
            maxLines = 1,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (prefix != null) {
                        Text(
                            prefix, 
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), 
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(2.dp))
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
    discountInput: String,
    isEditingSentBudget: Boolean,
    onDiscountChange: (String) -> Unit,
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
        tonalElevation = 12.dp,
        shadowElevation = 24.dp,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Totals and Discount Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SUBTOTAL: ${currencyFormatter.format(subtotal)}", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DESC: ", 
                            style = MaterialTheme.typography.labelMedium, 
                            color = MaterialTheme.colorScheme.error
                        )
                        BasicTextField(
                            value = discountInput,
                            onValueChange = onDiscountChange,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    innerTextField()
                                    Text("%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TOTAL A PAGAR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = currencyFormatter.format(total),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Primary Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EnchuButton(
                    onClick = onAddItemClick,
                    text = "Añadir Material / Mano de Obra",
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    icon = Icons.Default.Add
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isEditingSentBudget) {
                        TextButton(
                            onClick = onSaveDraft,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Guardar Borrador", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    EnchuButton(
                        onClick = onApprove,
                        text = "Siguiente",
                        modifier = Modifier.weight(1.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetPreviewStep(
    uiState: NewBudgetUiState,
    viewModel: NewBudgetViewModel,
    onSaveDraft: () -> Unit,
    onApprove: () -> Unit
) {
    val context = LocalContext.current
    var pdfBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isLoadingPdf by remember { mutableStateOf(true) }
    
    // Zoom State
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(Unit) {
        isLoadingPdf = true
        try {
            val file = viewModel.generatePdf(context)
            
            withContext(Dispatchers.IO) {
                val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(fileDescriptor)
                val page = pdfRenderer.openPage(0)
                
                val density = context.resources.displayMetrics.density
                val width = (page.width * 2).toInt()
                val height = (page.height * 2).toInt()
                
                val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                canvas.drawColor(android.graphics.Color.WHITE)
                
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                
                page.close()
                pdfRenderer.close()
                fileDescriptor.close()
                
                pdfBitmap = bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoadingPdf = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Vista Previa del Documento", 
            style = MaterialTheme.typography.titleMedium, 
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Contenedor del PDF Renderizado con Zoom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.707f)
                .background(Color.LightGray, RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 3f)
                        if (scale > 1f) {
                            val maxTranslateX = (size.width * (scale - 1)) / 2
                            val maxTranslateY = (size.height * (scale - 1)) / 2
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(-maxTranslateX, maxTranslateX),
                                y = (offset.y + pan.y).coerceIn(-maxTranslateY, maxTranslateY)
                            )
                        } else {
                            offset = Offset.Zero
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingPdf) {
                CircularProgressIndicator()
            } else if (pdfBitmap != null) {
                Image(
                    bitmap = pdfBitmap!!.asImageBitmap(),
                    contentDescription = "Vista previa del PDF",
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("Error al generar vista previa", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botones Finales Premium
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isEditingSentBudget) {
                EnchuButton(
                    onClick = onApprove,
                    text = "Guardar Cambios",
                    modifier = Modifier.weight(1f)
                )
            } else {
                TextButton(
                    onClick = onSaveDraft,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Borrador", fontWeight = FontWeight.Bold)
                }

                EnchuButton(
                    onClick = onApprove,
                    text = "Confirmar",
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
    }
}