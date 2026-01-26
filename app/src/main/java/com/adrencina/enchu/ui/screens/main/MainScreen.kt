package com.adrencina.enchu.ui.screens.main

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.adrencina.enchu.ui.navigation.BottomNavItem
import com.adrencina.enchu.ui.navigation.Routes
import com.adrencina.enchu.ui.screens.clients.ClientsScreen
import com.adrencina.enchu.ui.screens.home.HomeScreen
import com.adrencina.enchu.ui.screens.profile.ProfileScreen
import com.adrencina.enchu.ui.screens.presupuestos.PresupuestosScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onObraClick: (String) -> Unit,
    onAddObraClick: () -> Unit,
    onAddBudgetClick: () -> Unit,
    onEditBudgetClick: (String) -> Unit,
    onAddClientClick: () -> Unit,
    onClientClick: (String) -> Unit,
    onArchivedObrasClick: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToTeamScreen: () -> Unit,
    budgetTabToOpen: Int? = null
) {
    val bottomNavController = rememberNavController()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Recuperar el resultado de la navegación
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
                onFabClick = {
                    when (currentDestination?.route) {
                        Routes.PRESUPUESTOS_SCREEN -> onAddBudgetClick()
                        Routes.CLIENTES_SCREEN -> onAddClientClick()
                        else -> showBottomSheet = true
                    }
                }
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
                    onAddObraClick = onAddObraClick,
                    onArchivedObrasClick = onArchivedObrasClick,
                    newObraResult = newObraResult,
                    onClearNewObraResult = onClearNewObraResult
                )
            }
            composable(Routes.CLIENTES_SCREEN) {
                ClientsScreen(
                    onAddClientClick = onAddClientClick,
                    onClientClick = onClientClick
                )
            }
            composable(Routes.PRESUPUESTOS_SCREEN) {
                PresupuestosScreen(
                    onNewBudgetClick = onAddBudgetClick,
                    onEditBudgetClick = onEditBudgetClick,
                    initialTab = budgetTabToOpen
                )
            }
            composable(Routes.PROFILE_SCREEN) {
                ProfileScreen(
                    onLogout = onLogout,
                    onNavigateToTeamScreen = onNavigateToTeamScreen
                )
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = "Crear",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    ListItem(
                        headlineContent = { Text("Nuevo Presupuesto") },
                        supportingContent = { Text("Crea un borrador detallado") },
                        leadingContent = { 
                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary) 
                        },
                        modifier = Modifier.clickable {
                            showBottomSheet = false
                            onAddBudgetClick()
                        }
                    )
                    
                    ListItem(
                        headlineContent = { Text("Nueva Obra") },
                        supportingContent = { Text("Directo a ejecución") },
                        leadingContent = { 
                            Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary) 
                        },
                        modifier = Modifier.clickable {
                            showBottomSheet = false
                            onAddObraClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBottomNavigation(
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit,
    onFabClick: () -> Unit
) {
    // Definimos los items y sus etiquetas
    val leftItems = listOf(
        BottomNavItem.Home to "Obras",
        BottomNavItem.Clientes to "Clientes"
    )
    val rightItems = listOf(
        BottomNavItem.Presupuestos to "Presupuesto",
        BottomNavItem.Profile to "Perfil"
    )

    // Colores basados en el tema de la aplicación
    val barBackgroundColor = MaterialTheme.colorScheme.surface
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. La Barra de Navegación
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            color = barBackgroundColor,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp), // Reducido a 12dp
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Grupo Izquierdo
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    leftItems.forEach { (screen, label) ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        CustomBottomNavItem(
                            icon = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            label = label,
                            isSelected = isSelected,
                            activeColor = activeColor,
                            inactiveColor = inactiveColor,
                            onClick = { onNavigate(screen.route) }
                        )
                    }
                }

                // Espaciador central para el FAB
                Spacer(modifier = Modifier.width(70.dp))

                // Grupo Derecho
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rightItems.forEach { (screen, label) ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        CustomBottomNavItem(
                            icon = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            label = label,
                            isSelected = isSelected,
                            activeColor = activeColor,
                            inactiveColor = inactiveColor,
                            onClick = { onNavigate(screen.route) }
                        )
                    }
                }
            }
        }

        // 2. El FAB Flotante
        FloatingActionButton(
            onClick = onFabClick,
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(55.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-15).dp) // Bajado otros 4dp (-19 + 4 = -15)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Crear",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun CustomBottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(4.dp) // Reducido padding interno
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp) // Reducido a 24dp
        )
        Spacer(modifier = Modifier.height(2.dp)) // Reducido espacio vertical
        Text(
            text = label,
            fontSize = 10.sp, // Reducido a 10sp para evitar saltos de línea
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) activeColor else inactiveColor,
            maxLines = 1 // Asegurar una sola línea
        )
    }
}
