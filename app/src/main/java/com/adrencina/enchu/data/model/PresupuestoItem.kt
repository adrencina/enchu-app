package com.adrencina.enchu.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa un ítem dentro del presupuesto de una obra.
 * Puede ser un material o mano de obra.
 */
data class PresupuestoItem(
    @DocumentId val id: String = "",
    val descripcion: String = "",
    val cantidad: Double = 0.0,
    val precioUnitario: Double = 0.0,
    val tipo: String = "MATERIAL", // "MATERIAL" o "MANO_DE_OBRA"
    @ServerTimestamp
    val fechaCreacion: Date? = null
) {
    // Propiedad calculada para el total del ítem (no se persiste, se calcula)
    val total: Double
        get() = cantidad * precioUnitario
}
