package com.adrencina.enchu.domain.model

import java.util.Date

data class Obra(
    val id: String = "",
    val userId: String = "",
    val organizationId: String = "",
    val clienteId: String = "",
    val clienteNombre: String = "",
    val clienteCuit: String = "",
    val clientTaxCondition: String = "",
    val nombreObra: String = "",
    val descripcion: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val estado: EstadoObra = EstadoObra.PRESUPUESTADO,
    val budgetNumber: Int = 0,
    val isArchived: Boolean = false,
    val descuento: Double = 0.0,
    val validez: Int = 15,
    val notas: String = "",
    val presupuestoTotal: Double = 0.0,
    val fechaCreacion: Date? = null,
    val tareasTotales: Int = 0,
    val tareasCompletadas: Int = 0,
    // Future-proofing fields
    val assignedMemberIds: List<String> = emptyList(), // Para asignar empleados específicos
    val lastActivity: Date? = null // Para ordenar por "Último Toque"
)