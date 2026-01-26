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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
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

            // Sección Mis Obras (Activas)
            if (state.activeObras.isNotEmpty()) {
                ActiveWorksRow(obras = state.activeObras, onClick = onObraClick)
            } else if (searchQuery.isEmpty()) {
                Text(
                    text = "No tienes obras en curso",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Sección Obras Archivadas
            if (state.archivedObras.isNotEmpty()) {
                ArchivedWorksPreview(
                    obras = state.archivedObras,
                    onViewAll = onArchivedClick,
                    onClick = onObraClick
                )
            }

            // Grid de Informes
            ReportsGrid()
        }
    }
}
