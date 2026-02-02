package com.adrencina.enchu.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa un avance o registro fotográfico dentro de una obra.
 * Pertenecerá a la sub-colección "avances" de una Obra.
 */
data class AvanceDocument(
    @DocumentId val id: String = "",
    val descripcion: String = "",
    val fotosUrls: List<String> = emptyList(),
    @ServerTimestamp
    val fecha: Date? = null
)
