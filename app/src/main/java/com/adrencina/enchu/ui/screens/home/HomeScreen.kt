package com.adrencina.enchu.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
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
    onObraClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

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
        topBar = { HomeTopAppBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddObraClick,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = AppIcons.Add,
                    contentDescription = AppStrings.addObra
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val newPadding = PaddingValues(
            top = paddingValues.calculateTopPadding(),
            start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
            end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
            bottom = 0.dp
        )

        HomeScreenContent(
            uiState = uiState,
            onObraClick = onObraClick,
            modifier = Modifier.padding(newPadding)
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
    SmallTopAppBar(
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
            containerColor = MaterialTheme.colorScheme.error,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
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