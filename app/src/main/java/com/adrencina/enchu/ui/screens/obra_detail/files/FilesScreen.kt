package com.adrencina.enchu.ui.screens.obra_detail.files

import android.content.ActivityNotFoundException
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.adrencina.enchu.data.model.FileEntity
import com.adrencina.enchu.ui.components.EnchuButton
import com.adrencina.enchu.ui.components.EnchuDialog
import com.adrencina.enchu.ui.components.AppTextField
import com.adrencina.enchu.ui.theme.Dimens
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun FilesScreen(
    viewModel: FilesViewModel = hiltViewModel()
) {
    val lazyPagingItems = viewModel.files.collectAsLazyPagingItems()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.viewEffect.collectLatest { effect ->
            when (effect) {
                is FileViewEffect.OpenFile -> {
                    try {
                        context.startActivity(effect.intent)
                    } catch (e: ActivityNotFoundException) {
                        scope.launch { snackbarHostState.showSnackbar("No se encontró una aplicación para abrir este archivo.") }
                    }
                }
                is FileViewEffect.ShareFile -> {
                    try {
                        context.startActivity(effect.intent)
                    } catch (e: ActivityNotFoundException) {
                        scope.launch { snackbarHostState.showSnackbar("No hay aplicaciones disponibles para compartir este archivo.") }
                    }
                }
                is FileViewEffect.ShowError -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (lazyPagingItems.loadState.refresh) {
                is LoadState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is LoadState.Error -> Text(
                    text = "Error al cargar los archivos.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
                is LoadState.NotLoading -> {
                    if (lazyPagingItems.itemCount == 0) {
                        Text(
                            text = "No hay archivos subidos.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
                        ) {
                            items(lazyPagingItems.itemCount) { index ->
                                val file = lazyPagingItems[index]
                                if (file != null) {
                                    FileItem(
                                        file = file,
                                        onClick = { viewModel.onFileClick(file) },
                                        onRename = { viewModel.onRenameRequest(file) },
                                        onDelete = { viewModel.onDeleteRequest(file) },
                                        onShare = { viewModel.onShareRequest(file) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showRenameDialog && uiState.fileToModify != null) {
            RenameDialog(
                file = uiState.fileToModify,
                onConfirm = { newName -> viewModel.onConfirmRename(newName) },
                onDismiss = { viewModel.onDismissDialog() }
            )
        }

        if (uiState.showDeleteConfirmDialog && uiState.fileToModify != null) {
            DeleteConfirmDialog(
                file = uiState.fileToModify,
                onConfirm = { viewModel.onConfirmDelete() },
                onDismiss = { viewModel.onDismissDialog() }
            )
        }
    }
}

@Composable
private fun RenameDialog(
    file: FileEntity?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (file == null) return

    val originalName = file.fileName.substringBeforeLast('.')
    val originalExtension = file.fileName.substringAfterLast('.', "")

    var nameWithoutExtension by remember(file) { mutableStateOf(originalName) }

    EnchuDialog(
        onDismiss = onDismiss,
        title = "Renombrar archivo",
        confirmButton = {
            EnchuButton(
                onClick = {
                    val finalName = if (originalExtension.isNotEmpty()) {
                        "$nameWithoutExtension.$originalExtension"
                    } else {
                        nameWithoutExtension
                    }
                    onConfirm(finalName)
                },
                text = "Guardar",
                enabled = nameWithoutExtension.isNotBlank() && nameWithoutExtension != originalName
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(56.dp)) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    ) {
        Column {
            Text(
                text = "Ingresa el nuevo nombre para el archivo:",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            AppTextField(
                value = nameWithoutExtension,
                onValueChange = { nameWithoutExtension = it },
                placeholder = "Nuevo nombre",
                singleLine = true
            )
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    file: FileEntity?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (file == null) return
    
    EnchuDialog(
        onDismiss = onDismiss,
        title = "Eliminar archivo",
        confirmButton = {
            EnchuButton(
                onClick = onConfirm,
                text = "Eliminar",
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(56.dp)) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    ) {
        Text(
            text = "¿Estás seguro de que quieres eliminar \"${file.fileName}\"? Esta acción no se puede deshacer.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

