package com.adrencina.enchu.ui.screens.obra_detail.tareas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.data.model.Tarea
import com.adrencina.enchu.ui.theme.Dimens

@Composable
fun TareasScreen(
    tareas: List<Tarea>,
    onAddTarea: (String) -> Unit,
    onToggleTarea: (Tarea) -> Unit,
    onDeleteTarea: (Tarea) -> Unit
) {
    var newTaskText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.PaddingMedium)
    ) {
        // Input Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.PaddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTaskText,
                onValueChange = { newTaskText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nueva tarea (ej: Comprar material)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (newTaskText.isNotBlank()) {
                            onAddTarea(newTaskText)
                            newTaskText = ""
                        }
                    }
                )
            )
            Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
            FilledIconButton(
                onClick = {
                    if (newTaskText.isNotBlank()) {
                        onAddTarea(newTaskText)
                        newTaskText = ""
                    }
                }
            ) {
                Icon(AppIcons.Add, contentDescription = "Agregar tarea")
            }
        }

        // List Area
        if (tareas.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay tareas pendientes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = Dimens.PaddingLarge)
            ) {
                items(tareas, key = { it.id }) { tarea ->
                    TareaItem(
                        tarea = tarea,
                        onToggle = { onToggleTarea(tarea) },
                        onDelete = { onDeleteTarea(tarea) }
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
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.small,
        tonalElevation = if (tarea.completada) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = { onToggle() }
            )
            Text(
                text = tarea.descripcionTarea,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = if (tarea.completada) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (tarea.completada) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    AppIcons.Delete,
                    contentDescription = "Eliminar tarea",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
