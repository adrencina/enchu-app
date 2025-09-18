package com.adrencina.enchu.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.ui.theme.EnchuTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObraCard(
    obra: Obra,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f), // Mantiene la tarjeta cuadrada
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.PaddingMedium)
        ) {
            // Sección de Títulos (ocupa el espacio superior)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    // CAMBIO: El título principal ahora es el nombre del cliente
                    text = obra.clienteNombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    // CAMBIO: El subtítulo es el nombre de la obra
                    text = obra.nombreObra,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Sección inferior (Fecha e Ícono)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    // CAMBIO: Usamos la fecha real del objeto Obra
                    text = obra.fechaCreacion?.toFormattedString() ?: "",
                    style = MaterialTheme.typography.bodySmall,
                )
                // CAMBIO: Añadimos el ícono de galería
                Icon(
                    imageVector = AppIcons.Gallery,
                    contentDescription = "Galería de la obra",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// Función de extensión para formatear la fecha de forma limpia
private fun Date.toFormattedString(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(this)
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ObraCardPreview() {
    EnchuTheme {
        ObraCard(
            obra = Obra(
                id = "1",
                nombreObra = "Ampliación Salón",
                clienteNombre = "ESFORZAR S.A.", // Usamos el campo correcto
                fechaCreacion = Date()
            ),
            onClick = {}
        )
    }
}