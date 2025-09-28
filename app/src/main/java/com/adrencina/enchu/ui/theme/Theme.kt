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
    primary = AzulCorporativo,       // For main titles and icons
    onPrimary = Color.White,
    secondary = NaranjaAccion,       // For FloatingActionButton
    onSecondary = Color.White,       // For icon inside FAB
    background = FondoGrisClaro,     // App background
    onBackground = TextoNegro,       // Main text color
    surface = SuperficieBlanca,      // Card background
    onSurface = TextoNegro,          // Text on cards
    onSurfaceVariant = TextoGris,    // Secondary text (like subtitles)
    outline = BordeGris,             // Card borders
    error = Error,
    onError = OnError,
)

private val DarkColors = darkColorScheme(
    // Keeping dark theme as a fallback, but not the focus
    primary = AzulCorporativo,
    onPrimary = Color.White,
    secondary = NaranjaAccion,
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
    darkTheme: Boolean = false, // Defaulting to light theme as per design
    dynamicColor: Boolean = false,
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
            // Set status bar to match the app background
            window.statusBarColor = colorScheme.background.toArgb()
            // Ensure status bar icons (clock, battery) are dark and visible
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Using the new Typography from Type.kt
        shapes = Shapes,
        content = content
    )
}