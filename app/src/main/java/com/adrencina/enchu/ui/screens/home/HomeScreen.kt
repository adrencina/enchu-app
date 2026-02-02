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
import androidx.compose.ui.text.font.FontWeight
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

        // 2. Contenido Scrollable
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            DashboardSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )
            
            Spacer(Modifier.height(12.dp))

            ActiveWorksRow(
                obras = state.activeObras,
                onClick = onObraClick
            )

            // Grid de Informes Financieros REALES
            ReportsGrid(
                saldoTotal = state.saldoTotal,
                totalPendiente = state.totalPendiente,
                totalGastado = state.totalGastado
            )
            
            // ... resto del contenido (Archivados, etc)
            if (state.archivedObras.isNotEmpty()) {
                ArchivedWorksRow(
                    obras = state.archivedObras,
                    onClick = onArchivedClick // O navegar a detalle
                )
            }
            
            Spacer(Modifier.height(80.dp)) // Espacio para BottomBar
        }
    }
}

@Composable
fun ArchivedWorksRow(
    obras: List<com.adrencina.enchu.domain.model.Obra>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Archivados recientes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClick) {
                Text("Ver todo")
            }
        }
        
        // ... (Simpler list or row for archived)
    }
}
