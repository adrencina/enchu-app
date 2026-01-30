package com.adrencina.enchu.data.repository

import com.adrencina.enchu.data.model.Avance
import com.adrencina.enchu.data.model.Movimiento
import com.adrencina.enchu.data.model.Obra
import com.adrencina.enchu.data.model.PresupuestoItem
import com.adrencina.enchu.data.model.PresupuestoWithItems
import com.adrencina.enchu.data.model.Tarea
import kotlinx.coroutines.flow.Flow

/**
 * Esta es la interfaz (el contrato) para nuestro Repositorio.
 * Define qué acciones se pueden realizar con los datos de las obras.
 * El resto de la app usará esta interfaz, no la implementación directa.
 */
interface ObraRepository {

    /**
     * Crea una nueva Obra a partir de un Presupuesto existente (local).
     * Transfiere los datos del cliente y los items del presupuesto a Firestore.
     * @return Result<String> con el ID de la nueva obra creada.
     */
    suspend fun createObraFromPresupuesto(presupuesto: PresupuestoWithItems): Result<String>

    /**
     * Obtiene una lista de todas las obras de un usuario en tiempo real.
     * Usamos Flow para que la UI se actualice automáticamente si hay cambios en la base de datos.
     */
    fun getObras(): Flow<List<Obra>>

    fun getArchivedObras(): Flow<List<Obra>>

    suspend fun saveObra(obra: Obra): Result<Unit>

    fun getObraById(obraId: String): Flow<Obra>

    suspend fun updateObra(obra: Obra): Result<Unit>

    suspend fun archiveObra(obraId: String): Result<Unit>

    suspend fun deleteObra(obraId: String): Result<Unit>

    // Tareas
    fun getTareas(obraId: String): Flow<List<Tarea>>
    suspend fun addTarea(obraId: String, tarea: Tarea): Result<Unit>
    suspend fun updateTareaStatus(obraId: String, tareaId: String, completada: Boolean): Result<Unit>
    suspend fun deleteTarea(obraId: String, tareaId: String): Result<Unit>

    // Avances
    fun getAvances(obraId: String): Flow<List<Avance>>
    suspend fun addAvance(obraId: String, avance: Avance): Result<Unit>
    suspend fun deleteAvance(obraId: String, avanceId: String): Result<Unit>

    // Caja (Movimientos)
    fun getMovimientos(obraId: String): Flow<List<Movimiento>>
    suspend fun addMovimiento(obraId: String, movimiento: Movimiento): Result<Unit>
    suspend fun deleteMovimiento(obraId: String, movimientoId: String): Result<Unit>

    // Presupuesto
    fun getPresupuestoItems(obraId: String): Flow<List<PresupuestoItem>>
    suspend fun addPresupuestoItem(obraId: String, item: PresupuestoItem): Result<Unit>
    suspend fun updatePresupuestoItem(obraId: String, item: PresupuestoItem): Result<Unit>
    suspend fun deletePresupuestoItem(obraId: String, itemId: String): Result<Unit>
    
    /**
     * Actualiza el estado logístico de un ítem (Comprado, Instalado) y su costo real.
     */
    suspend fun updateItemLogistics(
        obraId: String, 
        itemId: String, 
        isComprado: Boolean, 
        isInstalado: Boolean, 
        costoReal: Double?
    ): Result<Unit>
}