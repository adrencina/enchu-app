package com.adrencina.enchu.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = AzulCorporativo,
    onPrimary = Color.White,
    secondary = Accion,
    onSecondary = Color.Black,
    background = FondoPrincipal,
    onBackground = TextoPrincipal,
    surface = FondoPrincipal,
    onSurface = TextoPrincipal,
    error = Error,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = AzulCorporativo,
    onPrimary = Color.White,
    secondary = Accion,
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    error = Error,
    onError = Color.White,
)

@Composable
fun EnchuTheme(
    darkTheme: Boolean = false, // si más adelante querés soportar el modo oscuro agregas: isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // si más adelante querés soportar colores dinámicos
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}