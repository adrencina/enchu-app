package com.adrencina.enchu.ui.screens.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.viewmodel.WelcomeViewModel

@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel = hiltViewModel(),
    onProfileCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isProfileCreated) {
        if (uiState.isProfileCreated) {
            onProfileCreated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Bienvenido a Enchu!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "¿Cómo quieres usar la app?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Option 1: Create
        FilledTonalButton(
            onClick = viewModel::onCreateIndependentProfile,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Soy Dueño / Independiente")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        HorizontalDivider()
        
        Spacer(modifier = Modifier.height(32.dp))

        // Option 2: Join
        Text(
            text = "¿Te invitaron a un equipo?",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = uiState.inviteCode,
            onValueChange = viewModel::onInviteCodeChanged,
            label = { Text("Código de Invitación (ID)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = viewModel::onJoinOrganization,
            enabled = !uiState.isLoading && uiState.inviteCode.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Unirme al Equipo")
            }
        }

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
