package com.adrencina.enchu.ui.navigation

/**
 * Objeto que contiene todas las rutas de navegación de la aplicación como constantes.
 * Usar un objeto como este previene errores de tipeo al navegar.
 */
object Routes {
    const val SPLASH_SCREEN = "splash_screen"
    const val LOGIN_SCREEN = "login_screen"

    // Rutas para la navegación principal
    const val MAIN_WRAPPER = "main_wrapper"
    const val HOME_SCREEN = "home_screen"
    const val CLIENTES_SCREEN = "clientes_screen"
    const val PROFILE_SCREEN = "profile_screen"

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
    fun createObraDetailRoute(obraId: String) = "obra_detail_screen/$obraId"

    fun createClientDetailRoute(clientId: String) = "client_detail_screen/$clientId"
}