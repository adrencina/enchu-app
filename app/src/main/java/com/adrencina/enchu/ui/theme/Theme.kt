package com.adrencina.enchu.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
    onSurfaceVariant = TextoGris, // Perfecto para textos secundarios
    outline = BordeGris,
    error = Error,
    onError = OnError,
)

private val DarkColors = darkColorScheme(
    primary = AzulCorporativoOscuro, // Un azul similar pero más vivo en oscuro
    onPrimary = SuperficieNegra,
    secondary = NaranjaAccionOscuro, // Naranja vibrante para el modo oscuro
    onSecondary = SuperficieNegra,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro,
    onSurfaceVariant = TextoGrisOscuro, // Textos secundarios en oscuro
    outline = BordeGrisOscuro,
    error = ErrorOscuro,
    onError = OnErrorOscuro,
)

@Composable
fun EnchuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Ahora toma del sistema por defecto
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColors
        else -> LightColors
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}