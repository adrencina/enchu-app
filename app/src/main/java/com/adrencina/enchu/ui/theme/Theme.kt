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

// --- Esquemas Stitch ---
private val StitchLightColors = lightColorScheme(
    primary = StitchPrimary,
    onPrimary = Color.White,
    secondary = StitchPrimary, // Usamos el mismo primary como secundario por ahora o Stitch no dio secundario
    onSecondary = Color.White,
    background = StitchBackgroundLight,
    onBackground = StitchTextPrimaryLight,
    surface = StitchSurfaceLight,
    onSurface = StitchTextPrimaryLight,
    onSurfaceVariant = StitchTextSecondaryLight,
    outline = StitchTextSecondaryLight, // Usamos secondary text para bordes
    error = Error,
    onError = OnError,
)

private val StitchDarkColors = darkColorScheme(
    primary = StitchPrimary, // Podríamos necesitar aclarar esto para dark mode, pero probemos el original
    onPrimary = Color.White, // Ojo con contraste en dark mode
    secondary = StitchPrimary,
    onSecondary = Color.White,
    background = StitchBackgroundDark,
    onBackground = StitchTextPrimaryDark,
    surface = StitchSurfaceDark,
    onSurface = StitchTextPrimaryDark,
    onSurfaceVariant = StitchTextSecondaryDark,
    outline = StitchTextSecondaryDark,
    error = ErrorOscuro,
    onError = OnErrorOscuro,
)

@Composable
fun EnchuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Ahora toma del sistema por defecto
    content: @Composable () -> Unit
) {
    // CAMBIO TEMPORAL: Usando paleta Stitch
    val colorScheme = when {
        darkTheme -> StitchDarkColors // Antes: DarkColors
        else -> StitchLightColors     // Antes: LightColors
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