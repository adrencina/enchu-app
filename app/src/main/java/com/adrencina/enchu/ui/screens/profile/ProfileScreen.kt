package com.adrencina.enchu.ui.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.adrencina.enchu.data.model.Organization
import com.adrencina.enchu.data.repository.ThemeMode
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.viewmodel.ProfileViewModel
import com.adrencina.enchu.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onNavigateToTeamScreen: () -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val context = LocalContext.current

    // Función auxiliar para abrir enlaces
    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Manejar error si no hay navegador (raro)
        }
    }

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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TopAppBar minimalista
        TopAppBar(
            title = { 
                Text(
                    "Menú", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold 
                ) 
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Tarjeta de Perfil Compacta
            ProfileCompactCard(user = uiState.user, organization = uiState.organization)

            // 2. Resumen Rápido (Stats)
            StatsRowCompact(
                obrasCount = uiState.obrasCount,
                clientesCount = uiState.clientesCount
            )

            // 3. Gestión del Negocio
            SettingsSection(title = "NEGOCIO") {
                MenuItem(
                    icon = Icons.Default.Business,
                    text = "Datos de Empresa",
                    onClick = profileViewModel::onEditOrgClick
                )
                MenuItem(
                    icon = Icons.Default.Group,
                    text = "Mi Equipo",
                    onClick = onNavigateToTeamScreen
                )
                // Aquí iría Suscripción en el futuro si se hace clickeable
                MenuItem(
                    icon = if (uiState.organization?.plan == "PRO") Icons.Outlined.Build else Icons.Outlined.Build,
                    text = "Suscripción: ${uiState.organization?.plan ?: "FREE"}",
                    onClick = { /* TODO: Abrir pantalla de suscripción */ },
                    trailing = {
                        Surface(
                            color = if (uiState.organization?.plan == "PRO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (uiState.organization?.plan == "PRO") "PRO" else "FREE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.organization?.plan == "PRO") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                )
            }

            // 4. Configuración App
            SettingsSection(title = "APLICACIÓN") {
                val isDark = themeMode == ThemeMode.DARK
                MenuItem(
                    icon = Icons.Default.DarkMode,
                    text = "Modo Oscuro",
                    onClick = { settingsViewModel.saveThemeMode(if (isDark) ThemeMode.LIGHT else ThemeMode.DARK) },
                    trailing = {
                        Switch(
                            checked = isDark,
                            onCheckedChange = { checked ->
                                settingsViewModel.saveThemeMode(if (checked) ThemeMode.DARK else ThemeMode.LIGHT)
                            },
                            modifier = Modifier.scale(0.8f) // Switch un poco más pequeño
                        )
                    }
                )
            }

            // 5. Legal y Soporte (IMPORTANTE para Play Store)
            SettingsSection(title = "LEGAL") {
                MenuItem(
                    icon = Icons.Default.PrivacyTip,
                    text = "Políticas de Privacidad",
                    onClick = { openUrl("https://gist.githubusercontent.com/adrencina/01be9c72a52a0f980f996dbe99bd7c46/raw/dbbf89917e7828a9f4b4174d348b2c7dbff126cb/politica_privacidad.md") }
                )
                MenuItem(
                    icon = Icons.Default.Gavel, // O Description
                    text = "Términos y Condiciones",
                    onClick = { openUrl("https://gist.githubusercontent.com/adrencina/a5554210b4eb92f61e65da2e0b983bb4/raw/2f12c7bb21df618ab3c0186ce64705c045d12db2/terminos_condiciones.md") }
                )
                MenuItem(
                    icon = Icons.Default.Info,
                    text = "Acerca de Enchu v2.1",
                    onClick = { /* TODO: Mostrar dialogo de versión */ }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Cerrar Sesión (Estilo Botón Peligroso pero elegante)
            OutlinedButton(
                onClick = {
                    profileViewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    containerColor = Color.Transparent
                )
            ) {
                Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileCompactCard(user: FirebaseUser?, organization: Organization?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            val photoUrl = user?.photoUrl
            if (photoUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(photoUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = user?.displayName?.take(1)?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info Texto
            Column {
                Text(
                    text = user?.displayName ?: "Usuario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user?.email ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = organization?.name ?: "Sin Organización",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatsRowCompact(obrasCount: Int, clientesCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Obras
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.Build, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(obrasCount.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Obras", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        // Clientes
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.People, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(clientesCount.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Clientes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp), // Padding ajustado para ser compacto pero tocable
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}