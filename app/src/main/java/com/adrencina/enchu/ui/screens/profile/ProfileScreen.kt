package com.adrencina.enchu.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.adrencina.enchu.data.model.Organization
import com.adrencina.enchu.data.repository.ThemeMode
import com.adrencina.enchu.ui.screens.profile.EditOrganizationDialog
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.viewmodel.ProfileViewModel
import com.adrencina.enchu.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseUser
import androidx.compose.foundation.clickable

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onNavigateToTeamScreen: () -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val themeMode by settingsViewModel.themeMode.collectAsState()

    if (uiState.showEditOrgDialog && uiState.organization != null) {
        EditOrganizationDialog(
            organization = uiState.organization!!,
            onDismiss = profileViewModel::onDismissEditOrgDialog,
            onConfirm = profileViewModel::onUpdateOrganization,
            onLogoSelected = profileViewModel::onLogoSelected
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.PaddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingLarge)
    ) {
        // 1. Header
        ProfileHeader(user = uiState.user)

        // 2. Stats
        StatsSection(
            obrasCount = uiState.obrasCount,
            clientesCount = uiState.clientesCount
        )

        // 3. Menu Options
        MenuSection(
            themeMode = themeMode,
            onThemeModeChange = settingsViewModel::saveThemeMode,
            onEditOrgClick = profileViewModel::onEditOrgClick,
            onManageTeamClick = onNavigateToTeamScreen, // Pass new callback
            onLogout = {
                profileViewModel.logout()
                onLogout()
            }
        )
    }
}

@Composable
fun ProfileHeader(user: FirebaseUser?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val photoUrl = user?.photoUrl

        if (photoUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(photoUrl),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.displayName?.take(1)?.uppercase() ?: "U",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

        Text(
            text = user?.displayName ?: "Usuario",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = user?.email ?: "correo@ejemplo.com",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatsSection(obrasCount: Int, clientesCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
    ) {
        StatCard(
            label = "Obras",
            value = obrasCount.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Clientes",
            value = clientesCount.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.PaddingMedium)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MenuSection(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onEditOrgClick: () -> Unit,
    onManageTeamClick: () -> Unit, // New callback
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = Dimens.PaddingSmall)
        )

        MenuItem(
            icon = Icons.Default.Business,
            text = "Datos de Empresa y Logo",
            onClick = onEditOrgClick
        )
        MenuItem(
            icon = Icons.Default.Group, // New icon for team
            text = "Mi Equipo",
            onClick = onManageTeamClick
        )
        MenuItem(
            icon = Icons.Default.Settings,
            text = "Ajustes de Aplicación",
            onClick = { /* TODO */ }
        )

        Spacer(modifier = Modifier.height(Dimens.PaddingMedium)) // Space for theme controls

        Text(
            text = "Modo de Tema",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = Dimens.PaddingSmall)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = themeMode == mode,
                    onClick = { onThemeModeChange(mode) },
                    label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercaseChar() }) },
                    shape = MaterialTheme.shapes.small,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        MenuItem(
            icon = Icons.Default.Info,
            text = "Acerca de Enchu v1.0",
            onClick = { /* TODO */ }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.PaddingMedium))

        MenuItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            text = "Cerrar Sesión",
            onClick = onLogout
        )
    }
}

@Composable
fun MenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(text) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )
}
