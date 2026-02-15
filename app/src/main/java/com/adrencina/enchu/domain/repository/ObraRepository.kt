package com.adrencina.enchu.domain.repository

import com.adrencina.enchu.domain.common.Resource
import com.adrencina.enchu.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ObraRepository {
    suspend fun createObraFromPresupuesto(presupuesto: Presupuesto): Result<String>
    fun getObras(): Flow<Resource<List<Obra>>>
    fun getArchivedObras(): Flow<Resource<List<Obra>>>
    suspend fun archiveObra(obraId: String): Result<Unit>
    suspend fun deleteObra(obraId: String): Result<Unit>
    suspend fun saveObra(obra: Obra): Result<Unit>
    fun getObraById(obraId: String): Flow<Obra?>
    suspend fun updateObra(obra: Obra): Result<Unit>

    // Tareas
    fun getTareas(obraId: String): Flow<List<Tarea>>
    suspend fun addTarea(obraId: String, tarea: Tarea): Result<Unit>
    suspend fun updateTareaStatus(obraId: String, tareaId: String, completada: Boolean): Result<Unit>
    suspend fun completeTareaWithImage(obraId: String, tareaId: String, imageUri: android.net.Uri): Result<Unit>
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
    suspend fun updateItemLogistics(obraId: String, itemId: String, isComprado: Boolean, isInstalado: Boolean, costoReal: Double?): Result<Unit>
    
    // Gestión de Personal
    suspend fun assignMemberToObra(obraId: String, memberId: String): Result<Unit>
    suspend fun removeMemberFromObra(obraId: String, memberId: String): Result<Unit>
    suspend fun updateMemberPermissions(obraId: String, memberId: String, permissions: MemberPermissions): Result<Unit>
}
