package com.adrencina.enchu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adrencina.enchu.data.model.FileEntity
import com.adrencina.enchu.data.model.MaterialEntity
import com.adrencina.enchu.data.model.PresupuestoEntity
import com.adrencina.enchu.data.model.PresupuestoItemEntity

@Database(
    entities = [
        FileEntity::class, 
        MaterialEntity::class, 
        PresupuestoEntity::class, 
        PresupuestoItemEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun materialDao(): MaterialDao
    abstract fun presupuestoDao(): PresupuestoDao
}
