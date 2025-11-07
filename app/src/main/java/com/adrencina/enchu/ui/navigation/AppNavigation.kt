package com.adrencina.enchu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adrencina.enchu.ui.screens.addobra.AddObraScreen
import com.adrencina.enchu.ui.screens.home.HomeScreen
import com.adrencina.enchu.ui.screens.login.LoginScreen
import com.adrencina.enchu.ui.screens.obra_detail.ObraDetailScreen
import com.adrencina.enchu.ui.screens.splash.SplashScreen

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
                    navController.navigate(Routes.HOME_SCREEN) {
                        popUpTo(Routes.SPLASH_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN_SCREEN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME_SCREEN) {
                        popUpTo(Routes.LOGIN_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME_SCREEN) { backStackEntry ->
            val newObraResult = backStackEntry.savedStateHandle.get<String>("new_obra_result")

            HomeScreen(
                newObraResult = newObraResult,
                onClearNewObraResult = {
                    backStackEntry.savedStateHandle.remove<String>("new_obra_result")
                },
                onAddObraClick = { navController.navigate(Routes.ADD_OBRA_SCREEN) },
                onObraClick = { obraId ->
                    navController.navigate(Routes.createObraDetailRoute(obraId))
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