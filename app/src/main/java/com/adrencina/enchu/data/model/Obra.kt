package com.adrencina.enchu.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa el modelo de datos para una Obra.
 * @param id El ID único del documento en Firestore. Se anota con @DocumentId para que
 * Firestore lo popule automáticamente al leer datos.
 * @param userId El ID del usuario al que pertenece la obra.
 * @param clienteId El ID del cliente asociado a esta obra.
 * @param nombreObra El nombre descriptivo de la obra.
 * @param descripcion Una descripción más detallada de la obra.
 * @param estado El estado actual de la obra (Ej: "En Progreso", "Finalizado").
 * @param fechaCreacion La fecha en que se creó el documento, generada por el servidor.
 */
data class Obra(
    @DocumentId val id: String = "", // <-- CAMBIO CLAVE: AÑADIMOS EL ID
    val userId: String = "",
    val clienteId: String = "",
    val nombreObra: String = "",
    val descripcion: String = "",
    val estado: String = "En Progreso",
    @ServerTimestamp val fechaCreacion: Date? = null
)