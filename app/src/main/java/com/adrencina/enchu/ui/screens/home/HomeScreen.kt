package com.adrencina.enchu.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.R
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.core.resources.AppStrings
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.ui.components.ObraCard
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.ui.theme.EnchuTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    newObraResult: String?,
    onClearNewObraResult: () -> Unit,
    onAddObraClick: () -> Unit,
    onObraClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(newObraResult) {
        if (newObraResult != null) {
            // 1. Le decimos al ViewModel que muestre el Snackbar.
            viewModel.onNewObraCreated(newObraResult)

            // 2. CAMBIO: Eliminamos la llamada a viewModel.loadObras(). ¡Ya no es necesaria!
            // El listener de Firestore se encargará de la actualización automáticamente.

            // 3. Limpiamos el resultado.
            onClearNewObraResult()
        }
    }

    // Efecto para mostrar el Snackbar (sin cambios)
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is HomeUiEffect.ShowObraCreatedSnackbar -> {
                    val message = context.getString(R.string.obra_created_success_format, effect.clientName)
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = { HomeTopAppBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddObraClick,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(
                    imageVector = AppIcons.Add,
                    contentDescription = AppStrings.addObra
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        HomeScreenContent(
            uiState = uiState,
            onObraClick = onObraClick,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onObraClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is HomeUiState.Loading -> LoadingState()
            is HomeUiState.Success -> {
                if (uiState.obras.isEmpty()) {
                    EmptyState()
                } else {
                    ObrasGrid(obras = uiState.obras, onObraClick = onObraClick)
                }
            }
            is HomeUiState.Error -> ErrorState(message = uiState.message)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar() {
    TopAppBar(
        title = { Text(AppStrings.homeScreenTitle) },
        actions = {
            IconButton(onClick = { /* TODO: Implement search */ }) {
                Icon(imageVector = AppIcons.Search, contentDescription = AppStrings.search)
            }
            IconButton(onClick = { /* TODO: Implement menu */ }) {
                Icon(imageVector = AppIcons.MoreVert, contentDescription = AppStrings.moreOptions)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun LoadingState() {
    CircularProgressIndicator(
        modifier = Modifier.size(Dimens.ProgressIndicatorSize),
        color = MaterialTheme.colorScheme.secondary // Color Accion
    )
}

@Composable
private fun EmptyState() {
    Text(
        text = AppStrings.emptyObrasMessage,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(Dimens.PaddingLarge)
    )
}

@Composable
private fun ErrorState(message: String) {
    Text(
        text = String.format(AppStrings.errorLoadingObras, message),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(Dimens.PaddingLarge)
    )
}

@Composable
private fun ObrasGrid(obras: List<Obra>, onObraClick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.PaddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium),
        horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
    ) {
        items(items = obras, key = { it.id }) { obra ->
            ObraCard(obra = obra, onClick = { onObraClick(obra.id) })
        }
    }
}

// --- PREVIEWS ---
class HomeUiStatePreviewProvider : PreviewParameterProvider<HomeUiState> {
    private val sampleObras = listOf(
        Obra("1", "Ampliación Casa", "Cliente A"),
        Obra("2", "Pintura Depto", "Cliente B"),
        Obra("3", "Instalación Eléctrica", "Cliente C"),
        Obra("4", "Proyecto Quincho", "Cliente D"),
    )

    override val values = sequenceOf(
        HomeUiState.Loading,
        HomeUiState.Success(emptyList()),
        HomeUiState.Success(sampleObras),
        HomeUiState.Error("No se pudo conectar al servidor.")
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun HomeScreenPreview(
    @PreviewParameter(HomeUiStatePreviewProvider::class) uiState: HomeUiState
) {
    EnchuTheme {
        Scaffold(
            topBar = { HomeTopAppBar() },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) { Icon(AppIcons.Add, AppStrings.addObra) }
            }
        ) { padding ->
            HomeScreenContent(uiState = uiState, onObraClick = {}, modifier = Modifier.padding(padding))
        }
    }
}