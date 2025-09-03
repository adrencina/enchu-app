package com.adrencina.enchu.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa una obra en la base de datos (colección "obras").
 * El ID del documento será el obraId.
 */
data class Obra(
    val userId: String = "",
    val clienteId: String = "",
    val nombreObra: String = "",
    val descripcion: String = "",
    val direccionObra: String = "",
    val estado: String = "En Progreso",
    val fotoPortadaUrl: String = "",
    @ServerTimestamp
    val fechaInicio: Date? = null
)