package com.adrencina.enchu.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa un archivo adjunto en una obra.
 * Pertenecerá a la sub-colección "documentos" de una Obra.
 */
data class Documento(
    val nombreArchivo: String = "",
    val urlArchivo: String = "",
    val tamanoArchivo: Long = 0, // En bytes
    @ServerTimestamp
    val fechaSubida: Date? = null
)