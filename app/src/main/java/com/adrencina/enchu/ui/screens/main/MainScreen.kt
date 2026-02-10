package com.adrencina.enchu.ui.screens.main

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.adrencina.enchu.ui.navigation.Routes
import com.adrencina.enchu.ui.screens.clients.ClientsScreen
import com.adrencina.enchu.ui.screens.home.HomeScreen
import com.adrencina.enchu.ui.screens.profile.ProfileScreen
import com.adrencina.enchu.ui.screens.presupuestos.PresupuestosScreen
import android.util.Log
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PostAdd
import androidx.core.content.FileProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onObraClick: (String) -> Unit,
    onObraActionClick: (String, Int) -> Unit,
    onObraAccepted: (String) -> Unit,
    onAddObraClick: () -> Unit,
    onAddBudgetClick: (String?) -> Unit,
    onEditBudgetClick: (String) -> Unit,
    onAddClientClick: () -> Unit,
    onClientClick: (String) -> Unit,
    onArchivedObrasClick: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToTeamScreen: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    budgetTabToOpen: Int? = null,
    onBudgetTabConsumed: () -> Unit = {},
    shouldResetToHome: Boolean = false,
    onResetToHomeConsumed: () -> Unit = {}
) {
    val bottomNavController = rememberNavController()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val newObraResult = navBackStackEntry?.savedStateHandle?.get<String>("new_obra_result")
    val onClearNewObraResult: () -> Unit = {
        navBackStackEntry?.savedStateHandle?.remove<String>("new_obra_result")
    }
    
    LaunchedEffect(budgetTabToOpen) {
        if (budgetTabToOpen != null) {
            bottomNavController.navigate(Routes.PRESUPUESTOS_SCREEN) {
                popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            kotlinx.coroutines.delay(300) 
            onBudgetTabConsumed()
        }
    }
    
    LaunchedEffect(shouldResetToHome) {
        if (shouldResetToHome) {
            bottomNavController.navigate(Routes.HOME_SCREEN) {
                popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            onResetToHomeConsumed()
        }
    }

    Scaffold(
        bottomBar = {
            CustomBottomNavigation(
                currentDestination = currentDestination,
                onNavigate = { route ->
                    bottomNavController.navigate(route) {
                        popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onFabClick = { showBottomSheet = true },
                fabIcon = Icons.Default.Add,
                showFab = true // Siempre visible por ahora
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.HOME_SCREEN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME_SCREEN) {
                HomeScreen(
                    onObraClick = onObraClick,
                    onObraActionClick = onObraActionClick,
                    onAddObraClick = onAddObraClick,
                    onArchivedObrasClick = onArchivedObrasClick,
                    newObraResult = newObraResult,
                    onClearNewObraResult = onClearNewObraResult
                )
            }
            composable(Routes.CLIENTES_SCREEN) {
                ClientsScreen(
                    onAddClientClick = onAddClientClick,
                    onClientClick = onClientClick,
                    onCreateBudgetClick = { clientId -> onAddBudgetClick(clientId) }
                )
            }
            composable(Routes.PRESUPUESTOS_SCREEN) {
                PresupuestosScreen(
                    onNewBudgetClick = { onAddBudgetClick(null) },
                    onEditBudgetClick = onEditBudgetClick,
                    onNavigateToObra = onObraAccepted,
                    initialTab = budgetTabToOpen
                )
            }
            composable(Routes.PROFILE_SCREEN) {
                ProfileScreen(
                    onLogout = onLogout,
                    onNavigateToTeamScreen = onNavigateToTeamScreen,
                    onNavigateToSubscription = onNavigateToSubscription
                )
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 48.dp)
                ) {
                    Text(
                        text = "Acciones Rápidas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 20.dp, start = 8.dp)
                    )
                    
                    // Lógica Condicional de Opciones
                    when (currentRoute) {
                        Routes.CLIENTES_SCREEN -> {
                            BottomSheetMenuItem(
                                title = "Nuevo Cliente",
                                subtitle = "Agrega un contacto al directorio",
                                icon = Icons.Default.PersonAdd,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                onClick = {
                                    showBottomSheet = false
                                    onAddClientClick()
                                }
                            )
                        }
                        Routes.PRESUPUESTOS_SCREEN -> {
                            BottomSheetMenuItem(
                                title = "Nuevo Presupuesto",
                                subtitle = "Crea un borrador detallado",
                                icon = Icons.Default.PostAdd,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                onClick = {
                                    showBottomSheet = false
                                    onAddBudgetClick(null)
                                }
                            )
                        }
                        else -> { // Home y Menú
                            BottomSheetMenuItem(
                                title = "Nuevo Presupuesto",
                                subtitle = "Crea un borrador detallado",
                                icon = Icons.Default.Description,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                onClick = {
                                    showBottomSheet = false
                                    onAddBudgetClick(null)
                                }
                            )
                            
                            Spacer(Modifier.height(8.dp))
                            
                            BottomSheetMenuItem(
                                title = "Nueva Obra",
                                subtitle = "Directo a ejecución",
                                icon = Icons.Default.Home,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                onClick = {
                                    showBottomSheet = false
                                    onAddObraClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomSheetMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(subtitle) },
        leadingContent = { 
            Surface(
                color = containerColor,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun CustomBottomNavigation(
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit,
    onFabClick: () -> Unit,
    fabIcon: ImageVector = Icons.Default.Add,
    showFab: Boolean = true
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp
        ) {
            Column {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                        BottomNavItemComposable(
                            icon = Icons.Default.Home,
                            label = "Inicio",
                            isSelected = currentDestination?.hierarchy?.any { it.route == Routes.HOME_SCREEN } == true,
                            onClick = { onNavigate(Routes.HOME_SCREEN) }
                        )
                        BottomNavItemComposable(
                            icon = Icons.Default.Group,
                            label = "Clientes",
                            isSelected = currentDestination?.hierarchy?.any { it.route == Routes.CLIENTES_SCREEN } == true,
                            onClick = { onNavigate(Routes.CLIENTES_SCREEN) }
                        )
                    }

                    Spacer(modifier = Modifier.width(72.dp))

                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                        BottomNavItemComposable(
                            icon = Icons.Default.Description,
                            label = "Ventas",
                            isSelected = currentDestination?.hierarchy?.any { it.route == Routes.PRESUPUESTOS_SCREEN } == true,
                            onClick = { onNavigate(Routes.PRESUPUESTOS_SCREEN) }
                        )
                        BottomNavItemComposable(
                            icon = Icons.Default.AccountCircle,
                            label = "Menú",
                            isSelected = currentDestination?.hierarchy?.any { it.route == Routes.PROFILE_SCREEN } == true,
                            onClick = { onNavigate(Routes.PROFILE_SCREEN) }
                        )
                    }
                }
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }

        if (showFab) {
            FloatingActionButton(
                onClick = onFabClick,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-12).dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(fabIcon, contentDescription = "Crear", modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun BottomNavItemComposable(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 4.dp), // Reducido de 12.dp a 4.dp
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = color, 
            fontWeight = fontWeight, 
            fontSize = 10.sp,
            maxLines = 1,
            softWrap = false
        )
    }
}
