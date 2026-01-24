package com.adrencina.enchu.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "presupuesto_items",
    foreignKeys = [
        ForeignKey(
            entity = PresupuestoEntity::class,
            parentColumns = ["id"],
            childColumns = ["presupuestoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["presupuestoId"])]
)
data class PresupuestoItemEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val presupuestoId: String,
    val descripcion: String,
    val cantidad: Double,
    val unidad: String? = null,
    val precioUnitario: Double,
    val tipo: String, // MATERIAL, MANO_OBRA, OTRO
    val fuente: String, // MANUAL, CATALOGO_LOCAL
    val orden: Int = 0 // Para drag & drop
)
