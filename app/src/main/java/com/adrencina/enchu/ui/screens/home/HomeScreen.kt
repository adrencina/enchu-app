package com.adrencina.enchu.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.data.model.Obra

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    // A futuro, necesitaremos navegar. Añadimos los callbacks.
    // onObraClick: (String) -> Unit,
    // onAddObraClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Obras") },
                // Aquí irían los íconos de búsqueda y menú a futuro
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* onAddObraClick() */ }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Obra")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is HomeUiState.Success -> {
                    if (state.obras.isEmpty()) {
                        EmptyState()
                    } else {
                        ObrasGrid(obras = state.obras, onObraClick = { /* onObraClick(it) */ })
                    }
                }
                is HomeUiState.Error -> {
                    Text(
                        text = "Error al cargar las obras: ${state.message}",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ObrasGrid(obras: List<Obra>, onObraClick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(obras) { obra ->
            ObraCard(obra = obra, onClick = { onObraClick(obra.id) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObraCard(obra: Obra, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f) // Para que sea cuadrada
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // TODO: Necesitamos traer el nombre del cliente. Por ahora mostramos el ID.
                Text(text = obra.clienteId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = obra.nombreObra, style = MaterialTheme.typography.bodyMedium)
            }
            // TODO: A futuro, mostrar la fecha y el ícono
            Text(text = "Fecha", style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.End))
        }
    }
}


@Composable
fun EmptyState() {
    Text(
        text = "Aún no tenés obras creadas.\n¡Tocá el botón '+' para empezar!",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(16.dp)
    )
}