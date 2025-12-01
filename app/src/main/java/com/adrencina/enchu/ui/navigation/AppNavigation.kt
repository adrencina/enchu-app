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
import com.adrencina.enchu.ui.screens.splash.SplashScreen

import android.util.Log

import com.adrencina.enchu.ui.screens.main.MainScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH_SCREEN
    ) {
        composable(Routes.SPLASH_SCREEN) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN_SCREEN) {
                        popUpTo(Routes.SPLASH_SCREEN) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.MAIN_WRAPPER) {
                        popUpTo(Routes.SPLASH_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN_SCREEN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN_WRAPPER) {
                        popUpTo(Routes.LOGIN_SCREEN) { inclusive = true }
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
                }
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