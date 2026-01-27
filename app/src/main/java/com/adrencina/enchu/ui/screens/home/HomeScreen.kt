package com.adrencina.enchu.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.R
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    newObraResult: String?,
    onClearNewObraResult: () -> Unit,
    onAddObraClick: () -> Unit,
    onObraClick: (String) -> Unit,
    onArchivedObrasClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var searchQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(newObraResult) {
        if (newObraResult != null) {
            viewModel.onNewObraCreated(newObraResult)
            onClearNewObraResult()
        }
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                // Estado de carga silencioso: Mantenemos el fondo limpio mientras cargan los datos.
                // Esto evita el 'flickeo' de un spinner durante la transición rápida desde Login.
                Box(modifier = Modifier.fillMaxSize())
            }
            is HomeUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is HomeUiState.Success -> {
                HomeDashboardContent(
                    state = state,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { 
                        searchQuery = it 
                        viewModel.onSearchQueryChanged(it)
                    },
                    onObraClick = onObraClick,
                    onArchivedClick = onArchivedObrasClick,
                    onMenuClick = { /* TODO */ }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )
    }
}

@Composable
fun HomeDashboardContent(
    state: HomeUiState.Success,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onObraClick: (String) -> Unit,
    onArchivedClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Header Fijo
        DashboardHeader(
            userName = state.userName,
            onNotificationClick = { },
            onMenuClick = onMenuClick
        )

        // 2. Contenido Estático (Con Scroll de seguridad)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Barra de Búsqueda
            DashboardSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (searchQuery.isNotEmpty() && state.activeObras.isEmpty() && state.archivedObras.isEmpty()) {
                // Estado de "Búsqueda sin resultados"
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron resultados para \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                // Flujo normal o con resultados parciales
                
                // Sección Mis Obras (Activas) - Siempre visible (maneja su propio empty state)
                // Nota: Si estamos buscando, ActiveWorksRow mostrará solo las que coincidan.
                // Si la búsqueda no arroja activas pero sí archivadas, esto se mostrará vacío (lo cual es correcto o mejorable).
                // Para simplificar: Si no hay búsqueda, mostramos SIEMPRE. Si hay búsqueda, mostramos solo si hay coincidencias.
                
                if (searchQuery.isEmpty() || state.activeObras.isNotEmpty()) {
                    ActiveWorksRow(obras = state.activeObras, onClick = onObraClick)
                }

                // Sección Obras Archivadas - Siempre visible en modo normal
                if (searchQuery.isEmpty() || state.archivedObras.isNotEmpty()) {
                    ArchivedWorksPreview(
                        obras = state.archivedObras,
                        onViewAll = onArchivedClick,
                        onClick = onObraClick
                    )
                }
                
                // Grid de Informes (Solo visible si NO estamos buscando)
                if (searchQuery.isEmpty()) {
                    ReportsGrid()
                }
            }
        }
    }
}
