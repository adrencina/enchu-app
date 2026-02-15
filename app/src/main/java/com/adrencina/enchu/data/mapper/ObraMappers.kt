package com.adrencina.enchu.data.mapper

import com.adrencina.enchu.data.model.*
import com.adrencina.enchu.domain.model.*
import com.adrencina.enchu.data.model.MemberPermissions as MemberPermissionsDoc
import com.adrencina.enchu.domain.model.MemberPermissions as MemberPermissionsDomain

object ObraMapper {

    fun toDomain(doc: ObraDocument): Obra {
        return Obra(
            id = doc.id,
            userId = doc.userId,
            organizationId = doc.organizationId,
            clienteId = doc.clienteId,
            clienteNombre = doc.clienteNombre,
            clienteCuit = doc.clienteCuit,
            clientTaxCondition = doc.clientTaxCondition,
            nombreObra = doc.nombreObra,
            descripcion = doc.descripcion,
            telefono = doc.telefono,
            direccion = doc.direccion,
            estado = EstadoObra.fromValue(doc.estado),
            budgetNumber = doc.budgetNumber,
            isArchived = doc.isArchived,
            descuento = doc.descuento,
            validez = doc.validez,
            notas = doc.notas,
            presupuestoTotal = doc.presupuestoTotal,
            fechaCreacion = doc.fechaCreacion ?: java.util.Date(),
            lastActivity = doc.lastActivity ?: java.util.Date(),
            tareasTotales = doc.tareasTotales,
            tareasCompletadas = doc.tareasCompletadas,
            assignedMemberIds = doc.assignedMemberIds,
            memberPermissions = doc.memberPermissions.mapValues { permsToDomain(it.value) }
        )
    }

    fun toDocument(domain: Obra): ObraDocument {
        return ObraDocument(
            id = domain.id,
            userId = domain.userId,
            organizationId = domain.organizationId,
            clienteId = domain.clienteId,
            clienteNombre = domain.clienteNombre,
            clienteCuit = domain.clienteCuit,
            clientTaxCondition = domain.clientTaxCondition,
            nombreObra = domain.nombreObra,
            descripcion = domain.descripcion,
            telefono = domain.telefono,
            direccion = domain.direccion,
            estado = domain.estado.value,
            budgetNumber = domain.budgetNumber,
            isArchived = domain.isArchived,
            descuento = domain.descuento,
            validez = domain.validez,
            notas = domain.notas,
            presupuestoTotal = domain.presupuestoTotal,
            fechaCreacion = domain.fechaCreacion,
            lastActivity = domain.lastActivity,
            tareasTotales = domain.tareasTotales,
            tareasCompletadas = domain.tareasCompletadas,
            assignedMemberIds = domain.assignedMemberIds,
            memberPermissions = domain.memberPermissions.mapValues { permsToDocument(it.value) }
        )
    }

    fun permsToDomain(doc: MemberPermissionsDoc): MemberPermissionsDomain {
        return MemberPermissionsDomain(
            canEditTasks = doc.canEditTasks,
            canAddAvances = doc.canAddAvances,
            canViewFiles = doc.canViewFiles,
            canAddFiles = doc.canAddFiles,
            canViewFinances = doc.canViewFinances
        )
    }

    fun permsToDocument(domain: MemberPermissionsDomain): MemberPermissionsDoc {
        return MemberPermissionsDoc(
            canEditTasks = domain.canEditTasks,
            canAddAvances = domain.canAddAvances,
            canViewFiles = domain.canViewFiles,
            canAddFiles = domain.canAddFiles,
            canViewFinances = domain.canViewFinances
        )
    }

    // TAREAS
    fun taskToDomain(doc: TareaDocument): Tarea = Tarea(
        id = doc.id,
        userId = doc.userId,
        organizationId = doc.organizationId,
        descripcionTarea = doc.descripcionTarea,
        completada = doc.completada,
        fechaCreacion = doc.fechaCreacion,
        fechaVencimiento = doc.fechaVencimiento,
        completedByUserId = doc.completedByUserId,
        completedAt = doc.completedAt,
        completionImageUrl = doc.completionImageUrl
    )

    fun taskToDocument(domain: Tarea): TareaDocument = TareaDocument(
        id = domain.id,
        userId = domain.userId,
        organizationId = domain.organizationId,
        descripcionTarea = domain.descripcionTarea,
        completada = domain.completada,
        fechaCreacion = domain.fechaCreacion,
        fechaVencimiento = domain.fechaVencimiento,
        completedByUserId = domain.completedByUserId,
        completedAt = domain.completedAt,
        completionImageUrl = domain.completionImageUrl
    )

    // AVANCES
    fun avanceToDomain(doc: AvanceDocument): Avance = Avance(
        id = doc.id,
        userId = doc.userId,
        organizationId = doc.organizationId,
        descripcion = doc.descripcion,
        fotosUrls = doc.fotosUrls,
        fecha = doc.fecha ?: java.util.Date()
    )

    fun avanceToDocument(domain: Avance): AvanceDocument = AvanceDocument(
        id = domain.id,
        userId = domain.userId,
        organizationId = domain.organizationId,
        descripcion = domain.descripcion,
        fotosUrls = domain.fotosUrls,
        fecha = domain.fecha
    )

    // MOVIMIENTOS
    fun movementToDomain(doc: MovimientoDocument): Movimiento = Movimiento(
        id = doc.id,
        obraId = doc.obraId,
        monto = doc.monto,
        descripcion = doc.descripcion,
        tipo = doc.tipo,
        categoria = doc.categoria,
        fecha = doc.fecha ?: java.util.Date()
    )

    fun movementToDocument(domain: Movimiento): MovimientoDocument = MovimientoDocument(
        id = domain.id,
        obraId = domain.obraId,
        monto = domain.monto,
        descripcion = domain.descripcion,
        tipo = domain.tipo,
        categoria = domain.categoria,
        fecha = domain.fecha
    )
}
