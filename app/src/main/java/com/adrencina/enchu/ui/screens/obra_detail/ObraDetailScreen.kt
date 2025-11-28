package com.adrencina.enchu.ui.screens.obra_detail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.ui.screens.obra_detail.files.FilesScreen
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.ui.theme.EnchuTheme
import com.adrencina.enchu.ui.theme.Exito
import java.util.Date

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

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ObraDetailEffect.NavigateBack -> onNavigateBack()
                ObraDetailEffect.LaunchFilePicker -> filePickerLauncher.launch("*/*")
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
        onTabSelected = viewModel::onTabSelected,
        onFabPressed = viewModel::onFabPressed,
        onDismissEditDialog = viewModel::onDismissEditDialog,
        onConfirmEdit = viewModel::onConfirmEdit,
        onNameChanged = viewModel::onNameChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onEstadoChanged = viewModel::onEstadoChanged,
        onTelefonoChanged = viewModel::onTelefonoChanged,
        onDireccionChanged = viewModel::onDireccionChanged,
        onToggleExpandEditDialog = viewModel::onToggleExpandEditDialog,
        onDismissArchiveDialog = viewModel::onDismissArchiveDialog,
        onConfirmArchive = viewModel::onConfirmArchive
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
    onTabSelected: (Int) -> Unit,
    onFabPressed: () -> Unit,
    onDismissEditDialog: () -> Unit,
    onConfirmEdit: () -> Unit,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onEstadoChanged: (String) -> Unit,
    onTelefonoChanged: (String) -> Unit,
    onDireccionChanged: (String) -> Unit,
    onToggleExpandEditDialog: () -> Unit,
    onDismissArchiveDialog: () -> Unit,
    onConfirmArchive: () -> Unit
) {
    val tabTitles = listOf("REGISTROS", "ARCHIVOS", "TAREAS")

    Scaffold(
        topBar = {
            val (obra, isMenuExpanded) = when (uiState) {
                is ObraDetailUiState.Success -> uiState.obra to uiState.isMenuExpanded
                else -> null to false
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
                            onToggleExpand = onToggleExpandEditDialog
                        )
                    }

                    if (uiState.showArchiveDialog) {
                        ArchiveConfirmationDialog(
                            onDismiss = onDismissArchiveDialog,
                            onConfirm = onConfirmArchive
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

// Info Section
@Composable
private fun ObraInfoSection(obra: Obra) {
    Column(modifier = Modifier.padding(start = Dimens.PaddingMedium, end = Dimens.PaddingMedium, top = 0.dp, bottom = 0.dp)) {
        Text(
            text = obra.nombreObra,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (obra.descripcion.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = obra.descripcion,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        SuggestionChip(
            onClick = { /* No action */ },
            label = { Text(obra.estado) },
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = Exito.copy(alpha = 0.8f),
                labelColor = Color.White
            ),
            border = null,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
    }
}

// Tabs
@Composable
private fun ObraDetailTabs(selectedTabIndex: Int, tabTitles: List<String>, onTabSelected: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = { Text(text = title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
            )
        }
    }
}

// Tab Content
@Composable
private fun TabContentArea(selectedTabIndex: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        when (selectedTabIndex) {
            0 -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Registros") }
            1 -> FilesScreen()
            2 -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Tareas") }
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
        text = { Text("¿Estás seguro? La obra se moverá a la sección de archivados.") },
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
                    estado = "En Proceso",
                    fechaCreacion = Date()
                )
            ),
            onBackPressed = {},
            onMenuPressed = {},
            onDismissMenu = {},
            onEditObra = {},
            onArchiveObra = {},
            onTabSelected = {},
            onFabPressed = {},
            onDismissEditDialog = {},
            onConfirmEdit = {},
            onNameChanged = {},
            onDescriptionChanged = {},
            onEstadoChanged = {},
            onTelefonoChanged = {},
            onDireccionChanged = {},
            onToggleExpandEditDialog = {},
            onDismissArchiveDialog = {},
            onConfirmArchive = {}
        )
    }
}
