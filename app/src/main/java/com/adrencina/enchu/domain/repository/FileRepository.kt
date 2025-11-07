package com.adrencina.enchu.domain.repository

import android.net.Uri
import androidx.paging.PagingData
import com.adrencina.enchu.data.model.FileEntity
import kotlinx.coroutines.flow.Flow

interface FileRepository {

    suspend fun saveFileToWork(workId: String, sourceUri: Uri): Result<FileEntity>

    fun observeFilesForWork(workId: String): Flow<PagingData<FileEntity>>

    suspend fun enqueueUpload(fileId: String)

    suspend fun downloadFile(fileId: String): Result<FileEntity>
}
