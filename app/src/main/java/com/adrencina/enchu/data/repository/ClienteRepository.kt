package com.adrencina.enchu.data.repository

import com.adrencina.enchu.data.model.Cliente
import kotlinx.coroutines.flow.Flow

interface ClienteRepository {
    fun getClientes(): Flow<List<Cliente>>

    suspend fun saveCliente(cliente: Cliente): Result<Unit>

    suspend fun doesDniExist(dni: String): Boolean
}