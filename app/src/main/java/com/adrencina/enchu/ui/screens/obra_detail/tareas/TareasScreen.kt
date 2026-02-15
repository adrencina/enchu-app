package com.adrencina.enchu.ui.screens.obra_detail.tareas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.adrencina.enchu.ui.components.AppTextField
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.domain.model.Tarea
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.ui.components.EnchuDialog
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TareasScreen(
    tareas: List<Tarea>,
    onAddTarea: (String) -> Unit,
    onToggleTarea: (Tarea) -> Unit,
    onDeleteTarea: (Tarea) -> Unit
) {
    var newTaskText by remember { mutableStateOf("") }
    var selectedTareaForEvidence by remember { mutableStateOf<Tarea?>(null) }

    // Diálogo de Evidencia
    selectedTareaForEvidence?.let { tarea ->
        EvidenceDialog(
            tarea = tarea,
            onDismiss = { selectedTareaForEvidence = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.PaddingMedium)
    ) {
        // Input Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTextField(
                value = newTaskText,
                onValueChange = { newTaskText = it },
                modifier = Modifier.weight(1f),
                placeholder = "¿Qué hay que hacer?",
                imeAction = ImeAction.Done
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scaleVal by animateFloatAsState(
                targetValue = if (isPressed) 0.9f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "scale"
            )

            FilledIconButton(
                onClick = {
                    if (newTaskText.isNotBlank()) {
                        onAddTarea(newTaskText)
                        newTaskText = ""
                    }
                },
                interactionSource = interactionSource,
                modifier = Modifier
                    .size(56.dp)
                    .scale(scaleVal),
                shape = RoundedCornerShape(16.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(AppIcons.Add, contentDescription = "Agregar")
            }
        }

        // List Area
        if (tareas.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No hay tareas pendientes", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tareas, key = { it.id }) { tarea ->
                    TareaItem(
                        tarea = tarea,
                        onToggle = { onToggleTarea(tarea) },
                        onDelete = { onDeleteTarea(tarea) },
                        onShowEvidence = { selectedTareaForEvidence = tarea }
                    )
                }
            }
        }
    }
}

@Composable
fun TareaItem(
    tarea: Tarea,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onShowEvidence: () -> Unit
) {
    val containerColor = if (tarea.completada) 
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
    else 
        MaterialTheme.colorScheme.surfaceContainerLow

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        onClick = { if (tarea.completada) onShowEvidence() else onToggle() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = { if (!tarea.completada) onToggle() },
                enabled = !tarea.completada, // Inmutable si ya está marcada
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            
            Text(
                text = tarea.descripcionTarea,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (tarea.completada) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (tarea.completada) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
            )

            if (tarea.completada && !tarea.completionImageUrl.isNullOrBlank()) {
                Icon(
                    Icons.Default.Image, 
                    contentDescription = "Ver evidencia", 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                IconButton(onClick = onDelete) {
                    Icon(AppIcons.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun EvidenceDialog(tarea: Tarea, onDismiss: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val fechaHora = tarea.completedAt?.let { dateFormat.format(it) } ?: "Fecha desconocida"

    EnchuDialog(
        onDismiss = onDismiss,
        title = "Tarea Finalizada",
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(tarea.descripcionTarea, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Finalizada el: $fechaHora", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            
            AsyncImage(
                model = tarea.completionImageUrl,
                contentDescription = "Evidencia",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
        }
    }
}
