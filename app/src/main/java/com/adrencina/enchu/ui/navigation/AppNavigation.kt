package com.adrencina.enchu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adrencina.enchu.ui.screens.addobra.AddObraScreen
import com.adrencina.enchu.ui.screens.clients.AddClientScreen
import com.adrencina.enchu.ui.screens.clients.ClientDetailScreen
import com.adrencina.enchu.ui.screens.home.HomeScreen
import com.adrencina.enchu.ui.screens.login.LoginScreen
import com.adrencina.enchu.ui.screens.obra_detail.ObraDetailScreen
import android.util.Log
import com.adrencina.enchu.ui.screens.splash.SplashScreen

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel

import com.adrencina.enchu.ui.screens.main.MainScreen

import com.adrencina.enchu.ui.screens.welcome.WelcomeScreen
import com.adrencina.enchu.ui.screens.splash.SplashViewModel
import com.adrencina.enchu.ui.screens.splash.SplashUiEvent
import com.adrencina.enchu.ui.screens.profile.team.TeamScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH_SCREEN
    ) {
        composable(Routes.SPLASH_SCREEN) {
            val splashViewModel = hiltViewModel<SplashViewModel>()
            SplashScreen(viewModel = splashViewModel)
            
            val uiEvent by splashViewModel.uiEvent.collectAsState(initial = null) // Collect as State for LaunchedEffect
            
            LaunchedEffect(uiEvent) {
                when (uiEvent) {
                    SplashUiEvent.NavigateToHome -> {
                        navController.navigate(Routes.MAIN_WRAPPER) {
                            popUpTo(Routes.SPLASH_SCREEN) { inclusive = true }
                        }
                    }
                    SplashUiEvent.NavigateToLogin -> {
                        navController.navigate(Routes.LOGIN_SCREEN) {
                            popUpTo(Routes.SPLASH_SCREEN) { inclusive = true }
                        }
                    }
                    SplashUiEvent.NavigateToWelcome -> {
                        navController.navigate(Routes.WELCOME_SCREEN) {
                            popUpTo(Routes.SPLASH_SCREEN) { inclusive = true }
                        }
                    }
                    else -> {}
                }
            }
        }

        composable(Routes.LOGIN_SCREEN) {
            LoginScreen(
                onLoginSuccess = {
                    // Redirigir temporalmente a WELCOME para validar si necesita crear perfil
                    // La pantalla WelcomeScreen o un ViewModel debería decidir si saltar a Main si ya tiene perfil
                    // Por simplicidad en esta iteración, asumimos que LoginScreen ya hizo su trabajo de autenticación
                    // y aquí decidimos a donde ir.
                    // En un flujo ideal, el Splash ya redirige a Welcome si no hay perfil completo.
                    // Vamos a navegar a Welcome siempre tras Login para chequear.
                    navController.navigate(Routes.WELCOME_SCREEN) {
                        popUpTo(Routes.LOGIN_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.WELCOME_SCREEN) {
            WelcomeScreen(
                onProfileCreated = {
                    navController.navigate(Routes.MAIN_WRAPPER) {
                        popUpTo(Routes.WELCOME_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN_WRAPPER) {
            MainScreen(
                onObraClick = { obraId ->
                    navController.navigate(Routes.createObraDetailRoute(obraId))
                },
                onAddObraClick = {
                    try {
                        navController.navigate(Routes.ADD_OBRA_SCREEN)
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Failed to navigate to AddObra", e)
                    }
                },
                onAddClientClick = {
                    navController.navigate(Routes.ADD_CLIENT_SCREEN)
                },
                onClientClick = { clientId ->
                    navController.navigate(Routes.createClientDetailRoute(clientId))
                },
                onArchivedObrasClick = { navController.navigate(Routes.ARCHIVED_OBRAS_SCREEN) },
                onLogout = {
                    navController.navigate(Routes.LOGIN_SCREEN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToTeamScreen = { // Pass navigation for team screen
                    navController.navigate(Routes.TEAM_SCREEN)
                }
            )
        }

        composable(Routes.TEAM_SCREEN) { // New composable for TeamScreen
            TeamScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_OBRA_SCREEN) {
            AddObraScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateBackWithResult = { clientName ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("new_obra_result", clientName)
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ADD_CLIENT_SCREEN) {
            AddClientScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CLIENT_DETAIL_SCREEN,
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) {
            ClientDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onObraClick = { obraId ->
                    navController.navigate(Routes.createObraDetailRoute(obraId))
                }
            )
        }

        composable(Routes.ARCHIVED_OBRAS_SCREEN) {
            com.adrencina.enchu.ui.screens.archived_obras.ArchivedObrasScreen(
                onNavigateBack = { navController.popBackStack() },
                onObraClick = { obraId ->
                    navController.navigate(Routes.createObraDetailRoute(obraId))
                }
            )
        }

        composable(
            route = Routes.OBRA_DETAIL_SCREEN,
            arguments = listOf(navArgument("obraId") { type = NavType.StringType })
        ) {
            ObraDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}