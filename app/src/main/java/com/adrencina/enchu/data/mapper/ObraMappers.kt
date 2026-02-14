package com.adrencina.enchu.data.mapper

import com.adrencina.enchu.data.model.AvanceDocument
import com.adrencina.enchu.data.model.MovimientoDocument
import com.adrencina.enchu.data.model.ObraDocument
import com.adrencina.enchu.data.model.PresupuestoItemDocument
import com.adrencina.enchu.data.model.TareaDocument
import com.adrencina.enchu.domain.model.Avance
import com.adrencina.enchu.domain.model.EstadoObra
import com.adrencina.enchu.domain.model.Movimiento
import com.adrencina.enchu.domain.model.Obra
import com.adrencina.enchu.domain.model.PresupuestoItem
import com.adrencina.enchu.domain.model.Tarea

// --- OBRA ---

fun ObraDocument.toDomain(): Obra {
    return Obra(
        id = this.id,
        userId = this.userId,
        organizationId = this.organizationId,
        clienteId = this.clienteId,
        clienteNombre = this.clienteNombre,
        clienteCuit = this.clienteCuit,
        clientTaxCondition = this.clientTaxCondition,
        nombreObra = this.nombreObra,
        descripcion = this.descripcion,
        telefono = this.telefono,
        direccion = this.direccion,
        estado = EstadoObra.fromValue(this.estado),
        budgetNumber = this.budgetNumber,
        isArchived = this.isArchived,
        descuento = this.descuento,
        validez = this.validez,
        notas = this.notas,
        presupuestoTotal = this.presupuestoTotal,
        fechaCreacion = this.fechaCreacion,
        lastActivity = this.lastActivity,
        tareasTotales = this.tareasTotales,
        tareasCompletadas = this.tareasCompletadas
    )
}

fun Obra.toDocument(): ObraDocument {
    return ObraDocument(
        id = this.id,
        userId = this.userId,
        organizationId = this.organizationId,
        clienteId = this.clienteId,
        clienteNombre = this.clienteNombre,
        clienteCuit = this.clienteCuit,
        clientTaxCondition = this.clientTaxCondition,
        nombreObra = this.nombreObra,
        descripcion = this.descripcion,
        telefono = this.telefono,
        direccion = this.direccion,
        estado = this.estado.value,
        budgetNumber = this.budgetNumber,
        isArchived = this.isArchived,
        descuento = this.descuento,
        validez = this.validez,
        notas = this.notas,
        presupuestoTotal = this.presupuestoTotal,
        fechaCreacion = this.fechaCreacion,
        lastActivity = this.lastActivity,
        tareasTotales = this.tareasTotales,
        tareasCompletadas = this.tareasCompletadas
    )
}

// --- TAREA ---

fun TareaDocument.toDomain(): Tarea {
    return Tarea(
        id = this.id,
        userId = this.userId,
        organizationId = this.organizationId,
        descripcionTarea = this.descripcionTarea,
        completada = this.completada,
        fechaCreacion = this.fechaCreacion,
        fechaVencimiento = this.fechaVencimiento
    )
}

fun Tarea.toDocument(): TareaDocument {
    return TareaDocument(
        id = this.id,
        userId = this.userId,
        organizationId = this.organizationId,
        descripcionTarea = this.descripcionTarea,
        completada = this.completada,
        fechaCreacion = this.fechaCreacion,
        fechaVencimiento = this.fechaVencimiento
    )
}

// --- AVANCE ---

fun AvanceDocument.toDomain(): Avance {
    return Avance(
        id = this.id,
        userId = this.userId,
        organizationId = this.organizationId,
        descripcion = this.descripcion,
        fotosUrls = this.fotosUrls,
        fecha = this.fecha
    )
}

fun Avance.toDocument(): AvanceDocument {
    return AvanceDocument(
        id = this.id,
        userId = this.userId,
        organizationId = this.organizationId,
        descripcion = this.descripcion,
        fotosUrls = this.fotosUrls,
        fecha = this.fecha
    )
}

// --- MOVIMIENTO ---

fun MovimientoDocument.toDomain(): Movimiento {
    return Movimiento(
        id = this.id,
        userId = this.userId,
        organizationId = this.organizationId,
        obraId = this.obraId,
        descripcion = this.descripcion,
        monto = this.monto,
        tipo = this.tipo,
        categoria = this.categoria,
        fecha = this.fecha
    )
}

fun Movimiento.toDocument(): MovimientoDocument {
    return MovimientoDocument(
        id = this.id,
        userId = this.userId,
        organizationId = this.organizationId,
        obraId = this.obraId,
        descripcion = this.descripcion,
        monto = this.monto,
        tipo = this.tipo,
        categoria = this.categoria,
        fecha = this.fecha
    )
}

// --- PRESUPUESTO ITEM (FIRESTORE) ---

fun PresupuestoItemDocument.toDomain(): PresupuestoItem {
    return PresupuestoItem(
        id = this.id,
        userId = this.userId,
        organizationId = this.organizationId,
        descripcion = this.descripcion,
        cantidad = this.cantidad,
        unidad = null,
        precioUnitario = this.precioUnitario,
        tipo = this.tipo,
        fuente = "MANUAL",
        orden = 0,
        isComprado = this.isComprado,
        isInstalado = this.isInstalado,
        costoReal = this.costoReal,
        totalReal = this.totalReal,
        desvio = this.desvio
    )
}

fun PresupuestoItem.toDocument(): PresupuestoItemDocument {
    return PresupuestoItemDocument(
        id = this.id,
        userId = this.userId,
        organizationId = this.organizationId,
        descripcion = this.descripcion,
        cantidad = this.cantidad,
        precioUnitario = this.precioUnitario,
        tipo = this.tipo,
        isComprado = this.isComprado,
        isInstalado = this.isInstalado,
        costoReal = this.costoReal,
        fechaCreacion = null
    )
}
