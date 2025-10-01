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
            .height(130.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.PaddingMedium),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = obra.clienteNombre,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = obra.nombreObra,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = obra.fechaCreacion?.let { obraDateFormatter.format(it) } ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    painter = painterResource(id = AppIcons.Gallery),
                    contentDescription = "Galería de la obra",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

 @Preview(showBackground = true)
 @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
 @Composable
fun ObraCardPreview() {
    EnchuTheme {
        ObraCard(
            obra = Obra(
                id = "preview-id",
                nombreObra = "Reforma integral de un baño moderno",
                clienteNombre = "Constructora Acme",
                fechaCreacion = Date()
            ),
            onClick = {}
        )
    }
}
