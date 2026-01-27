package com.adrencina.enchu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
        composable(
            route = Routes.SPLASH_SCREEN,
            exitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
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

        composable(
            route = Routes.LOGIN_SCREEN,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.MAIN_WRAPPER) {
                        popUpTo(Routes.LOGIN_SCREEN) { inclusive = true }
                    }
                },
                onNavigateToWelcome = {
                    navController.navigate(Routes.WELCOME_SCREEN) {
                        popUpTo(Routes.LOGIN_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.WELCOME_SCREEN,
            enterTransition = { 
                fadeIn(animationSpec = tween(400)) + 
                scaleIn(initialScale = 0.95f, animationSpec = tween(400)) 
            },
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            WelcomeScreen(
                onProfileCreated = {
                    navController.navigate(Routes.MAIN_WRAPPER) {
                        popUpTo(Routes.WELCOME_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.MAIN_WRAPPER,
            enterTransition = { 
                fadeIn(animationSpec = tween(400)) + 
                scaleIn(initialScale = 0.95f, animationSpec = tween(400)) 
            }
        ) { backStackEntry ->
            val targetTab = backStackEntry.savedStateHandle.get<Int>("target_tab")
            if (targetTab != null) {
                backStackEntry.savedStateHandle.remove<Int>("target_tab")
            }
            
            val profileViewModel: com.adrencina.enchu.viewmodel.ProfileViewModel = hiltViewModel()

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
                    navController.navigate(Routes.createNewBudgetRoute())
                },
                onEditBudgetClick = { budgetId ->
                    navController.navigate(Routes.createNewBudgetRoute(budgetId))
                },
                onAddClientClick = {
                    navController.navigate(Routes.ADD_CLIENT_SCREEN)
                },
                onClientClick = { clientId ->
                    navController.navigate(Routes.createClientDetailRoute(clientId))
                },
                onArchivedObrasClick = {
                    navController.navigate(Routes.ARCHIVED_OBRAS_SCREEN)
                },
                onLogout = {
                    // El logout ya se realizó en ProfileViewModel. Solo navegamos.
                    navController.navigate(Routes.LOGIN_SCREEN) {
                        popUpTo(0)
                    }
                },
                onNavigateToTeamScreen = {
                    navController.navigate(Routes.TEAM_SCREEN)
                },
                budgetTabToOpen = targetTab
            )
        }

        composable(
            route = Routes.NEW_BUDGET_SCREEN,
            arguments = listOf(
                navArgument("budgetId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            NewBudgetScreen(
                onNavigateBack = { navController.popBackStack() },
                onBudgetSaved = { isSent ->
                    val tabIndex = if (isSent) 1 else 0
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("target_tab", tabIndex)
                    navController.popBackStack()
                }
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
