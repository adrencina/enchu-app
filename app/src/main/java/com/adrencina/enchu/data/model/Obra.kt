package com.adrencina.enchu.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa el modelo de datos para una Obra.
 * Incluye datos denormalizados (clienteNombre) para optimizar lecturas.
 */
data class Obra(
    @DocumentId val id: String = "",
    val userId: String = "",

    // Datos del Cliente
    val clienteId: String = "",
    val clienteNombre: String = "",

    // Datos de la Obra
    val nombreObra: String = "",
    val descripcion: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val estado: String = "Presupuestado",

    @ServerTimestamp val fechaCreacion: Date? = null
)