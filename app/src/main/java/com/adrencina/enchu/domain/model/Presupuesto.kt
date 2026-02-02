package com.adrencina.enchu.domain.model

data class Presupuesto(
    val id: String,
    val titulo: String,
    val clienteId: String,
    val clienteNombre: String,
    val clienteApellido: String,
    val clienteDireccion: String,
    val clienteTelefono: String,
    val clienteEmail: String,
    val items: List<PresupuestoItem>, // Lista completa de items
    val subtotal: Double,
    val impuestos: Double,
    val descuento: Double,
    val total: Double,
    val estado: String, // Podría migrarse a Enum EstadoPresupuesto
    val creadoEn: Long,
    val aprobadoEn: Long?,
    val aprobadoPor: String?,
    val notas: String,
    val validez: Int,
    val numero: Int
)
