package com.adrencina.enchu.domain.model

import java.util.Date

data class MemberPermissions(
    val canEditTasks: Boolean = true,
    val canAddAvances: Boolean = true,
    val canViewFiles: Boolean = true,
    val canAddFiles: Boolean = false,
    val canViewFinances: Boolean = false
)

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
    val direccion: String = "",
    val telefono: String = "",
    val estado: EstadoObra = EstadoObra.PRESUPUESTADO,
    val budgetNumber: Int = 0,
    val isArchived: Boolean = false,
    val presupuestoTotal: Double = 0.0,
    val descuento: Double = 0.0,
    val validez: Int = 15,
    val notas: String = "",
    val fechaCreacion: Date = Date(),
    val lastActivity: Date = Date(),
    val tareasTotales: Int = 0,
    val tareasCompletadas: Int = 0,
    val assignedMemberIds: List<String> = emptyList(),
    val memberPermissions: Map<String, MemberPermissions> = emptyMap()
)
