package com.adrencina.enchu.data.repository

import com.adrencina.enchu.data.model.MaterialEntity
import kotlinx.coroutines.flow.Flow

interface MaterialRepository {
    fun searchMaterials(query: String): Flow<List<MaterialEntity>>
    suspend fun syncMaterials()
    suspend fun getMaterialCount(): Int
}
