package com.adrencina.enchu.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

enum class SyncState {
    PENDING,
    UPLOADING,
    SYNCED,
    CONFLICT,
    DELETED,
    FAILED
}

@Entity(
    tableName = "files",
    indices = [
        Index(value = ["workId"]),
        Index(value = ["fileId"], unique = true)
    ]
)
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileId: String, // UUID
    val workId: String,
    val userId: String,

    // Metadata
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val checksum: String, // SHA-256

    // Paths
    val localPath: String,
    val thumbnailPath: String? = null,
    val remoteUrl: String? = null,

    // State
    val syncState: SyncState,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
