package com.adrencina.enchu.core.resources

/**
 * Centraliza los textos de la aplicación para facilitar la localización y el mantenimiento.
 */
object AppStrings {

    // Splash
    const val splashLogoDescription = "Logo de la aplicación"
    const val loadingIndicatorDescription = "Cargando contenido"

    // Login
    const val appName = "ENCHU"
    const val loginWithGoogle = "Ingresar con Google"
    const val googleIconDescription = "Ícono de Google"
    const val loginErrorDefault = "Ocurrió un error al iniciar sesión."

    // Home
    const val homeScreenTitle = "Mis obras"
    const val addObra = "Añadir Obra"
    const val search = "Buscar"
    const val moreOptions = "Más opciones"
    const val emptyObrasMessage = "Aún no tenés obras creadas.\n¡Tocá el botón '+' para empezar!"
    const val errorLoadingObras = "Error al cargar las obras: %s"

    // Add Obra Screen
    const val createObraTitle = "Crear Nueva Obra"
    const val save = "GUARDAR"
    const val obraNameLabel = "Nombre de la obra"
    const val obraNamePlaceholder = "Ej: Instalación portero"
    const val clientLabel = "Cliente"
    const val clientPlaceholder = "Ej: Juan Pérez"
    const val descriptionLabel = "Descripción (opcional)"
    const val descriptionPlaceholder = "Ej: Cableado e instalación..."
    const val phoneLabel = "Teléfono (opcional)"
    const val phonePlaceholder = "Ej: 221 3616161"
    const val addressLabel = "Dirección (opcional)"
    const val addressPlaceholder = "Ej: Cabred 1900"
    const val obraStateLabel = "Estado de la obra (opcional)"
    const val stateBudgeted = "Presupuestado"
    const val stateFinished = "Finalizado"
    const val statePaused = "En Pausa"
    const val stateInProgress = "En Progreso"

    // Discard Dialog
    const val discardObraTitle = "¿Descartar Obra?"
    const val discardObraMessage = "Si salís ahora, la información que ingresaste se perderá."
    const val cancel = "CANCELAR"
    const val discard = "DESCARTAR"

    // Snackbar
    const val obraCreatedSuccess = "Obra para %s creada con éxito."
}