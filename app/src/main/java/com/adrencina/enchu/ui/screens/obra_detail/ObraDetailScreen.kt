package com.adrencina.enchu.ui.screens.obra_detail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
        onFabPressed = viewModel::onFabPressed
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
    onTabSelected: (Int) -> Unit,
    onFabPressed: () -> Unit
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
                    ObraInfoSection(obra = uiState.obra)

                    ObraDetailTabs(
                        selectedTabIndex = uiState.selectedTabIndex,
                        tabTitles = tabTitles,
                        onTabSelected = onTabSelected
                    )

                    TabContentArea(
                        selectedTabIndex = uiState.selectedTabIndex,
                        tabTitles = tabTitles,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

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
                    text = obra?.nombre?.uppercase() ?: "Cargando...",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = AppIcons.ArrowBack,
                    contentDescription = "Atrás",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

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
            border = null
        )
    }
}

@Composable
private fun ObraDetailTabs(
    selectedTabIndex: Int,
    tabTitles: List<String>,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                                text = {
                                    Text(
                                        modifier = Modifier.offset(y = 4.dp),                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TabContentArea(
    selectedTabIndex: Int,
    tabTitles: List<String>,
    modifier: Modifier = Modifier
) {
    when (selectedTabIndex) {
        1 -> {
            FilesScreen()
        }
        else -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(Dimens.PaddingMedium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Contenido para ${tabTitles[selectedTabIndex]}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ObraDetailScreenContentPreview() {
    EnchuTheme {
        ObraDetailScreenContent(
            uiState = ObraDetailUiState.Success(
                obra = Obra(
                    id = "1",
                    clienteNombre = "ESFORZAR S.A.",
                    nombreObra = "Ampliación del salón de informática.",
                    descripcion = "Cableado e instalación general.",
                    estado = "En Progreso",
                    fechaCreacion = Date()
                ),
                selectedTabIndex = 0
            ),
            onBackPressed = {},
            onMenuPressed = {},
            onDismissMenu = {},
            onEditObra = {},
            onArchiveObra = {},
            onTabSelected = {},
            onFabPressed = {}
        )
    }
}