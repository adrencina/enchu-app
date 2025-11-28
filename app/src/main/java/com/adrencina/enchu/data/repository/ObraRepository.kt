package com.adrencina.enchu.data.repository

import com.adrencina.enchu.data.model.Obra
import kotlinx.coroutines.flow.Flow

/**
 * Esta es la interfaz (el contrato) para nuestro Repositorio.
 * Define qué acciones se pueden realizar con los datos de las obras.
 * El resto de la app usará esta interfaz, no la implementación directa.
 */
interface ObraRepository {

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
}