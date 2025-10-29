package com.adrencina.enchu.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa a un cliente en la base de datos (colección "clientes").
 */
data class Cliente(
    @DocumentId val id: String = "",
    val userId: String = "",
    val nombre: String = "",
    val dni: String = "",
    val telefono: String = "",
    val email: String = "",
    val direccion: String = "",
    @ServerTimestamp
    val fechaCreacion: Date? = null
)