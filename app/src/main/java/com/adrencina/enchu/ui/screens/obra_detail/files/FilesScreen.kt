package com.adrencina.enchu.ui.screens.obra_detail.files

import android.content.ActivityNotFoundException
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.data.model.FileEntity
import com.adrencina.enchu.ui.theme.Dimens
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ln
import kotlin.math.pow

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
                        scope.launch {
                            snackbarHostState.showSnackbar("No se encontró una aplicación para abrir este archivo.")
                        }
                    }
                }
                is FileViewEffect.ShareFile -> {
                    try {
                        context.startActivity(effect.intent)
                    } catch (e: ActivityNotFoundException) {
                        scope.launch {
                            snackbarHostState.showSnackbar("No hay aplicaciones disponibles para compartir este archivo.")
                        }
                    }
                }
                is FileViewEffect.ShowError -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (lazyPagingItems.loadState.refresh) {
                is LoadState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is LoadState.Error -> {
                    Text(
                        text = "Error al cargar los archivos.",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
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
                            items(lazyPagingItems.itemCount, key = { index -> lazyPagingItems.peek(index)?.fileId ?: index }) {
                                val file = lazyPagingItems[it]
                                if (file != null) {
                                    FileItem(
                                        file = file,
                                        onClick = { viewModel.onFileClicked(file) },
                                        onRenameClick = { viewModel.onRenameRequest(file) },
                                        onShareClick = { viewModel.onShareRequest(file) },
                                        onDeleteClick = { viewModel.onDeleteRequest(file) }
                                    )
                                    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                }
                            }

                            if (lazyPagingItems.loadState.append is LoadState.Loading) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(Dimens.PaddingMedium)) {
                                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.showRenameDialog) {
                RenameDialog(
                    file = uiState.fileToModify,
                    onConfirm = { newName -> viewModel.onConfirmRename(newName) },
                    onDismiss = { viewModel.onDismissDialog() }
                )
            }

            if (uiState.showDeleteConfirmDialog) {
                DeleteConfirmDialog(
                    file = uiState.fileToModify,
                    onConfirm = { viewModel.onConfirmDelete() },
                    onDismiss = { viewModel.onDismissDialog() }
                )
            }
        }
    }
}

@Composable
private fun FileItem(
    file: FileEntity,
    onClick: () -> Unit,
    onRenameClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = file.fileName.substringBeforeLast('.'),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            val formattedSize = formatFileSize(file.size)
            val formattedDate = formatDate(file.createdAt)
            Text(text = "$formattedSize • $formattedDate")
        },
        leadingContent = {
            FileIcon(fileExtension = file.fileName.substringAfterLast('.', ""))
        },
        trailingContent = {
            Box {
                IconButton(onClick = { isMenuExpanded = true }) {
                    Icon(
                        imageVector = AppIcons.MoreVert,
                        contentDescription = "Más opciones",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Renombrar") },
                        onClick = {
                            onRenameClick()
                            isMenuExpanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Compartir") },
                        onClick = {
                            onShareClick()
                            isMenuExpanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDeleteClick()
                            isMenuExpanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    )
}

@Composable
private fun FileIcon(fileExtension: String) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Article,
                contentDescription = "Icono de archivo",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = fileExtension.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
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

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        title = { Text("Renombrar archivo") },
        text = {
            OutlinedTextField(
                value = nameWithoutExtension,
                onValueChange = { nameWithoutExtension = it },
                label = { Text("Nuevo nombre") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = if (originalExtension.isNotEmpty()) {
                        "$nameWithoutExtension.$originalExtension"
                    } else {
                        nameWithoutExtension
                    }
                    onConfirm(finalName)
                },
                // Enable button only if the name has changed and is not blank
                enabled = nameWithoutExtension.isNotBlank() && nameWithoutExtension != originalName
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun DeleteConfirmDialog(
    file: FileEntity?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (file == null) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar eliminación") },
        text = { Text("¿Estás seguro de que quieres eliminar \"${file.fileName}\"? Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


private fun formatFileSize(sizeInBytes: Long): String {
    if (sizeInBytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (ln(sizeInBytes.toDouble()) / ln(1024.0)).toInt()
    return String.format("%.1f %s", sizeInBytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}

private fun formatDate(date: Date): String {
    val pattern = "dd MMM yyyy"
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(date)
}

