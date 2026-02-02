package com.adrencina.enchu.domain.model

import java.util.Date

data class Movimiento(
    val id: String = "",
    val userId: String = "",
    val organizationId: String = "",
    val obraId: String = "",
    val descripcion: String = "",
    val monto: Double = 0.0,
    val tipo: String = "INGRESO",
    val categoria: String = "OTRO",
    val fecha: Date? = null
)