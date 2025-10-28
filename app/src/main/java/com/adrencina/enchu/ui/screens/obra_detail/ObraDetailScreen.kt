package com.adrencina.enchu.ui.screens.obra_detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.data.model.Obra
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

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ObraDetailEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    ObraDetailScreenContent(
        uiState = uiState,
        onBackPressed = viewModel::onBackPressed,
        onMenuPressed = viewModel::onMenuPressed,
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
    onTabSelected: (Int) -> Unit,
    onFabPressed: () -> Unit
) {
    val tabTitles = listOf("REGISTROS", "ARCHIVOS", "TAREAS")

    Scaffold(
        topBar = {
            val obra = when (uiState) {
                is ObraDetailUiState.Success -> uiState.obra
                else -> null
            }
            ObraDetailTopAppBar(
                obra = obra,
                onBackPressed = onBackPressed,
                onMenuPressed = onMenuPressed
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ObraDetailTopAppBar(
    obra: Obra?,
    onBackPressed: () -> Unit,
    onMenuPressed: () -> Unit
) {
    TopAppBar(
        title = { Text(obra?.clienteNombre ?: "Cargando...") },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(imageVector = AppIcons.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = {
            IconButton(onClick = onMenuPressed) {
                Icon(imageVector = AppIcons.MoreVert, contentDescription = "Más opciones")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun ObraInfoSection(obra: Obra) {
    Column(modifier = Modifier.padding(Dimens.PaddingMedium)) {
        Text(
            text = obra.nombreObra,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (obra.descripcion.isNotBlank()) {
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            Text(
                text = obra.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
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
                text = { Text(title) },
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
            onTabSelected = {},
            onFabPressed = {}
        )
    }
}