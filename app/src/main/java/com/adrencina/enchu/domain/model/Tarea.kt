package com.adrencina.enchu.domain.model

import java.util.Date

data class Tarea(
    val id: String = "",
    val descripcionTarea: String = "",
    val completada: Boolean = false,
    val fechaCreacion: Date? = null,
    val fechaVencimiento: Date? = null
)