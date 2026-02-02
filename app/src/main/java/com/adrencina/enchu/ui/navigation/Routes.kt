package com.adrencina.enchu.ui.navigation

/**
 * Objeto que contiene todas las rutas de navegación de la aplicación como constantes.
 * Usar un objeto como este previene errores de tipeo al navegar.
 */
object Routes {
    const val SPLASH_SCREEN = "splash_screen"
    const val LOGIN_SCREEN = "login_screen"
    const val WELCOME_SCREEN = "welcome_screen" // New route

    // Rutas para la navegación principal
    const val MAIN_WRAPPER = "main_wrapper"
    const val HOME_SCREEN = "home_screen"
    const val CLIENTES_SCREEN = "clientes_screen"
    const val PROFILE_SCREEN = "profile_screen"
    const val TEAM_SCREEN = "team_screen"
    const val SUBSCRIPTION_SCREEN = "subscription_screen"
    const val PRESUPUESTOS_SCREEN = "presupuestos_screen"
    const val NEW_BUDGET_SCREEN = "new_budget_screen?budgetId={budgetId}&clientId={clientId}"

    fun createNewBudgetRoute(budgetId: String? = null, clientId: String? = null): String {
        val base = "new_budget_screen"
        val params = mutableListOf<String>()
        if (budgetId != null) params.add("budgetId=$budgetId")
        if (clientId != null) params.add("clientId=$clientId")
        
        return if (params.isEmpty()) {
            base
        } else {
            "$base?${params.joinToString("&")}"
        }
    }

    // Rutas para futuras pantallas
    const val ADD_OBRA_SCREEN = "add_obra_screen"
    const val ADD_CLIENT_SCREEN = "add_client_screen"
    const val CLIENT_DETAIL_SCREEN = "client_detail_screen/{clientId}"
    const val ARCHIVED_OBRAS_SCREEN = "archived_obras_screen"

    // Ruta con un argumento para el ID de la obra
    const val OBRA_DETAIL_SCREEN = "obra_detail_screen/{obraId}"

    /**
     * Función helper para construir la ruta al detalle de una obra.
     * Esto asegura que la ruta se construya siempre de la misma manera.
     * Uso: Routes.createObraDetailRoute(obra.id)
     */
    fun createObraDetailRoute(obraId: String): String {
        return try {
            val encodedId = java.net.URLEncoder.encode(obraId, "UTF-8")
            "obra_detail_screen/$encodedId"
        } catch (e: Exception) {
            "obra_detail_screen/$obraId" // Fallback
        }
    }

    fun createClientDetailRoute(clientId: String) = "client_detail_screen/$clientId"
}