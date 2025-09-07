package com.adrencina.enchu.ui.components

import android.content.res.Configuration
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.core.resources.AppStrings
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.ui.theme.EnchuTheme

@Composable
fun AppGoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = AppStrings.loginWithGoogle
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp) // Altura estándar para botones de acción
            .semantics { contentDescription = text },
        shape = MaterialTheme.shapes.large, // Usando tu Shape.kt
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary, // Tu color Accion
            contentColor = MaterialTheme.colorScheme.onSecondary // Tu color de texto sobre Accion
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = AppIcons.GoogleLogo),
                contentDescription = null, // El botón ya tiene descripción
                modifier = Modifier.size(Dimens.PaddingLarge)
            )
            Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
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