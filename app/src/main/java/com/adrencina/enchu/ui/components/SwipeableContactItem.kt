package com.adrencina.enchu.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.ui.theme.Dimens
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(MaterialTheme.shapes.medium)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                MaterialTheme.shapes.medium
            )
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Horizontal
            )
            .clickable { onClick() }
    ) {
        // CAPA 1 (Fondo): Botones de Acción
        val areActionsEnabled = state.currentValue == SwipeValue.Open

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(end = Dimens.PaddingMedium),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionButton(
                icon = Icons.Default.DateRange,
                contentDescription = "Agendar",
                tint = MaterialTheme.colorScheme.tertiary,
                onClick = onScheduleClick,
                enabled = areActionsEnabled
            )
            ActionButton(
                icon = Icons.Default.Message,
                contentDescription = "WhatsApp",
                tint = Color(0xFF25D366),
                onClick = onWhatsAppClick,
                enabled = areActionsEnabled
            )
            ActionButton(
                icon = Icons.Default.Call,
                contentDescription = "Llamar",
                tint = MaterialTheme.colorScheme.primary,
                onClick = onCallClick,
                enabled = areActionsEnabled
            )
        }

        // CAPA 2 (La "Tapa" deslizante)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = state.requireOffset().roundToInt(),
                        y = 0
                    )
                }
                .background(MaterialTheme.colorScheme.surface)
        )

        // CAPA 3 (Contenido Estático): Avatar + Nombre
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AVATAR
            val initials = remember(cliente.nombre) {
                cliente.nombre.split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .map { it.first().uppercase() }
                    .joinToString("")
            }
            
            val avatarColor = remember(cliente.nombre) {
                val colors = listOf(
                    Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0),
                    Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF2196F3),
                    Color(0xFF03A9F4), Color(0xFF00BCD4), Color(0xFF009688),
                    Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFFF9800)
                )
                val index = Math.abs(cliente.nombre.hashCode()) % colors.size
                colors[index]
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(avatarColor.copy(alpha = 0.15f))
                    .border(1.dp, avatarColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = avatarColor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(Dimens.PaddingMedium))

            // NOMBRE
            val displayName = remember(cliente.nombre) {
                if (cliente.nombre.length <= 10) {
                    cliente.nombre
                } else {
                    val firstLine = cliente.nombre.take(10)
                    val remaining = cliente.nombre.drop(10)
                    val secondLine = remaining.take(10)
                    val suffix = if (remaining.length > 10) "..." else ""
                    "$firstLine\n$secondLine$suffix"
                }
            }

            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}
