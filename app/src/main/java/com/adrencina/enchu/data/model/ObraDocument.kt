package com.adrencina.enchu.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa el modelo de datos para una Obra en Firestore.
 */
data class ObraDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val organizationId: String = "",

    // Datos del Cliente
    val clienteId: String = "",
    val clienteNombre: String = "",
    val clienteCuit: String = "",
    val clientTaxCondition: String = "",

    // Datos de la Obra
    val nombreObra: String = "",
    val descripcion: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val estado: String = "Presupuestado",
    val budgetNumber: Int = 0,
    @get:PropertyName("isArchived")
    val isArchived: Boolean = false,
    
    // Datos del Presupuesto
    val descuento: Double = 0.0,
    val validez: Int = 15,
    val notas: String = "",
    val presupuestoTotal: Double = 0.0,

    @ServerTimestamp val fechaCreacion: Date? = null,
    @ServerTimestamp val lastActivity: Date? = null,
    
    val tareasTotales: Int = 0,
    val tareasCompletadas: Int = 0
)
