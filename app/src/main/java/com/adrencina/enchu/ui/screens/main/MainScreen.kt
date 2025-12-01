package com.adrencina.enchu.ui.screens.main

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

@Composable
fun MainScreen(
    onObraClick: (String) -> Unit,
    onAddObraClick: () -> Unit,
    onAddClientClick: () -> Unit,
    onClientClick: (String) -> Unit,
    onArchivedObrasClick: () -> Unit,
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Clientes,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(80.dp),
                containerColor = MaterialTheme.colorScheme.background
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    
                    // Detectar si el botón está siendo presionado
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    
                    // Animación de escala: Crece a 1.2x al presionar, vuelve a 1.0x al soltar
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 1.2f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "iconScale"
                    )

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = null,
                                modifier = Modifier.scale(scale) // Aplicar animación
                            )
                        },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        interactionSource = interactionSource // Pasar source para detectar press
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.HOME_SCREEN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME_SCREEN) {
                HomeScreen(
                    newObraResult = null,
                    onClearNewObraResult = { },
                    onAddObraClick = onAddObraClick,
                    onObraClick = onObraClick,
                    onArchivedObrasClick = onArchivedObrasClick
                )
            }

            composable(Routes.CLIENTES_SCREEN) {
                ClientsScreen(
                    onAddClientClick = onAddClientClick,
                    onClientClick = onClientClick
                )
            }

            composable(Routes.PROFILE_SCREEN) {
                ProfileScreen(onLogout = onLogout)
            }
        }
    }
}