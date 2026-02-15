package com.adrencina.enchu.ui.screens.obra_detail

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.data.model.UserProfile
import com.adrencina.enchu.domain.model.*
import com.adrencina.enchu.ui.components.EmptyState
import com.adrencina.enchu.ui.components.EnchuButton
import com.adrencina.enchu.ui.components.EnchuDialog
import com.adrencina.enchu.ui.components.SkeletonBox
import com.adrencina.enchu.ui.screens.obra_detail.caja.AddMovimientoDialog
import com.adrencina.enchu.ui.screens.obra_detail.caja.CajaScreen
import com.adrencina.enchu.ui.screens.obra_detail.files.FilesScreen
import com.adrencina.enchu.ui.screens.obra_detail.presupuesto.MaterialSearchDialog
import com.adrencina.enchu.ui.screens.obra_detail.presupuesto.PresupuestoScreen
import com.adrencina.enchu.ui.screens.obra_detail.registros.AddAvanceDialog
import com.adrencina.enchu.ui.screens.obra_detail.registros.RegistrosScreen
import com.adrencina.enchu.ui.screens.obra_detail.tareas.TareasScreen
import java.util.Date

@Composable
fun ObraDetailSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(32.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { SkeletonBox(modifier = Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(12.dp)) }
        }
        Spacer(modifier = Modifier.height(24.dp))
        repeat(5) { SkeletonBox(modifier = Modifier.fillMaxWidth().height(80.dp).padding(vertical = 4.dp), shape = RoundedCornerShape(16.dp)) }
    }
}

@Composable
fun ObraDetailScreen(
    viewModel: ObraDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTareaIdForPhoto by remember { mutableStateOf("") }
    val filePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri -> uri?.let { viewModel.onFileSelected(it) } }
    val context = LocalContext.current

    val taskPhotoPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri -> 
        uri?.let { 
            // Necesitamos saber qué tarea era. Podemos guardarla en un remember.
            viewModel.onTaskPhotoSelected(selectedTareaIdForPhoto, it) 
        } 
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ObraDetailEffect.NavigateBack -> onNavigateBack()
                ObraDetailEffect.LaunchFilePicker -> filePickerLauncher.launch("*/*")
                is ObraDetailEffect.LaunchTaskPhotoPicker -> {
                    selectedTareaIdForPhoto = effect.tareaId
                    taskPhotoPickerLauncher.launch("image/*")
                }
                is ObraDetailEffect.SharePdf -> {
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", effect.file)
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
        onDeleteObraClick = viewModel::onDeleteObra,
        onExportPdf = viewModel::onExportPdf,
        onAssignMemberClick = viewModel::onAssignMemberClick,
        onTabSelected = viewModel::onTabSelected,
        onFabPressed = viewModel::onFabPressed,
        onDismissEditDialog = viewModel::onDismissEditDialog,
        onUpdateObra = viewModel::onUpdateObra,
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
        onConfirmAddMovimiento = viewModel::onConfirmAddMovimiento,
        onDeleteMovimiento = viewModel::onDeleteMovimiento,
        onDismissAddTareaDialog = viewModel::onDismissAddTareaDialog,
        onDismissAssignMemberDialog = viewModel::onDismissAssignMemberDialog,
        onToggleMemberAssignment = viewModel::onToggleMemberAssignment,
        onUpdateMemberPermissions = viewModel::onUpdateMemberPermissions,
        onRetry = viewModel::retry
    )
}

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
    onAssignMemberClick: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onFabPressed: () -> Unit,
    onDismissEditDialog: () -> Unit,
    onUpdateObra: (String, String, EstadoObra, String, String, Cliente?) -> Unit,
    onDismissArchiveDialog: () -> Unit,
    onConfirmArchive: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
    onAddTarea: (String) -> Unit,
    onToggleTarea: (Tarea) -> Unit,
    onDeleteTarea: (Tarea) -> Unit,
    onDismissAddAvanceDialog: () -> Unit,
    onConfirmAddAvance: (String, List<Uri>) -> Unit,
    onDeleteAvance: (Avance) -> Unit,
    onDismissAddPresupuestoItemDialog: () -> Unit,
    onConfirmAddPresupuestoItem: (PresupuestoItem) -> Unit,
    onDeletePresupuestoItem: (PresupuestoItem) -> Unit,
    onUpdatePresupuestoItemLogistics: (PresupuestoItem, Boolean, Boolean, Double?) -> Unit,
    onDismissAddMovimientoDialog: () -> Unit,
    onConfirmAddMovimiento: (Movimiento) -> Unit,
    onDeleteMovimiento: (String) -> Unit,
    onDismissAddTareaDialog: () -> Unit,
    onDismissAssignMemberDialog: () -> Unit,
    onToggleMemberAssignment: (String, Boolean) -> Unit,
    onUpdateMemberPermissions: (String, MemberPermissions) -> Unit,
    onRetry: () -> Unit
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
                    if (selectedTabIndex == 3) {
                        IconButton(onClick = onExportPdf) { Icon(Icons.Default.Description, contentDescription = "Exportar PDF", tint = MaterialTheme.colorScheme.primary) }
                    }
                    Box {
                        IconButton(onClick = onMenuPressed) { Icon(AppIcons.MoreVert, contentDescription = "Menú") }
                        DropdownMenu(expanded = isMenuExpanded, onDismissRequest = onDismissMenu) {
                            DropdownMenuItem(text = { Text("Asignar Equipo") }, onClick = onAssignMemberClick, leadingIcon = { Icon(Icons.Default.GroupAdd, contentDescription = null) })
                            DropdownMenuItem(text = { Text("Editar Obra") }, onClick = onEditObra)
                            DropdownMenuItem(text = { Text("Archivar Obra") }, onClick = onArchiveObra)
                            DropdownMenuItem(text = { Text("Eliminar Obra", color = MaterialTheme.colorScheme.error) }, onClick = onDeleteObraClick, leadingIcon = { Icon(Icons.Default.Delete, tint = MaterialTheme.colorScheme.error, contentDescription = null) })
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onFabPressed, containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary) {
                Icon(AppIcons.Add, contentDescription = "Añadir")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState) {
                is ObraDetailUiState.Loading -> ObraDetailSkeleton()
                is ObraDetailUiState.Error -> EmptyState(Icons.Default.CloudOff, "Algo salió mal", uiState.message, action = { EnchuButton(onClick = onRetry, text = "Reintentar") })
                is ObraDetailUiState.Success -> {
                    if (uiState.showEditDialog) EditObraDialog(uiState, onDismissEditDialog, onUpdateObra)
                    if (uiState.showAssignMemberDialog) {
                        AssignMemberDialog(
                            members = uiState.organizationMembers,
                            assignedIds = uiState.obra.assignedMemberIds,
                            memberPermissions = uiState.obra.memberPermissions,
                            onDismiss = onDismissAssignMemberDialog,
                            onToggleAssignment = onToggleMemberAssignment,
                            onUpdatePermissions = onUpdateMemberPermissions
                        )
                    }
                    if (uiState.showArchiveDialog) ArchiveConfirmationDialog(onDismissArchiveDialog, onConfirmArchive)
                    if (uiState.showDeleteDialog) DeleteConfirmationDialog(onDismissDeleteDialog, onConfirmDelete)
                    if (uiState.showAddAvanceDialog) AddAvanceDialog(onDismissAddAvanceDialog, onConfirmAddAvance)
                    if (uiState.showAddPresupuestoItemDialog) MaterialSearchDialog(onDismissAddPresupuestoItemDialog, onConfirmAddPresupuestoItem)
                    if (uiState.showAddTareaDialog) AddTareaDialog(onDismissAddTareaDialog, onAddTarea)
                    if (uiState.showAddMovimientoDialog) AddMovimientoDialog(onDismissAddMovimientoDialog, onConfirmAddMovimiento)

                    ObraInfoSection(uiState.obra, uiState.tareas)
                    ObraDetailTabs(uiState.selectedTabIndex, tabTitles, onTabSelected)
                    TabContentArea(uiState.selectedTabIndex, uiState.tareas, uiState.avances, uiState.presupuestoItems, uiState.movimientos, onAddTarea, onToggleTarea, onDeleteTarea, onDeleteAvance, onDeletePresupuestoItem, onUpdatePresupuestoItemLogistics, onDeleteMovimiento, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AssignMemberDialog(
    members: List<UserProfile>,
    assignedIds: List<String>,
    memberPermissions: Map<String, MemberPermissions>,
    onDismiss: () -> Unit,
    onToggleAssignment: (String, Boolean) -> Unit,
    onUpdatePermissions: (String, MemberPermissions) -> Unit
) {
    EnchuDialog(onDismiss = onDismiss, title = "Personal Asignado", confirmButton = { EnchuButton(onClick = onDismiss, text = "Cerrar") }) {
        if (members.isEmpty()) {
            Text("No hay otros miembros en tu equipo todavía.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(members) { member ->
                    val isAssigned = assignedIds.contains(member.id)
                    val permissions = memberPermissions[member.id] ?: MemberPermissions()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                                Box(contentAlignment = Alignment.Center) { 
                                    Text(member.displayName.take(1).uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) 
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(member.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(member.role, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Switch(checked = isAssigned, onCheckedChange = { onToggleAssignment(member.id, isAssigned) })
                        }

                        if (isAssigned) {
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            
                            PermissionSwitch("¿Puede editar tareas?", permissions.canEditTasks) { 
                                onUpdatePermissions(member.id, permissions.copy(canEditTasks = it)) 
                            }
                            PermissionSwitch("¿Puede añadir avances/fotos?", permissions.canAddAvances) { 
                                onUpdatePermissions(member.id, permissions.copy(canAddAvances = it)) 
                            }
                            PermissionSwitch("¿Puede subir planos/archivos?", permissions.canAddFiles) { 
                                onUpdatePermissions(member.id, permissions.copy(canAddFiles = it)) 
                            }
                            PermissionSwitch("¿Puede ver presupuestos/caja?", permissions.canViewFinances) { 
                                onUpdatePermissions(member.id, permissions.copy(canViewFinances = it)) 
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ObraDetailTopAppBar(obra: Obra?, onBackPressed: () -> Unit, actions: @Composable RowScope.() -> Unit) {
    TopAppBar(title = { Text(obra?.clienteNombre ?: "Cargando...", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }, navigationIcon = { IconButton(onClick = onBackPressed) { Icon(AppIcons.ArrowBack, contentDescription = "Atrás") } }, actions = actions, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent))
}

@Composable
private fun ObraInfoSection(obra: Obra, tareas: List<Tarea>) {
    var isExp by remember { mutableStateOf(false) }
    val progreso = if (tareas.isNotEmpty()) tareas.count { it.completada }.toFloat() / tareas.size.toFloat() else 0f
    ElevatedCard(onClick = { isExp = !isExp }, modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp), shape = RoundedCornerShape(32.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("DETALLES DE OBRA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    Text(obra.nombreObra, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                }
                Surface(color = if (obra.estado == EstadoObra.EN_PROGRESO) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
                    Text(obra.estado.value.uppercase(), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(8.dp, 4.dp))
                }
            }
            if (obra.descripcion.isNotBlank()) {
                AnimatedVisibility(isExp) { Text(obra.descripcion, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 16.dp)) }
                if (!isExp) Text("Ver más...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }
            if (tareas.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), strokeCap = StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun ObraDetailTabs(sel: Int, titles: List<String>, onSel: (Int) -> Unit) {
    ScrollableTabRow(selectedTabIndex = sel, containerColor = Color.Transparent, edgePadding = 0.dp) {
        titles.forEachIndexed { i, t -> Tab(selected = sel == i, onClick = { onSel(i) }, text = { Text(t, fontSize = 12.sp) }) }
    }
}

@Composable
private fun TabContentArea(sel: Int, t: List<Tarea>, a: List<Avance>, pi: List<PresupuestoItem>, m: List<Movimiento>, onAddT: (String) -> Unit, onTogT: (Tarea) -> Unit, onDelT: (Tarea) -> Unit, onDelA: (Avance) -> Unit, onDelPI: (PresupuestoItem) -> Unit, onUpdPI: (PresupuestoItem, Boolean, Boolean, Double?) -> Unit, onDelM: (String) -> Unit, mod: Modifier) {
    Box(modifier = mod) {
        when (sel) {
            0 -> RegistrosScreen(avances = a, onDeleteAvance = onDelA)
            1 -> FilesScreen()
            2 -> TareasScreen(tareas = t, onAddTarea = onAddT, onToggleTarea = onTogT, onDeleteTarea = onDelT)
            3 -> PresupuestoScreen(presupuestoItems = pi, onDeleteItem = onDelPI, onUpdateLogistics = onUpdPI)
            4 -> CajaScreen(movimientos = m, onDeleteMovimiento = onDelM)
        }
    }
}

@Composable
private fun EditObraDialog(ui: ObraDetailUiState.Success, onDismiss: () -> Unit, onUpd: (String, String, EstadoObra, String, String, Cliente?) -> Unit) {
    var n by remember { mutableStateOf(ui.editedObraName) }
    EnchuDialog(onDismiss = onDismiss, title = "Editar", confirmButton = { EnchuButton(onClick = { onUpd(n, ui.editedObraDescription, ui.editedObraEstado, ui.editedTelefono, ui.editedDireccion, ui.editedCliente) }, text = "Guardar") }) {
        OutlinedTextField(value = n, onValueChange = { n = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ArchiveConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    EnchuDialog(onDismiss = onDismiss, title = "Archivar", confirmButton = { EnchuButton(onClick = onConfirm, text = "Archivar") }) { Text("¿Archivar obra?") }
}

@Composable
private fun DeleteConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    EnchuDialog(onDismiss = onDismiss, title = "Eliminar", confirmButton = { EnchuButton(onClick = onConfirm, text = "Eliminar", containerColor = MaterialTheme.colorScheme.error) }) { Text("¿Eliminar obra?") }
}

@Composable
private fun AddTareaDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var t by remember { mutableStateOf("") }
    EnchuDialog(onDismiss = onDismiss, title = "Tarea", confirmButton = { EnchuButton(onClick = { onConfirm(t) }, text = "Añadir") }) {
        OutlinedTextField(value = t, onValueChange = { t = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
    }
}
