package com.adrencina.enchu.ui.screens.splash

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.core.resources.AppImages
import com.adrencina.enchu.core.resources.AppStrings
import com.adrencina.enchu.ui.theme.EnchuTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * Composable de pantalla: se conecta al ViewModel y maneja la lógica de navegación.
 */
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    // LaunchedEffect escucha los eventos de un solo uso del ViewModel.
    // `key1 = true` asegura que la corrutina se lance solo una vez.
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SplashUiEvent.NavigateToHome -> onNavigateToHome()
                is SplashUiEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    // Llama al Composable de presentación, que solo renderiza la UI.
    SplashScreenContent()
}

/**
 * Composable de presentación: solo se encarga de mostrar la interfaz visual.
 * No contiene lógica de negocio, es reutilizable y fácil de previsualizar.
 */
@Composable
fun SplashScreenContent(modifier: Modifier = Modifier) {
    Surface(
        // Utiliza los colores del tema, adaptándose a light/dark mode.
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("splash_screen_content"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = AppImages.Logo),
                contentDescription = AppStrings.splashLogoDescription,
                modifier = Modifier
                    .size(Dimens.SplashLogoSize) // Usa dimensiones centralizadas.
                    .semantics { contentDescription = AppStrings.splashLogoDescription },
                // El logo se tiñe con el color primario del tema.
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

            CircularProgressIndicator(
                modifier = Modifier
                    .size(Dimens.ProgressIndicatorSize)
                    .testTag("loading_indicator")
                    .semantics { contentDescription = AppStrings.loadingIndicatorDescription },
                // El indicador usa el color secundario del tema.
                color = MaterialTheme.colorScheme.secondary,
                strokeWidth = Dimens.ProgressIndicatorSize / 12
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SplashScreenPreview() {
    // El Preview ahora usa tu tema para ser 100% fiel a la app real.
    EnchuTheme {
        SplashScreenContent()
    }
}