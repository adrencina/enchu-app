package com.adrencina.enchu.ui.screens.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.content.Intent
import kotlinx.coroutines.launch

import com.adrencina.enchu.ui.components.EmptyState
import com.adrencina.enchu.ui.components.EnchuButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onObraClick: (String) -> Unit,
    onObraActionClick: (String, Int) -> Unit = { _, _ -> },
    onAddObraClick: () -> Unit,
    onArchivedObrasClick: () -> Unit = {},
    newObraResult: String? = null,
    onClearNewObraResult: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(newObraResult) {
        newObraResult?.let {
            viewModel.onNewObraCreated(it)
            onClearNewObraResult()
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Reintentar",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.retry()
            }
            viewModel.clearUserMessage()
        }
    }

    val cloudIcon = if (uiState.isError) Icons.Outlined.CloudOff else Icons.Default.Cloud
    val cloudColor = if (uiState.isError) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            val totalObras = uiState.recientes.size + (if (uiState.obraActiva != null) 1 else 0)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Spacer(Modifier.statusBarsPadding())
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Hola, Adrián",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (uiState.plan == "PRO") {
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(4.dp),
                                    ) {
                                        Text(
                                            text = "PRO",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Tenemos $totalObras obras en curso hoy",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = cloudIcon,
                                contentDescription = "Estado de conexión",
                                tint = cloudColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        
        if (uiState.isLoading) {
             Box(modifier = Modifier.padding(paddingValues)) {
                 HomeSkeletonLoader()
             }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 24.dp 
                )
            ) {
                if (uiState.userRole.canViewMoney()) {
                    item {
                        SummaryCard(
                            totalCobrado = uiState.totalCobrado,
                            totalPendiente = uiState.totalPendiente,
                            saldoTotal = uiState.saldoTotal
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }

                item {
                    uiState.obraActiva?.let { obra ->
                        HeroObraCard(
                            obra = obra,
                            onObraClick = onObraClick,
                            onCameraClick = { onObraActionClick(it, 0) },
                            onTasksClick = { onObraActionClick(it, 2) },
                            onFilesClick = { onObraActionClick(it, 1) },
                            onWhatsAppClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    val phone = obra.telefono.filter { it.isDigit() }
                                    data = android.net.Uri.parse("https://wa.me/$phone")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("No se pudo abrir WhatsApp")
                                    }
                                }
                            }
                        )
                    }
                }

                if (uiState.recientes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recientes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }
                    items(uiState.recientes) { obra ->
                        RecentObraCard(
                            obra = obra,
                            onClick = onObraClick
                        )
                    }
                } else if (uiState.obraActiva == null) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Construction,
                            title = "No hay obras activas",
                            description = "Parece que no tenés ninguna obra en curso. ¡Empezá creando una nueva!",
                            action = {
                                EnchuButton(
                                    onClick = onAddObraClick,
                                    text = "Crear mi primera obra",
                                    icon = Icons.Default.Add
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
