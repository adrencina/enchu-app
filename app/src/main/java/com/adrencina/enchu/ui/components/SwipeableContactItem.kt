package com.adrencina.enchu.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adrencina.enchu.data.model.Cliente
import kotlin.math.roundToInt

enum class SwipeValue {
    Closed,
    Open
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableContactItem(
    modifier: Modifier = Modifier,
    state: AnchoredDraggableState<SwipeValue>,
    cliente: Cliente,
    onClick: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onScheduleClick: () -> Unit
) {
    val density = LocalDensity.current
    // Reducimos el ancho para que los iconos estén más juntos
    val actionWidth = 130.dp 
    
    val displayName = remember(cliente.nombre) {
        val cleanName = cliente.nombre.trim()
        if (cleanName.length <= 10) {
            cleanName
        } else {
            val firstLine = cleanName.take(10)
            val remaining = cleanName.drop(10)
            val secondLine = remaining.take(10)
            val suffix = if (remaining.length > 10) "..." else ""
            "$firstLine\n$secondLine$suffix"
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface) // Fondo base igual al frente
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                RoundedCornerShape(16.dp)
            )
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Horizontal
            )
            .clickable { onClick() }
    ) {
        // --- CAPA 3 (FONDO): Botones de Acción ---
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .width(actionWidth)
                .align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionButton(
                icon = Icons.Default.DateRange,
                contentDescription = "Agendar",
                tint = MaterialTheme.colorScheme.tertiary,
                onClick = onScheduleClick
            )
            ActionButton(
                icon = Icons.AutoMirrored.Filled.Message,
                contentDescription = "WhatsApp",
                tint = Color(0xFF25D366),
                onClick = onWhatsAppClick
            )
            ActionButton(
                icon = Icons.Default.Call,
                contentDescription = "Llamar",
                tint = MaterialTheme.colorScheme.primary,
                onClick = onCallClick
            )
        }

        // --- CAPA 2 (INTERMEDIA): Cortina Deslizante ---
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = state.requireOffset().roundToInt(),
                        y = 0
                    )
                },
            color = MaterialTheme.colorScheme.surface // Mismo color exacto
        ) {
            // Capa vacía que tapa los botones
        }

        // --- CAPA 1 (SUPERIOR): Contenido Fijo ---
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = getAvatarColor(cliente.nombre),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = cliente.nombre.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium.copy(
                    lineHeight = 18.sp,
                    fontSize = 15.sp
                ),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp) // Reducido un poco para juntarlos más
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

private fun getAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFE53935),
        Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1)
    )
    val index = kotlin.math.abs(name.hashCode()) % colors.size
    return colors[index]
}
