package com.adrencina.enchu.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class PresupuestoWithItems(
    @Embedded val presupuesto: PresupuestoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "presupuestoId"
    )
    val items: List<PresupuestoItemEntity>
)
