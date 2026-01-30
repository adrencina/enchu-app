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
    
    // Campos de gestión logística
    val isComprado: Boolean = false,
    val isInstalado: Boolean = false,
    val costoReal: Double? = null, // Lo que realmente pagó el usuario
    
    @ServerTimestamp
    val fechaCreacion: Date? = null
) {
    // Propiedad calculada para el total estimado (lo que se le cobra al cliente)
    val totalEstimado: Double
        get() = cantidad * precioUnitario

    // Propiedad calculada para el total real (lo que realmente costó)
    val totalReal: Double
        get() = cantidad * (costoReal ?: precioUnitario)

    // El desvío: Negativo es pérdida de ganancia, Positivo es ahorro
    val desvio: Double
        get() = totalEstimado - totalReal

    // Alias para compatibilidad con código existente que use .total
    val total: Double get() = totalEstimado
}
