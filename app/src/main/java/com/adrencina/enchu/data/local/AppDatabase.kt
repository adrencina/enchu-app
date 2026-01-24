package com.adrencina.enchu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adrencina.enchu.data.model.FileEntity
import com.adrencina.enchu.data.model.MaterialEntity

@Database(
    entities = [FileEntity::class, MaterialEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun materialDao(): MaterialDao
}
