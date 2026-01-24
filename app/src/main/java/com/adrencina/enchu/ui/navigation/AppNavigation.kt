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
import com.adrencina.enchu.ui.screens.new_budget.NewBudgetScreen

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
            
            val uiEvent by splashViewModel.uiEvent.collectAsState(initial = null)
            
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
                onAddBudgetClick = {
                    navController.navigate(Routes.NEW_BUDGET_SCREEN)
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
                onNavigateToTeamScreen = {
                    navController.navigate(Routes.TEAM_SCREEN)
                }
            )
        }

        composable(Routes.NEW_BUDGET_SCREEN) {
            NewBudgetScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TEAM_SCREEN) {
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
