package com.adrencina.enchu.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa una tarea en la checklist de una obra.
 * Pertenecerá a la sub-colección "tareas" de una Obra.
 */
data class TareaDocument(
    @DocumentId val id: String = "",
    val userId: String = "", // Creador
    val organizationId: String = "",
    val descripcionTarea: String = "",
    val completada: Boolean = false,
    @ServerTimestamp
    val fechaCreacion: Date? = null,
    val fechaVencimiento: Date? = null,
    
    // Trazabilidad de ejecución
    val completedByUserId: String? = null,
    val completedAt: Date? = null,
    val completionImageUrl: String? = null
)
