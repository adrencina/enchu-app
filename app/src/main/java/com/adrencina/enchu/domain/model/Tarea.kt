package com.adrencina.enchu.domain.model

import java.util.Date

data class Tarea(
    val id: String = "",
    val userId: String = "",
    val organizationId: String = "",
    val descripcionTarea: String = "",
    val completada: Boolean = false,
    val fechaCreacion: Date? = null,
    val fechaVencimiento: Date? = null,
    
    val completedByUserId: String? = null,
    val completedAt: Date? = null,
    val completionImageUrl: String? = null
)
