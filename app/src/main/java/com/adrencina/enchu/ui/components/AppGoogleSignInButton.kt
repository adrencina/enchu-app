package com.adrencina.enchu.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.core.resources.AppStrings
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.ui.theme.EnchuTheme

import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AppGoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    text: String = "Continuar con Google"
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    val containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val contentColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF1F1F1F)
    val borderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color(0xFF747775)

    Button(
        onClick = { if (!isLoading) onClick() },
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics { contentDescription = text },
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Contenido principal (Logo + Texto)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (isLoading) 0.1f else 1f), // Se desvanece casi por completo al cargar
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start // Logo a la izquierda, texto centrado
            ) {
                Image(
                    painter = painterResource(id = AppIcons.GoogleLogo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f) // Centra el texto en el espacio restante
                )
            }

            // Spinner miniatura superpuesto
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.secondary // Naranja de acción
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppGoogleSignInButtonPreview() {
    EnchuTheme {
        Surface(modifier = Modifier.padding(Dimens.PaddingMedium)) {
            AppGoogleSignInButton(onClick = {})
        }
    }
}