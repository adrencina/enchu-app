package com.adrencina.enchu.data.local

import androidx.room.*
import com.adrencina.enchu.data.model.PresupuestoEntity
import com.adrencina.enchu.data.model.PresupuestoItemEntity
import com.adrencina.enchu.data.model.PresupuestoWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface PresupuestoDao {
    @Transaction
    @Query("SELECT * FROM presupuestos ORDER BY creadoEn DESC")
    fun getAllPresupuestosWithItems(): Flow<List<PresupuestoWithItems>>

    @Transaction
    @Query("SELECT * FROM presupuestos WHERE id = :id")
    suspend fun getPresupuestoWithItemsById(id: String): PresupuestoWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresupuesto(presupuesto: PresupuestoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PresupuestoItemEntity>)

    @Query("DELETE FROM presupuesto_items WHERE presupuestoId = :presupuestoId")
    suspend fun deleteItemsByPresupuestoId(presupuestoId: String)

    @Transaction
    suspend fun upsertPresupuestoWithItems(presupuesto: PresupuestoEntity, items: List<PresupuestoItemEntity>) {
        insertPresupuesto(presupuesto)
        deleteItemsByPresupuestoId(presupuesto.id)
        insertItems(items)
    }

    @Delete
    suspend fun deletePresupuesto(presupuesto: PresupuestoEntity)

    @Update
    suspend fun updatePresupuesto(presupuesto: PresupuestoEntity)
}
