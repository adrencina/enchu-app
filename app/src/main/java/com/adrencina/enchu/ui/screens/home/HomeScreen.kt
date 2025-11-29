package com.adrencina.enchu.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.R
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.core.resources.AppStrings
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.ui.components.ObraCard
import com.adrencina.enchu.ui.theme.Dimens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            MisObrasTopBar(
                searchQuery = searchQuery,
                onSearchQueryChanged = { query ->
                    searchQuery = query
                    viewModel.onSearchQueryChanged(query)
                },
                onMenuClick = { /* TODO: Implement menu */ }
            )
        },
        floatingActionButton = {
            val state = uiState
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Dimens.PaddingLarge),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (state is HomeUiState.Success && state.archivedCount > 0) {
                    androidx.compose.material3.TextButton(
                        onClick = onArchivedObrasClick,
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 20.dp)
                    ) {
                        Icon(imageVector = AppIcons.Archive, contentDescription = null)
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(Dimens.PaddingSmall))
                        Text("Obras archivadas (${state.archivedCount})")
                    }
                }

                FloatingActionButton(
                    onClick = onAddObraClick,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        // Ajuste para posición estándar visual (16dp lateral, 32dp inferior para seguridad en edge-to-edge)
                        .padding(end = Dimens.PaddingMedium, bottom = 82.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.Add,
                        contentDescription = AppStrings.addObra
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
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

@Composable
fun MisObrasTopBar(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.PaddingLarge)
    ) {
        // Fila del Título y el botón de menú
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.PaddingMedium, end = Dimens.PaddingExtraSmall)
                .padding(bottom = Dimens.PaddingExtraSmall),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppStrings.homeScreenTitle,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Row {
                IconButton(onClick = { /* TODO: Implement notifications */ }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificaciones", // TODO: Add to AppStrings
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = AppIcons.MoreVert,
                        contentDescription = AppStrings.moreOptions,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Barra de Búsqueda
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(56.dp),
            placeholder = {
                Text(
                    text = "Buscar en mis obras...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = AppIcons.Search,
                    contentDescription = AppStrings.search,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = AppIcons.Close,
                            contentDescription = AppStrings.close,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            singleLine = true
        )
    }
}

@Composable
private fun LoadingState() {
    CircularProgressIndicator(
        modifier = Modifier.size(Dimens.ProgressIndicatorSize),
        color = MaterialTheme.colorScheme.secondary
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
        contentPadding = PaddingValues(
            top = Dimens.PaddingSmall,
            start = Dimens.PaddingMedium,
            end = Dimens.PaddingMedium,
            bottom = Dimens.PaddingMedium
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium),
        horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
    ) {
        items(items = obras, key = { it.id }) { obra ->
            ObraCard(obra = obra, onClick = { onObraClick(obra.id) })
        }
    }
}
