package com.adrencina.enchu.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "presupuestos")
data class PresupuestoEntity(
    @PrimaryKey val id: String, // UUID generado al crear
    val titulo: String,
    val clienteId: String,
    val clienteNombre: String,
    val clienteApellido: String,
    val clienteDireccion: String,
    val clienteTelefono: String,
    val clienteEmail: String,
    val subtotal: Double = 0.0,
    val impuestos: Double = 0.0,
    val descuento: Double = 0.0,
    val total: Double = 0.0,
    val estado: String = "PENDIENTE", // PENDIENTE, APROBADO, CANCELADO
    val creadoEn: Long = System.currentTimeMillis(),
    val aprobadoEn: Long? = null,
    val aprobadoPor: String? = null,
    val notas: String = "",
    val validez: Int = 15,
    val numero: Int = 0 // Número correlativo (Talonario)
)
