package com.adrencina.enchu.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.adrencina.enchu.data.model.FileEntity
import com.adrencina.enchu.data.model.SyncState

@Dao
interface FileDao {

    @Query("SELECT * FROM files WHERE workId = :workId ORDER BY createdAt DESC")
    fun filesForWork(workId: String): PagingSource<Int, FileEntity>

    @Upsert
    suspend fun upsert(file: FileEntity)

    @Query("UPDATE files SET syncState = :syncState WHERE fileId = :fileId")
    suspend fun updateSyncState(fileId: String, syncState: SyncState)

    @Query("DELETE FROM files WHERE fileId = :fileId")
    suspend fun delete(fileId: String)

    @Query("SELECT * FROM files WHERE fileId = :fileId")
    suspend fun getFileById(fileId: String): FileEntity?

    @Query("UPDATE files SET remoteUrl = :remoteUrl, syncState = :syncState WHERE fileId = :fileId")
    suspend fun updateFileStatus(fileId: String, remoteUrl: String, syncState: SyncState)

}
