package com.adrencina.enchu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.adrencina.enchu.data.model.MaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query("""
        SELECT * FROM materials 
        WHERE name LIKE '%' || :query || '%' 
        OR keywords LIKE '%' || :query || '%' 
        OR category LIKE '%' || :query || '%'
        ORDER BY name ASC 
        LIMIT 50
    """)
    fun searchMaterials(query: String): Flow<List<MaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(materials: List<MaterialEntity>)

    @Query("DELETE FROM materials")
    suspend fun clearAll()
    
    @Query("SELECT COUNT(*) FROM materials")
    suspend fun getCount(): Int
}
