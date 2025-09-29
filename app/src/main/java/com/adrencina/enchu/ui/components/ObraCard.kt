package com.adrencina.enchu.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
        modifier = modifier
            .height(130.dp) // Altura fija como en el diseño
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // Bordes redondeados
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface, // Fondo blanco
        ),
        // CAMBIO: Añadimos el borde sutil
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.PaddingMedium),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Sección de Títulos
            Column {
                Text(
                    text = obra.clienteNombre.uppercase(), // Mayúsculas como en el diseño
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = obra.nombreObra,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Texto gris
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Sección inferior
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = obra.fechaCreacion?.toFormattedString() ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    painter = painterResource(id = AppIcons.Gallery), // Usamos el nuevo ícono
                    contentDescription = "Galería de la obra",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ObraCardPreview() {
    EnchuTheme {
        // The background color of the preview should be the app's background
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(Dimens.PaddingMedium)) {
                ObraCard(
                    obra = Obra(
                        id = "preview-1",
                        nombreObra = "Ampliación Salón Principal",
                        clienteNombre = "ESFORZAR S.A.",
                        fechaCreacion = Date()
                    ),
                    onClick = {}
                )
            }
        }
    }
}