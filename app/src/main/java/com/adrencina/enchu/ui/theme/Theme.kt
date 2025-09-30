package com.adrencina.enchu.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = AzulCorporativo,
    onPrimary = SuperficieBlanca,
    secondary = NaranjaAccion,
    onSecondary = SuperficieBlanca,
    background = FondoGrisClaro,
    onBackground = TextoNegro,
    surface = SuperficieBlanca,
    onSurface = TextoNegro,
    onSurfaceVariant = TextoGris,
    outline = BordeGris,
    error = Error,
    onError = OnError,
)

@Composable
fun EnchuTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}