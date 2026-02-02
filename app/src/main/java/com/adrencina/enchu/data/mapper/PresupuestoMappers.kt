package com.adrencina.enchu.data.mapper

import com.adrencina.enchu.data.model.PresupuestoEntity
import com.adrencina.enchu.data.model.PresupuestoItemEntity
import com.adrencina.enchu.domain.model.Presupuesto
import com.adrencina.enchu.domain.model.PresupuestoItem

fun PresupuestoItemEntity.toDomain(): PresupuestoItem {
    return PresupuestoItem(
        id = this.localId.toString(), // Convertimos Long a String
        descripcion = this.descripcion,
        cantidad = this.cantidad,
        unidad = this.unidad,
        precioUnitario = this.precioUnitario,
        tipo = this.tipo,
        fuente = this.fuente,
        orden = this.orden,
        isComprado = false,
        isInstalado = false,
        costoReal = null
    )
}

fun PresupuestoEntity.toDomain(items: List<PresupuestoItemEntity>): Presupuesto {
    return Presupuesto(
        id = this.id,
        titulo = this.titulo,
        clienteId = this.clienteId,
        clienteNombre = this.clienteNombre,
        clienteApellido = this.clienteApellido,
        clienteDireccion = this.clienteDireccion,
        clienteTelefono = this.clienteTelefono,
        clienteEmail = this.clienteEmail,
        items = items.map { it.toDomain() },
        subtotal = this.subtotal,
        impuestos = this.impuestos,
        descuento = this.descuento,
        total = this.total,
        estado = this.estado,
        creadoEn = this.creadoEn,
        aprobadoEn = this.aprobadoEn,
        aprobadoPor = this.aprobadoPor,
        notas = this.notas,
        validez = this.validez,
        numero = this.numero
    )
}