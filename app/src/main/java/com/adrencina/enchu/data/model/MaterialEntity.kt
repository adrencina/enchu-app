package com.adrencina.enchu.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "materials",
    indices = [Index(value = ["name", "keywords"], unique = false)]
)
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val unit: String,
    val keywords: String // Comma separated for simpler querying
)
