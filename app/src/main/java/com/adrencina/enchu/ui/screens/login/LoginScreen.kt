package com.adrencina.enchu.ui.screens.login

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.R
import com.adrencina.enchu.core.resources.AppImages
import com.adrencina.enchu.core.resources.AppStrings
import com.adrencina.enchu.ui.components.AppGoogleSignInButton
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.ui.theme.EnchuTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

/**
 * Composable de pantalla: maneja la lógica, el estado y la interacción con el ViewModel.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val signInState by viewModel.signInState.collectAsState()
    val context = LocalContext.current
    val webClientId = stringResource(id = R.string.default_web_client_id)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                viewModel.onSignInResult(account.idToken)
            } catch (e: ApiException) {
                viewModel.onSignInResult(null)
            }
        }
    )

    fun onGoogleSignInClick() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        launcher.launch(googleSignInClient.signInIntent)
    }

    LaunchedEffect(key1 = signInState.isSignInSuccessful) {
        if (signInState.isSignInSuccessful) {
            onLoginSuccess()
        }
    }

    LoginScreenContent(
        error = signInState.signInError,
        onSignInClick = ::onGoogleSignInClick
    )
}

/**
 * Composable de presentación: solo muestra la UI basada en los parámetros recibidos.
 */
@Composable
fun LoginScreenContent(
    modifier: Modifier = Modifier,
    error: String?,
    onSignInClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.PaddingLarge)
                .testTag("login_screen_content"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = AppImages.Logo),
                contentDescription = AppStrings.splashLogoDescription,
                modifier = Modifier
                    .size(Dimens.LoginLogoSize)
                    .semantics { contentDescription = AppStrings.splashLogoDescription }
            )

            Spacer(modifier = Modifier.height(Dimens.PaddingLarge))

            Text(
                text = AppStrings.appName,
                style = MaterialTheme.typography.displaySmall, // De tu Typography.kt
                color = MaterialTheme.colorScheme.primary // De tu Color.kt
            )

            Spacer(modifier = Modifier.height(Dimens.PaddingLarge))

            AppGoogleSignInButton(onClick = onSignInClick)

            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Preview con parámetros para ver diferentes estados de la UI
class LoginScreenPreviewParameterProvider : PreviewParameterProvider<String?> {
    override val values = sequenceOf(
        null, // Sin error
        "No se pudo obtener el token de Google." // Con error
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Compact Screen", widthDp = 320, heightDp = 640)
@Composable
private fun LoginScreenContentPreview(
    @PreviewParameter(LoginScreenPreviewParameterProvider::class) error: String?
) {
    EnchuTheme {
        LoginScreenContent(error = error, onSignInClick = {})
    }
}