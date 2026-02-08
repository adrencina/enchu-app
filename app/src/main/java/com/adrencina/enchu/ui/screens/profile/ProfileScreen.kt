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
import com.adrencina.enchu.domain.model.Organization
import com.adrencina.enchu.data.repository.ThemeMode
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.viewmodel.ProfileViewModel
import com.adrencina.enchu.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseUser

import com.adrencina.enchu.domain.model.UserRole

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import com.adrencina.enchu.ui.components.EnchuButton
import com.adrencina.enchu.ui.components.EnchuDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onNavigateToTeamScreen: () -> Unit,
    onNavigateToSubscription: () -> Unit
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
        CenterAlignedTopAppBar(
            title = { 
                Text(
                    "MENÚ", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ) 
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))
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
                    onClick = onNavigateToSubscription,
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

            // 5. DEBUG: Cambiar Rol (Temporal para Pruebas)
            SettingsSection(title = "DEBUG: TESTEO DE ROLES") {
                val currentRole = UserRole.fromValue(uiState.userProfile?.role ?: "OWNER")
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    UserRole.entries.forEach { role ->
                        FilterChip(
                            selected = currentRole == role,
                            onClick = { profileViewModel.onUpdateUserRole(role.value) },
                            label = { Text(role.value) }
                        )
                    }
                }
                Text(
                    "Esta sección es temporal para que puedas probar cómo cambia la Home según el rol.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 6. Legal y Soporte (IMPORTANTE para Play Store)
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

            // 6. Cerrar Sesión Premium
            EnchuButton(
                onClick = {
                    profileViewModel.logout()
                    onLogout()
                },
                text = "Cerrar Sesión",
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.error,
                icon = Icons.AutoMirrored.Outlined.ExitToApp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileCompactCard(user: FirebaseUser?, organization: Organization?) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Premium
            val photoUrl = user?.photoUrl
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (photoUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(photoUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = user?.displayName?.take(1)?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Info Texto
            Column {
                Text(
                    text = user?.displayName ?: "Usuario",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = organization?.name?.uppercase() ?: "SIN ORGANIZACIÓN",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatsRowCompact(obrasCount: Int, clientesCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatItemPremium(
            count = obrasCount, 
            label = "Obras", 
            icon = Icons.Outlined.Build,
            modifier = Modifier.weight(1f)
        )
        StatItemPremium(
            count = clientesCount, 
            label = "Clientes", 
            icon = Icons.Outlined.People,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatItemPremium(count: Int, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), 
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = count.toString(), 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label.uppercase(), 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
        )
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "scale")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}