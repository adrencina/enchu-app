package com.adrencina.enchu.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.ui.theme.EnchuTheme

/**
 * ADDED: Nuevo componente reutilizable para las secciones del formulario.
 * Muestra un título y luego renderiza el contenido dentro de una Card blanca.
 */
@Composable
fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = null
        ) {
            Column(
                modifier = Modifier.padding(start = 0.dp, top = 4.dp, end = 0.dp, bottom = 8.dp),
                content = content
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun FormSectionPreview() {
    EnchuTheme {
        Column(Modifier.padding(Dimens.PaddingMedium)) {
            FormSection(title = "Nombre de la sección") {
                Text(text = "Aquí dentro va el contenido, como un campo de texto.")
                Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
                Text(text = "Puede ser cualquier componente Composable.")
            }
        }
    }
}