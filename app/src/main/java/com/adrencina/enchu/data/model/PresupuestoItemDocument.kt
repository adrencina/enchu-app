package com.adrencina.enchu.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa un ítem dentro del presupuesto de una obra en Firestore.
 */
data class PresupuestoItemDocument(
    @DocumentId val id: String = "",
    val descripcion: String = "",
    val cantidad: Double = 0.0,
    val precioUnitario: Double = 0.0,
    val tipo: String = "MATERIAL",
    
    // Campos de gestión logística
    val isComprado: Boolean = false,
    val isInstalado: Boolean = false,
    val costoReal: Double? = null,
    
    @ServerTimestamp
    val fechaCreacion: Date? = null
) {
    // Propiedad calculada
    val totalEstimado: Double
        get() = cantidad * precioUnitario

    val totalReal: Double
        get() = cantidad * (costoReal ?: precioUnitario)

    val desvio: Double
        get() = totalEstimado - totalReal
}