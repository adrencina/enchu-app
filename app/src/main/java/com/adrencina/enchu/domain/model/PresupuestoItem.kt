package com.adrencina.enchu.domain.model

data class PresupuestoItem(
    val id: String = "",
    val descripcion: String = "",
    val cantidad: Double = 0.0,
    val unidad: String? = null,
    val precioUnitario: Double = 0.0,
    val tipo: String = "MATERIAL",
    val fuente: String = "MANUAL",
    val orden: Int = 0,
    val isComprado: Boolean = false,
    val isInstalado: Boolean = false,
    val costoReal: Double? = null,
    val totalReal: Double = 0.0,
    val desvio: Double = 0.0
) {
    val subtotal: Double
        get() = cantidad * precioUnitario
}
