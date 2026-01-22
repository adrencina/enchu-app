package com.adrencina.enchu.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.ui.theme.EnchuTheme
import java.text.SimpleDateFormat
import java.util.*

private val obraDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObraCard(
    obra: Obra,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Parte Superior: Nombre del Cliente y de la Obra
            Column {
                Text(
                    text = obra.clienteNombre,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp, // Un poquito más grande al no haber icono
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Nombre de la obra
                Text(
                    text = obra.nombreObra,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, // Volvemos a permitir 2 líneas para la obra también
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            // Parte Inferior: Fecha (Izquierda) e Indicador Circular (Derecha)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = obra.fechaCreacion?.let { obraDateFormatter.format(it) } ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                
                EstadoIndicator(estado = obra.estado)
            }
        }
    }
}

@Composable
private fun EstadoIndicator(estado: String) {
    val color = when (estado) {
        "Finalizado" -> Color(0xFF1E8E3E)
        "En Progreso" -> Color(0xFF1967D2)
        "En Pausa" -> Color(0xFFF9AB00)
        else -> Color(0xFF5F6368)
    }

    Box(
        modifier = Modifier
            .size(10.dp) // Un poco más pequeño y sutil
            .background(color = color, shape = CircleShape)
    )
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ObraCardPreview() {
    EnchuTheme {
        ObraCard(
            obra = Obra(
                id = "preview-id",
                nombreObra = "Instalación de Cámaras de Seguridad en el perímetro central del edificio",
                clienteNombre = "Consorcio Edificio Plaza de la República Central",
                fechaCreacion = Date(),
                estado = "Finalizado"
            ),
            onClick = {}
        )
    }
}