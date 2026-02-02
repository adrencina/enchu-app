package com.adrencina.enchu.ui.screens.obra_detail
import com.adrencina.enchu.domain.model.EstadoObra

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.domain.model.Avance
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.adrencina.enchu.domain.model.Obra
import com.adrencina.enchu.domain.model.PresupuestoItem
import com.adrencina.enchu.domain.model.Tarea
import com.adrencina.enchu.ui.screens.obra_detail.files.FilesScreen
import com.adrencina.enchu.ui.screens.obra_detail.presupuesto.MaterialSearchDialog
import com.adrencina.enchu.ui.screens.obra_detail.presupuesto.PresupuestoScreen
import com.adrencina.enchu.ui.screens.obra_detail.registros.AddAvanceDialog
import com.adrencina.enchu.ui.screens.obra_detail.registros.RegistrosScreen
import com.adrencina.enchu.ui.screens.obra_detail.tareas.TareasScreen
import com.adrencina.enchu.ui.screens.obra_detail.caja.CajaScreen
import com.adrencina.enchu.ui.screens.obra_detail.caja.AddMovimientoDialog
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.ui.theme.EnchuTheme
import com.adrencina.enchu.ui.theme.Exito
import java.util.Date

import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable

// Main entry point
@Composable
fun ObraDetailScreen(
    viewModel: ObraDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { viewModel.onFileSelected(it) }
        }
    )

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ObraDetailEffect.NavigateBack -> onNavigateBack()
                ObraDetailEffect.LaunchFilePicker -> filePickerLauncher.launch("*/*")
                is ObraDetailEffect.SharePdf -> {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        effect.file
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir Presupuesto"))
                }
            }
        }
    }

    ObraDetailScreenContent(
        uiState = uiState,
        onBackPressed = viewModel::onBackPressed,
        onMenuPressed = viewModel::onMenuPressed,
        onDismissMenu = viewModel::onDismissMenu,
        onEditObra = viewModel::onEditObra,
        onArchiveObra = viewModel::onArchiveObra,
        onDeleteObraClick = viewModel::onDeleteObraClick,
        onExportPdf = viewModel::onExportPdf,
        onTabSelected = viewModel::onTabSelected,
        onFabPressed = viewModel::onFabPressed,
        onDismissEditDialog = viewModel::onDismissEditDialog,
        onConfirmEdit = viewModel::onConfirmEdit,
        onNameChanged = viewModel::onNameChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onEstadoChanged = viewModel::onEstadoChanged,
        onTelefonoChanged = viewModel::onTelefonoChanged,
        onDireccionChanged = viewModel::onDireccionChanged,
        onClienteChanged = viewModel::onClienteChanged,
        onToggleExpandEditDialog = viewModel::onToggleExpandEditDialog,
        onDismissArchiveDialog = viewModel::onDismissArchiveDialog,
        onConfirmArchive = viewModel::onConfirmArchive,
        onDismissDeleteDialog = viewModel::onDismissDeleteDialog,
        onConfirmDelete = viewModel::onConfirmDelete,
        onAddTarea = viewModel::onAddTarea,
        onToggleTarea = viewModel::onToggleTarea,
        onDeleteTarea = viewModel::onDeleteTarea,
        onDismissAddAvanceDialog = viewModel::onDismissAddAvanceDialog,
        onConfirmAddAvance = viewModel::onConfirmAddAvance,
        onDeleteAvance = viewModel::onDeleteAvance,
        onDismissAddPresupuestoItemDialog = viewModel::onDismissAddPresupuestoItemDialog,
        onConfirmAddPresupuestoItem = viewModel::onAddPresupuestoItem,
        onDeletePresupuestoItem = viewModel::onDeletePresupuestoItem,
        onUpdatePresupuestoItemLogistics = viewModel::onUpdateItemLogistics,
        onDismissAddMovimientoDialog = viewModel::onDismissAddMovimientoDialog,
        onConfirmAddMovimiento = viewModel::onAddMovimiento,
        onDeleteMovimiento = viewModel::onDeleteMovimiento,
        onDismissAddTareaDialog = viewModel::onDismissAddTareaDialog
    )
}

// Content composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObraDetailScreenContent(
    uiState: ObraDetailUiState,
    onBackPressed: () -> Unit,
    onMenuPressed: () -> Unit,
    onDismissMenu: () -> Unit,
    onEditObra: () -> Unit,
    onArchiveObra: () -> Unit,
    onDeleteObraClick: () -> Unit,
    onExportPdf: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onFabPressed: () -> Unit,
    onDismissEditDialog: () -> Unit,
    onConfirmEdit: () -> Unit,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onEstadoChanged: (String) -> Unit,
    onTelefonoChanged: (String) -> Unit,
    onDireccionChanged: (String) -> Unit,
    onClienteChanged: (com.adrencina.enchu.data.model.Cliente) -> Unit,
    onToggleExpandEditDialog: () -> Unit,
    onDismissArchiveDialog: () -> Unit,
    onConfirmArchive: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
    onAddTarea: (String) -> Unit,
    onToggleTarea: (Tarea) -> Unit,
    onDeleteTarea: (Tarea) -> Unit,
    onDismissAddAvanceDialog: () -> Unit,
    onConfirmAddAvance: (String, List<android.net.Uri>) -> Unit,
    onDeleteAvance: (Avance) -> Unit,
    onDismissAddPresupuestoItemDialog: () -> Unit,
    onConfirmAddPresupuestoItem: (PresupuestoItem) -> Unit,
    onDeletePresupuestoItem: (PresupuestoItem) -> Unit,
    onUpdatePresupuestoItemLogistics: (PresupuestoItem, Boolean, Boolean, Double?) -> Unit,
    onDismissAddMovimientoDialog: () -> Unit,
    onConfirmAddMovimiento: (com.adrencina.enchu.domain.model.Movimiento) -> Unit,
    onDeleteMovimiento: (String) -> Unit,
    onDismissAddTareaDialog: () -> Unit
) {
    val tabTitles = listOf("LOG", "ARCHIVOS", "TAREAS", "MATERIALES", "CAJA")

    Scaffold(
        topBar = {
            val (obra, isMenuExpanded, selectedTabIndex) = when (uiState) {
                is ObraDetailUiState.Success -> Triple(uiState.obra, uiState.isMenuExpanded, uiState.selectedTabIndex)
                else -> Triple(null, false, 0)
            }
            ObraDetailTopAppBar(
                obra = obra,
                onBackPressed = onBackPressed,
                actions = {
                    Box {
                        IconButton(onClick = onMenuPressed) {
                            Icon(
                                imageVector = AppIcons.MoreVert,
                                contentDescription = "Menú de opciones"
                            )
                        }
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = onDismissMenu
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar Obra") },
                                onClick = onEditObra
                            )
                            DropdownMenuItem(
                                text = { Text("Archivar Obra") },
                                onClick = onArchiveObra
                            )
                            if (selectedTabIndex == 3) { // Presupuesto Tab
                                DropdownMenuItem(
                                    text = { Text("Exportar PDF") },
                                    onClick = onExportPdf
                                )
                            }
                            // Opción Eliminar (Separada visualmente si es posible, o al final)
                            DropdownMenuItem(
                                text = { Text("Eliminar Obra", color = MaterialTheme.colorScheme.error) },
                                onClick = onDeleteObraClick,
                                leadingIcon = { 
                                    Icon(
                                        imageVector = Icons.Default.Delete, 
                                        contentDescription = null, 
                                        tint = MaterialTheme.colorScheme.error
                                    ) 
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabPressed,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = AppIcons.Add,
                    contentDescription = "Añadir"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is ObraDetailUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ObraDetailUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${uiState.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is ObraDetailUiState.Success -> {
                    if (uiState.showEditDialog) {
                        EditObraDialog(
                            uiState = uiState,
                            onDismiss = onDismissEditDialog,
                            onConfirm = onConfirmEdit,
                            onNameChanged = onNameChanged,
                            onDescriptionChanged = onDescriptionChanged,
                            onEstadoChanged = onEstadoChanged,
                            onTelefonoChanged = onTelefonoChanged,
                            onDireccionChanged = onDireccionChanged,
                            onClienteChanged = onClienteChanged, // Pasamos el callback
                            onToggleExpand = onToggleExpandEditDialog
                        )
                    }

                    if (uiState.showArchiveDialog) {
                        ArchiveConfirmationDialog(
                            onDismiss = onDismissArchiveDialog,
                            onConfirm = onConfirmArchive
                        )
                    }
                    
                    if (uiState.showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = onDismissDeleteDialog,
                            title = { Text("Eliminar Obra") },
                            text = { Text("¿Estás seguro de que quieres eliminar esta obra y todos sus datos asociados? Esta acción es irreversible.") },
                            confirmButton = {
                                TextButton(
                                    onClick = onConfirmDelete,
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Eliminar")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = onDismissDeleteDialog) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }

                    if (uiState.showAddAvanceDialog) {
                        AddAvanceDialog(
                            onDismiss = onDismissAddAvanceDialog,
                            onConfirm = onConfirmAddAvance
                        )
                    }

                    if (uiState.showAddPresupuestoItemDialog) {
                        MaterialSearchDialog(
                            onDismiss = onDismissAddPresupuestoItemDialog,
                            onConfirm = onConfirmAddPresupuestoItem
                        )
                    }

                    if (uiState.showAddTareaDialog) {
                        AddTareaDialog(
                            onDismiss = onDismissAddTareaDialog,
                            onConfirm = { 
                                onAddTarea(it)
                                onDismissAddTareaDialog()
                            }
                        )
                    }

                    if (uiState.showAddMovimientoDialog) {
                        AddMovimientoDialog(
                            onDismiss = onDismissAddMovimientoDialog,
                            onConfirm = onConfirmAddMovimiento
                        )
                    }

                    ObraInfoSection(obra = uiState.obra)

                    ObraDetailTabs(
                        selectedTabIndex = uiState.selectedTabIndex,
                        tabTitles = tabTitles,
                        onTabSelected = onTabSelected
                    )

                    TabContentArea(
                        selectedTabIndex = uiState.selectedTabIndex,
                        tareas = uiState.tareas,
                        avances = uiState.avances,
                        presupuestoItems = uiState.presupuestoItems,
                        movimientos = uiState.movimientos,
                        onAddTarea = onAddTarea,
                        onToggleTarea = onToggleTarea,
                        onDeleteTarea = onDeleteTarea,
                        onDeleteAvance = onDeleteAvance,
                        onDeletePresupuestoItem = onDeletePresupuestoItem,
                        onUpdatePresupuestoItemLogistics = onUpdatePresupuestoItemLogistics,
                        onDeleteMovimiento = onDeleteMovimiento,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// TopAppBar
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ObraDetailTopAppBar(
    obra: Obra?,
    onBackPressed: () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    TopAppBar(
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = obra?.clienteNombre?.uppercase() ?: "Cargando...",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = AppIcons.ArrowBack,
                    contentDescription = "Atrás",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

// Info Section Minimalista
@Composable
private fun ObraInfoSection(obra: Obra) {
    var isDescriptionExpanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { isDescriptionExpanded = !isDescriptionExpanded }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = obra.nombreObra,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))

            SuggestionChip(
                onClick = { /* No action */ },
                label = { 
                    Text(
                        text = obra.estado.value, 
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = if (obra.estado == EstadoObra.EN_PROGRESO) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = null,
                modifier = Modifier.height(24.dp)
            )
        }

        if (obra.descripcion.isNotBlank()) {
            AnimatedVisibility(visible = isDescriptionExpanded) {
                Text(
                    text = obra.descripcion,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (!isDescriptionExpanded) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "Ver descripción",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Tabs Minimalistas con Iconos
@Composable
private fun ObraDetailTabs(selectedTabIndex: Int, tabTitles: List<String>, onTabSelected: (Int) -> Unit) {
    val icons = listOf(
        Icons.Default.Description, // LOG
        Icons.Default.Folder,      // ARCHIVOS
        Icons.Default.Task,        // TAREAS
        Icons.Default.ShoppingCart,// MATERIALES
        Icons.Default.AttachMoney  // CAJA
    )

    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 0.dp,
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (index < icons.size) {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Tab Content
@Composable
private fun TabContentArea(
    selectedTabIndex: Int,
    tareas: List<Tarea>,
    avances: List<Avance>,
    presupuestoItems: List<PresupuestoItem>,
    movimientos: List<com.adrencina.enchu.domain.model.Movimiento>,
    onAddTarea: (String) -> Unit,
    onToggleTarea: (Tarea) -> Unit,
    onDeleteTarea: (Tarea) -> Unit,
    onDeleteAvance: (Avance) -> Unit,
    onDeletePresupuestoItem: (PresupuestoItem) -> Unit,
    onUpdatePresupuestoItemLogistics: (PresupuestoItem, Boolean, Boolean, Double?) -> Unit,
    onDeleteMovimiento: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (selectedTabIndex) {
            0 -> RegistrosScreen(
                avances = avances,
                onDeleteAvance = onDeleteAvance
            )
            1 -> FilesScreen()
            2 -> TareasScreen(
                tareas = tareas,
                onAddTarea = onAddTarea,
                onToggleTarea = onToggleTarea,
                onDeleteTarea = onDeleteTarea
            )
            3 -> PresupuestoScreen(
                presupuestoItems = presupuestoItems,
                onDeleteItem = onDeletePresupuestoItem,
                onUpdateLogistics = onUpdatePresupuestoItemLogistics
            )
            4 -> CajaScreen(
                movimientos = movimientos,
                onDeleteMovimiento = onDeleteMovimiento
            )
        }
    }
}

@Composable
private fun ArchiveConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Archivar Obra") },
        text = { Text("¿Deseas finalizar y archivar esta obra?\n\nAl archivarla, liberarás espacio en tu lista de 'Obras Activas' y podrás crear nuevas obras sin perder los datos de esta.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Archivar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Preview
@Preview(showBackground = true)
@Composable
fun ObraDetailScreenContentPreview() {
    EnchuTheme {
        ObraDetailScreenContent(
            uiState = ObraDetailUiState.Success(
                obra = Obra(
                    clienteNombre = "Cliente de Prueba",
                    nombreObra = "Nombre de la Obra",
                    descripcion = "Esta es una descripción de ejemplo para la obra que es un poco más larga para ver cómo se ajusta.",
                    estado = EstadoObra.EN_PROGRESO,
                    fechaCreacion = Date()
                )
            ),
            onBackPressed = {},
            onMenuPressed = {},
            onDismissMenu = {},
            onEditObra = {},
            onArchiveObra = {},
            onDeleteObraClick = {},
            onExportPdf = {},
            onTabSelected = {},
            onFabPressed = {},
            onDismissEditDialog = {},
            onConfirmEdit = {},
            onNameChanged = {},
            onDescriptionChanged = {},
            onEstadoChanged = {},
            onTelefonoChanged = {},
            onDireccionChanged = {},
            onClienteChanged = {},
            onToggleExpandEditDialog = {},
            onDismissArchiveDialog = {},
            onConfirmArchive = {},
            onDismissDeleteDialog = {},
            onConfirmDelete = {},
            onAddTarea = {},
            onToggleTarea = {},
            onDeleteTarea = {},
            onDismissAddAvanceDialog = {},
            onConfirmAddAvance = { _, _ -> },
            onDeleteAvance = {},
            onDismissAddPresupuestoItemDialog = {},
            onConfirmAddPresupuestoItem = {},
            onDeletePresupuestoItem = {},
            onUpdatePresupuestoItemLogistics = { _, _, _, _ -> },
            onDismissAddMovimientoDialog = {},
            onConfirmAddMovimiento = {},
            onDeleteMovimiento = {},
            onDismissAddTareaDialog = {}
        )
    }
}

@Composable
private fun AddTareaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarea") },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Descripción") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirm(text)
                        onDismiss()
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
