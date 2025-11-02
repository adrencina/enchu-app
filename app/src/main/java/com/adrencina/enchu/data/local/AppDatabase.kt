package com.adrencina.enchu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adrencina.enchu.data.model.FileEntity

@Database(
    entities = [FileEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
}
