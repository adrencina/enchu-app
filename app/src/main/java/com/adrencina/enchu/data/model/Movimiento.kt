package com.adrencina.enchu.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa un movimiento de dinero dentro de una obra.
 */
data class Movimiento(
    @DocumentId val id: String = "",
    val obraId: String = "",
    val descripcion: String = "",
    val monto: Double = 0.0,
    val tipo: String = "INGRESO", // "INGRESO" (Cobro) o "EGRESO" (Gasto)
    val categoria: String = "OTRO", // "PAGO_CLIENTE", "MATERIALES", "HERRAMIENTAS", "VIATICOS", etc.
    @ServerTimestamp
    val fecha: Date? = null
)
