package com.adrencina.enchu.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adrencina.enchu.data.model.Obra
import java.time.LocalTime

@Composable
fun DashboardHeader(
    userName: String,
    onNotificationClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val currentHour = LocalTime.now().hour
    val (greeting, emoji) = when (currentHour) {
        in 5..11 -> "¡Buen día" to "☀️"
        in 12..19 -> "¡Buenas tardes" to "👋"
        else -> "¡Buenas noches" to "🌙"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$greeting, $userName! $emoji",
            style = MaterialTheme.typography.titleSmall, // Reducido
            fontWeight = FontWeight.Bold
        )
        
        Row {
            IconButton(onClick = onNotificationClick) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", modifier = Modifier.size(24.dp))
            }
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menú", modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun DashboardSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 2.dp)
            .height(48.dp)
            .clip(CircleShape),
        placeholder = { Text("Buscar...", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        singleLine = true
    )
}

@Composable
fun ActiveWorksRow(
    obras: List<Obra>,
    onClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Mis Obras",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
        )

        if (obras.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp) // Altura fija de tarjeta
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tienes obras en curso",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(obras) { obra ->
                    DashboardObraCard(obra = obra, onClick = { onClick(obra.id) })
                }
            }
        }
    }
}

@Composable
fun DashboardObraCard(obra: Obra, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(140.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Blanco sobre gris
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = obra.nombreObra,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = obra.clienteNombre,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "En curso",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ArchivedWorksPreview(
    obras: List<Obra>,
    onViewAll: () -> Unit,
    onClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Historial", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            TextButton(
                onClick = onViewAll,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.heightIn(max = 32.dp)
            ) {
                Text("Ver todo", style = MaterialTheme.typography.labelSmall)
            }
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(obras.take(5)) { obra ->
                DashboardArchivedCard(obra = obra, onClick = { onClick(obra.id) })
            }
        }
    }
}

@Composable
fun DashboardArchivedCard(obra: Obra, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(120.dp),
        // Color más oscuro/saturado para contrastar con el fondo blanco general, pero diferente a las activas
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), 
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = obra.nombreObra,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Terminada",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ReportsGrid() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text("Informes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportCard(
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF4CAF50),
                label = "Ganancia Real",
                modifier = Modifier.weight(1f)
            )
            ReportCard(
                icon = Icons.Default.Payments,
                color = Color(0xFFFF9800),
                label = "Saldos Pendientes",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportCard(
                icon = Icons.Default.AssignmentLate,
                color = Color(0xFF2196F3),
                label = "¿Presupuesté bien?",
                modifier = Modifier.weight(1f)
            )
            ReportCard(
                icon = Icons.Default.EmojiEvents,
                color = Color(0xFFFFC107),
                label = "Ranking Trabajos",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ReportCard(icon: ImageVector, color: Color, label: String, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.height(70.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}